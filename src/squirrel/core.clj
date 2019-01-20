(ns squirrel.core
  (:import
   (org.apache.flink.api.common.functions ReduceFunction FlatMapFunction)
   (org.apache.flink.api.java.tuple Tuple2)
   (org.apache.flink.streaming.api.datastream DataStreamSource)
   (org.apache.flink.streaming.api.environment StreamExecutionEnvironment)
   (org.apache.flink.streaming.api.windowing.time Time))
  (:require
   [clojure.string :as str]
   ;; [squirrel.utils :refer [to-vec]]
   )
  (:gen-class)
  )

(defn to-vecc [t]
  (condp = (type t)
    Tuple2 (let [^Tuple2 t t] [(.f0 t) (.f1 t)])))

(deftype SplitLine []
  FlatMapFunction
  (flatMap [_ line collector]
    (doseq [w (str/split line #"\s")]
      (.collect collector (Tuple2. w (long 1))))))

(deftype CountWords []
  ReduceFunction
  (reduce [_ t1 t2]
    (let [[w1 c1] (to-vecc t1)
          [w1 c2] (to-vecc t2)]
      (Tuple2. w1 (+ c1 c2)))))

(defn window-word-count [window-sec]
  (fn [^DataStreamSource stream]
    (-> stream
        (.flatMap (SplitLine.))
        (.returns "Tuple2<String,Long>")
        (.keyBy (int-array [0]))
        (.timeWindow (Time/seconds window-sec))
        (.reduce (CountWords.)))))

(defn -main [& args]
  (let [fenv (StreamExecutionEnvironment/getExecutionEnvironment)]
    (-> fenv
        (.socketTextStream "localhost" (Integer/parseInt (first args)) "\n")
        ^DataStreamSource
        ((window-word-count 5))
        (.print)
        (.setParallelism 1)) ; print in a single thread
    (.execute fenv "window-word-count")))
