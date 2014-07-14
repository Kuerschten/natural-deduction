(ns natural-deduction.core
  (require 
    [clojure.core.logic]
    [clojure.walk :refer :all]))

(load "scope")
(load "apply_rule")
(load "proof")

stop

; foreward
(pretty-printer (proof-step
                  (build-proof '[a b INFER (b ∧ a)])
                  (first (clojure.set/select #(= (:name %) "and-i2") (load-rules "resources/rules/natdec.clj")))
                  true
                  1 2 3))

(pretty-printer (proof-step
                  (build-proof '[a b INFER c])
                  (first (clojure.set/select #(= (:name %) "and-i2") (load-rules "resources/rules/natdec.clj")))
                  true
                  1 2 3))

; backward
(pretty-printer (proof-step
                  (build-proof '[INFER (a → b)])
                  (first (clojure.set/select #(= (:name %) "impl-i") (load-rules "resources/rules/natdec.clj")))
                  false
                  1 5))

(pretty-printer (proof-step
                  (build-proof '[a INFER (a ∧ b)])
                  (first (clojure.set/select #(= (:name %) "and-i1-backward") (load-rules "resources/rules/natdec.clj")))
                  false
                  2 6))

(pretty-printer (proof-step
                  (build-proof '[b INFER (a ∧ b)])
                  (first (clojure.set/select #(= (:name %) "and-i1-backward") (load-rules "resources/rules/natdec.clj")))
                  false
                  2 6))

; between
(pretty-printer (proof-step
                  (build-proof '[(a ∨ b) INFER X])
                  (first (clojure.set/select #(= (:name %) "or-e") (load-rules "resources/rules/natdec.clj")))
                  true
                  4 5 6))

(pretty-printer (proof-step
                  (build-proof '[(predicate-formula ∃ x (P(x))) INFER X])
                  (first (clojure.set/select #(= (:name %) "exists-e") (load-rules "resources/rules/natdec.clj")))
                  true
                  8 9 10))

(pretty-printer (proof-step
                  (build-proof '[(var i) INFER (predicate-formula ∃ x (P(x)))])
                  (first (clojure.set/select #(= (:name %) "exists-i") (load-rules "resources/rules/natdec.clj")))
                  false
                  3 4 12))
