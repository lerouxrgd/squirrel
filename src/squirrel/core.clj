(ns squirrel.core
  (:import
   (org.apache.flink.api.common.functions FlatMapFunction ReduceFunction)
   (org.apache.flink.api.common.typeinfo TypeInformation)
   (org.apache.flink.api.java.tuple Tuple2)
   (org.apache.flink.api.java.typeutils ResultTypeQueryable TupleTypeInfo)
   (org.apache.flink.streaming.api.datastream DataStreamSource)
   (org.apache.flink.streaming.api.environment StreamExecutionEnvironment)
   (org.apache.flink.streaming.api.windowing.time Time))
  (:require
   [clojure.string :as str])
  (:gen-class))

;; Utility functions and macros

(defprotocol ToVec
  (to-vec [this]))

(extend-protocol ToVec
  Tuple2
  (to-vec [^Tuple2 this]
    [(.f0 this) (.f1 this)]))

(defmacro with-require [namespaces & body]
  `(let [init# (volatile! false)]
     (when-not @init#
       ~@(for [ns namespaces]
           `(require '~ns))
       (vreset! init# true))
     ~@body))

(defmacro type-of [type]
  `(TypeInformation/of ~type))

(defmacro tuple-of [& types]
  `(TupleTypeInfo.
    (into-array TypeInformation ~(vec (for [^Class t types] `(type-of ~t))))))

(defmacro tuple [& vals]
  (case (count vals)
    2 (let [[a b] vals] `(Tuple2. ~a ~b))))

;; Topology definition

(deftype SplitLine []
  FlatMapFunction
  (flatMap [_ line collector]
    (doseq [w (str/split line #"\s")]
      (.collect collector (tuple w (long 1)))))
  ResultTypeQueryable
  (getProducedType [_]
    (tuple-of String Long)))

(deftype CountWords []
  ReduceFunction
  (reduce [_ t1 t2]
    (with-require [squirrel.core]
      (let [[w1 c1] (to-vec t1)
            [w1 c2] (to-vec t2)]
        (tuple w1 (+ c1 c2))))))

(defn window-word-count [window-sec]
  (fn [^DataStreamSource stream]
    (-> stream
        (.flatMap (SplitLine.))
        (.keyBy (int-array [0]))
        (.timeWindow (Time/seconds window-sec))
        (.reduce (CountWords.)))))

(defn -main [& args]
  (let [fenv (StreamExecutionEnvironment/getExecutionEnvironment)]
    (-> fenv
        (.socketTextStream "localhost" (Integer/parseInt (first args)) "\n")
        ^DataStreamSource
        ((window-word-count 10))
        (.print)
        (.setParallelism 1)) ; print in a single thread
    (.execute fenv "window-word-count")))
