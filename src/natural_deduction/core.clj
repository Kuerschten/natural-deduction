(ns natural-deduction.core
  (require 
    [clojure.core.logic]
    [clojure.walk :refer :all]))

(load "scope")
(load "apply_rule")
(load "proof")

stop

(pretty-printer (proof-step (first (load-rules "resources/rules/natdec.clj")) (build-proof '[a b INFER (b ∧ a)]) true 1 2 3))

(pretty-printer (proof-step (first (load-rules "resources/rules/natdec.clj")) (build-proof '[a b INFER (b ∧ c)]) true 1 2 3))
