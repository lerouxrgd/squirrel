(defproject squirrel "0.1.0"
  :description "Demo of Flink usage from Clojure"
  :dependencies
  [[org.clojure/clojure "1.10.0"]
   [org.apache.flink/flink-streaming-java_2.11 "1.4.2"]]
  :aot :all
  :repl-options {:init-ns squirrel.core}
  :global-vars {*warn-on-reflection* true})
