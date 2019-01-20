(defproject squirrel "0.1.0"
  :description "Demo of Flink usage from Clojure"
  :dependencies
  [[org.clojure/clojure "1.10.0"]
   [org.apache.flink/flink-streaming-java_2.11 "1.4.2" :scope "provided"]]
  :main squirrel.core
  :aot :all
  :repl-options {:init-ns squirrel.core}
  :global-vars {*warn-on-reflection* true}
  :packaging "clojure"
  :pom-addition
  [:pluginRepositories
   ([:pluginRepository
     [:id "sonatype-forge"]
     [:url "https://repository.sonatype.org/content/groups/forge"]])]
  :pom-plugins
  [[com.theoryinpractise/clojure-maven-plugin "1.8.1"
    {:extensions "true"
     :configuration
     ([:sourceDirectories
       ([:sourceDirectory "src"])])}]
   [org.apache.maven.plugins/maven-assembly-plugin "3.1.1"
    {:executions
     ([:execution
       [:id "uberjar"]
       [:phase "package"]
       [:goals ([:goal "single"])]
       [:configuration
        [:descriptorRefs ([:descriptorRef "jar-with-dependencies"])]
        [:archive [:manifest [:mainClass "squirrel.core"]]]
        [:finalName "${project.name}-standalone"]
        [:appendAssemblyId "false"]]])}]])
