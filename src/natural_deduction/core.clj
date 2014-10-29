(ns natural-deduction.core
  (require 
    [clojure.core.logic]
    [clojure.pprint :as pp]
    [clojure.walk :refer :all]
    [clojure.math.combinatorics :as combo]))

(load "scope")
(declare get-theorem)
(load "apply_rule")
(load "proof")
(load "file_handling")
