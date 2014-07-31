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

; proofs
(def rules (load-rules "resources/rules/natdec.clj"))

(pretty-printer
  (-> (build-proof '[P INFER (¬ (¬ P))])
    (proof-step-backward (get-rule rules "not-i")  2 7)
    (proof-step-foreward (get-rule rules "and-i1")  1 10 11)
    (proof-step-foreward (get-rule rules "not-e")  13 11)
    ))

(pretty-printer
  (-> (build-proof '[(¬ (¬ P)) INFER P])
    (proof-step-backward (get-rule rules "raa")  6 7)
    (proof-step-foreward (get-rule rules "and-i2") 5 10 11)
    (proof-step-foreward (get-rule rules "not-e") 13 11)
    ))

(pretty-printer
  (-> (build-proof '[(P → Q) (¬ Q) INFER (¬ P)])
    (proof-step-backward (get-rule rules "not-i") 8 11)
    (proof-step-foreward (get-rule rules "same") 4 13)
    (proof-step-foreward (get-rule rules "impl-e") 12 15 13)
    (proof-step-foreward (get-rule rules "and-i2") 7 16 13)
    (proof-step-foreward (get-rule rules "not-e") 17 13)
    ))

(pretty-printer
  (-> (build-proof '[INFER (P ∨ (¬ P))])
    (proof-step-backward (get-rule rules "raa") 1 7)
    ; ???
    ))

(pretty-printer
  (-> (build-proof '[a INFER (a ∨ b)])
    (proof-step-foreward (get-rule rules "or-i1") 1 2)
    (unify 8 'new7 'b)
    (proof-step-foreward (get-rule rules "same") 8 2)
    ))