(ns squirrel.utils
  (:import
   (org.apache.flink.api.common.functions FlatMapFunction)
   (org.apache.flink.api.common.functions.util ListCollector)
   (org.apache.flink.api.java.typeutils ResultTypeQueryable)
   (org.apache.flink.streaming.api.datastream DataStreamSource)
   (org.apache.flink.streaming.api.environment StreamExecutionEnvironment)
   (org.apache.flink.streaming.api.functions.sink SinkFunction)
   (org.apache.flink.streaming.api.functions.source SourceFunction)))

(deftype WaitingSource [wait-ms output-type outputs]
  SourceFunction
  (run [_ ctx]
    (doseq [o outputs]
      (.collect ctx o))
    (Thread/sleep wait-ms))
  ResultTypeQueryable
  (getProducedType [_]
    output-type))

(def ^:dynamic *sink*)

(deftype DynSink []
  SinkFunction
  (invoke [_ v]
    (conj! *sink* v)))

(defmacro flink-exec [steps-fn src]
  `(let [fenv# (doto (StreamExecutionEnvironment/getExecutionEnvironment)
                 (.setParallelism 1))]
     (with-redefs [*sink* (transient [])]
       (-> fenv#
           (.addSource ~src)
           ^DataStreamSource (~steps-fn)
           (.addSink (DynSink.)))
       (.execute fenv#)
       (persistent! *sink*))))

(defn capture-flatmap [^FlatMapFunction fmfn input]
  (let [res (java.util.ArrayList.)]
    (.flatMap fmfn input (ListCollector. res))
    (into [] res)))
