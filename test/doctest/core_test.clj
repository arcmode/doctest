(ns doctest.core-test
  (:require [clojure.test :refer :all]
            [doctest.core :refer :all]))

(defn example
  "Example doc-tested fn.

   This function exists only as a sample to be used while
   doctesting `doctet.core/doctest-ns`. Should be removed
   as soon as we have a mocking system.

  => (example 1 2 3 4)
     10"
  [& numbers]
  (apply + numbers))

(doctest-ns doctest.core)
