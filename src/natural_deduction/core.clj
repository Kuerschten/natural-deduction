(ns natural-deduction.core
  (require 
    [clojure.core.logic]
    [clojure.walk :refer :all]))

(load "scope")
(load "apply_rule")
(load "proof")

stop

(def rules (load-rules "resources/rules/natdec.clj"))

; foreward
(pretty-printer (proof-step-foreward
                  (build-proof '[a b INFER (b ∧ a)])
                  (get-rule rules "and-i2")
                  1 2 3))

(pretty-printer (proof-step-foreward
                  (build-proof '[a b INFER c])
                  (get-rule rules "and-i2")
                  1 2 3))

; backward
(pretty-printer (proof-step-backward
                  (build-proof '[INFER (a → b)])
                  (get-rule rules "impl-i")
                  1 2))

(pretty-printer (proof-step-backward
                  (build-proof '[a INFER (a ∧ b)])
                  (get-rule rules "and-i1-backward")
                  2 3))

(pretty-printer (proof-step-backward
                  (build-proof '[b INFER (a ∧ b)])
                  (get-rule rules "and-i1-backward")
                  2 3))

; inside
(pretty-printer (proof-step-foreward
                  (build-proof '[(a ∨ b) INFER X])
                  (get-rule rules "or-e")
                  1 2 3))

(pretty-printer (proof-step-foreward
                  (build-proof '[(predicate-formula ∃ x (P(x))) INFER X])
                  (get-rule rules "exists-e")
                  1 2 3))

(pretty-printer (proof-step-backward
                  (build-proof '[(var i) INFER (predicate-formula ∃ x (P(x)))])
                  (get-rule rules "exists-i")
                  1 2 3))

; proofs
(def rules (load-rules "resources/rules/natdec.clj"))

(pretty-printer
  (-> (build-proof '[P INFER (¬ (¬ P))])
    (proof-step-backward (get-rule rules "not-i")  2 3)
    (proof-step-foreward (get-rule rules "not-e1")  1 4 5)
    ))

(pretty-printer
  (-> (build-proof '[(¬ (¬ P)) INFER P])
    (proof-step-backward (get-rule rules "raa")  2 3)
    (proof-step-foreward (get-rule rules "not-e2") 1 4 5)
    ))

(pretty-printer
  (-> (build-proof '[(P → Q) (¬ Q) INFER (¬ P)])
    (proof-step-backward (get-rule rules "not-i") 3 4)
    (proof-step-foreward (get-rule rules "impl-e2") 1 5 6)
    (proof-step-foreward (get-rule rules "not-e2") 2 8 6)
    ))

(pretty-printer
  (-> (build-proof '[INFER (P ∨ (¬ P))])
    (proof-step-backward (get-rule rules "raa") 1 2)
    (proof-step-backward (get-rule rules "not-e2") 3 4 5)
    (proof-step-backward (get-rule rules "or-i2") 4 6)
    (proof-step-backward (get-rule rules "not-i") 4 7)
    (proof-step-foreward (get-rule rules "or-i1") 8 9)
    (unify 12 'new11 '(¬ P))
    (proof-step-foreward (get-rule rules "not-e2") 3 12 9)
    ))

(pretty-printer
  (-> (build-proof '[a INFER (a ∨ b)])
    (proof-step-foreward (get-rule rules "or-i1") 1 2)
    (unify 5 'new4 'b)
    ))