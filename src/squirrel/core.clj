(ns squirrel.core
  (:import
   (org.apache.flink.api.common.functions FlatMapFunction)
   (org.apache.flink.api.java.tuple Tuple2)
   (org.apache.flink.streaming.api.datastream DataStreamSource))
  (:require
   [clojure.string :as str]))

(deftype SplitLine []
  FlatMapFunction
  (flatMap [_ line collector]
    (doseq [w (str/split line #"\s")]
      (.collect collector (Tuple2. w (long 1))))))

(defn word-count [^DataStreamSource stream]
  (-> stream
      (.flatMap (SplitLine.))
      (.returns "Tuple2<String,Long>")
      (.keyBy (int-array [0]))
      (.sum 1)))
