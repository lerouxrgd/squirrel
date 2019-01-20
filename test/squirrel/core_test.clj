(ns squirrel.core-test
  (:import
   (squirrel.core SplitLine CountWords)
   (squirrel.utils WaitingSource))
  (:require
   [clojure.test :refer :all]
   [squirrel.core :refer :all]
   [squirrel.utils :refer :all]))

(deftest unit-test
  (testing "SplitLine is correct"
    (is (= [["how" 1] ["are" 1] ["you" 1]]
           (->> (capture-flatmap (SplitLine.) "how are you")
                (map to-vec)
                (into [])))))
  (testing "CountWords is correct"
    (is (= ["yo" 3] 
           (->> (.reduce (CountWords.) (tuple "yo" 1) (tuple "yo" 2))
                (to-vec))))))

(deftest integration-test
  (testing "Windowed word count"
    (is (= {"do" 3 "you" 1 "yes" 1 "i" 1}
           (->> ["do you do" "yes i do"]
                (WaitingSource. 1200)
                (flink-exec (window-word-count 1) "String")
                (map to-vec)
                (into {}))))))
