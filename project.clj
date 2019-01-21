(defproject squirrel "0.1.0"
  :description "Flink window word count example in Clojure"
  :dependencies
  [[org.clojure/clojure "1.10.0"]]
  :main squirrel.core
  :aot [squirrel.core]
  :global-vars {*warn-on-reflection* true}
  :profiles
  {:provided
   {:dependencies
    [[org.apache.flink/flink-streaming-java_2.11 "1.7.1"]]}
   :repl
   {:repl-options {:init-ns squirrel.core}
    :aot [squirrel.utils]}
   :test
   {:aot [squirrel.utils]}})
