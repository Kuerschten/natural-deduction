(ns natural-deduction.core
  (require 
    [clojure.core.logic]
    [clojure.walk :refer :all]))

(load "scope")
(load "apply_rule")
(load "proof")

stop

(pretty-printer (proof-step
                  (first(clojure.set/select #(= (:name %) "and-i2") (load-rules "resources/rules/natdec.clj")))
                  (build-proof '[a b INFER (b ∧ a)])
                  true
                  1 2 3))

(pretty-printer (proof-step
                  (first(clojure.set/select #(= (:name %) "and-i2") (load-rules "resources/rules/natdec.clj")))
                  (build-proof '[a b INFER c])
                  true
                  1 2 3))

(pretty-printer (proof-step
                  (first(clojure.set/select #(= (:name %) "impl-i") (load-rules "resources/rules/natdec.clj")))
                  (build-proof '[INFER (a → b)])
                  false
                  1 5))

(pretty-printer (proof-step
                  (first(clojure.set/select #(= (:name %) "and-i1-backward") (load-rules "resources/rules/natdec.clj")))
                  (build-proof '[a INFER (a ∧ b)])
                  false
                  2 6))

(pretty-printer (proof-step
                  (first(clojure.set/select #(= (:name %) "and-i1-backward") (load-rules "resources/rules/natdec.clj")))
                  (build-proof '[b INFER (a ∧ b)])
                  false
                  2 6))
