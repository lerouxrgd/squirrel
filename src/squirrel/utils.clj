(ns squirrel.utils
  (:import
   (java.util ArrayList)
   (org.apache.flink.api.common.functions.util ListCollector)
   (org.apache.flink.api.java.tuple Tuple2)
   (org.apache.flink.streaming.api.datastream DataStreamSource)
   (org.apache.flink.streaming.api.environment StreamExecutionEnvironment)
   (org.apache.flink.streaming.api.functions.sink SinkFunction)
   (squirrel.core SplitLine)))

(def ^:dynamic *sink* (ArrayList.))

(deftype DynSink []
  SinkFunction
  (invoke [this v]
    (.add ^ArrayList *sink* v)))

(defmacro flink-exec [steps-fn inputs]
  `(let [fenv# (doto (StreamExecutionEnvironment/getExecutionEnvironment)
                (.setParallelism 1))]
     (-> fenv#
         (.fromElements (object-array ~inputs))
         ^DataStreamSource (~steps-fn)
         (.addSink (DynSink.)))
     (.execute fenv#)
     (let [res# (into [] *sink*)]
       (.clear ^ArrayList *sink*)
       res#)))

(defn capture-flatmap [flatmapper input]
  (let [res (ArrayList.)]
    (.flatMap flatmapper input (ListCollector. res))
    res))

(defprotocol ToVec
  (to-vec [this]))

(extend-protocol ToVec
  Tuple2
  (to-vec [this]
    [(.f0 this) (.f1 this)]))

