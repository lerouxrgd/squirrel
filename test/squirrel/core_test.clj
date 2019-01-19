(ns squirrel.core-test
  (:import
   (squirrel.core SplitLine))
  (:require
   [clojure.test :refer :all]
   [squirrel.core :refer :all]
   [squirrel.utils :refer :all]))

(deftest unit-test
  (testing "Correctness of SplitLine"
    (is (= [["how" 1] ["are" 1] ["you" 1]]
           (->> (capture-flatmap (SplitLine.) "how are you")
                (map to-vec)
                (into []))))))

(deftest integration-test
  (testing "Whole word count processing"
    (is (= {"do" 3 "you" 1 "yes" 1 "i" 1}
           (->> (flink-exec word-count ["do you do" "yes i do"])
                (map to-vec)
                (into {}))))))
