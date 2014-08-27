(ns natural-deduction.core
  (require 
    [clojure.core.logic]
    [clojure.pprint :as pp]
    [clojure.walk :refer :all]))

(load "scope")
(load "apply_rule")
(load "proof")
(load "file_handling")

stop

;; single tests

(def rules (:rules (read-masterfile "resources/systemfiles/LfM.clj")))

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
                  (build-proof '[(∃ x (P(x))) INFER X])
                  (get-rule rules "exists-e")
                  1 2 3))

(pretty-printer (proof-step-backward
                  (build-proof '[(actual i) INFER (∃ x (P(x)))])
                  (get-rule rules "exists-i")
                  1 2 3))

(pretty-printer (proof-step-backward
                  (build-proof '[(actual i) (P (i)) INFER (∃ x (P(x)))])
                  (get-rule rules "exists-i")
                  1 3 4))

; proofs
(def master-file (read-proofed-theorems "resources/systemfiles/LfM_proofed_theorems.clj" (read-masterfile "resources/systemfiles/LfM.clj")))
(def rules (:rules master-file))
(def theorems (:theorems master-file))
(def operators (:operators master-file))

; propositional calculus

; https://homepages.thm.de/~hg11260/mat/lfm-al-e.pdf

; 16.a
(pretty-printer
  (-> (build-proof '[P INFER (¬ (¬ P))])
    (proof-step-backward (get-rule rules "not-i")  2 3)
    (proof-step-foreward (get-rule rules "not-e1")  1 2 3)
    ))

; 16.b
(pretty-printer
  (-> (build-proof '[(¬ (¬ P)) INFER P])
    (proof-step-backward (get-rule rules "raa")  2 3)
    (proof-step-foreward (get-rule rules "not-e2") 1 2 3)
    ))

; 16.c
(pretty-printer
  (-> (build-proof '[(P → Q) (¬ Q) INFER (¬ P)])
    (proof-step-backward (get-rule rules "not-i") 3 4)
    (proof-step-foreward (get-rule rules "impl-e2") 1 3 4)
    (proof-step-foreward (get-rule rules "not-e2") 2 4 5)
    ))

; 16.d
(pretty-printer
  (-> (build-proof '[INFER (P ∨ (¬ P))])
    (proof-step-backward (get-rule rules "raa") 1 2)
    (proof-step-backward (get-rule rules "not-e2") 1 2 3)
    (proof-step-backward (get-rule rules "or-i2") 2 3)
    (proof-step-backward (get-rule rules "not-i") 2 3)
    (proof-step-foreward (get-rule rules "or-i1") 2 3)
    (unify 3 'new11 '(¬ P))
    (proof-step-foreward (get-rule rules "not-e2") 1 3 4)
    ))

; 16.e
(pretty-printer
  (-> (build-proof '[(P → Q) INFER ((¬ Q) → (¬ P))])
    (proof-step-backward (get-rule rules "impl-i") 2 3)
    (proof-step-backward (get-rule rules "not-i") 3 4)
    (proof-step-foreward (get-rule rules "impl-e2") 1 3 4)
    (proof-step-foreward (get-rule rules "not-e2") 2 4 5)
    ))

; 16.f
(pretty-printer
  (-> (build-proof '[(P → Q) ((¬ P) → Q) INFER Q])
    (proof-step-foreward (reform-proofed-theorem (get-theorem theorems "16.d") operators) 3)
    (unify 3 'new6 'P)
    (proof-step-foreward (get-rule rules "or-e") 3 4 5)
    (proof-step-foreward (get-rule rules "impl-e2") 1 4 5)
    (proof-step-foreward (get-rule rules "impl-e2") 2 6 7)
    ))

; 17.a
(pretty-printer
  (-> (build-proof '[((P ∧ Q) ∧ R) (S ∧ T) INFER (Q ∧ S)])
    (proof-step-foreward (get-rule rules "and-e1") 1 3)
    (proof-step-foreward (get-rule rules "and-e1") 2 4)
    (proof-step-foreward (get-rule rules "and-e2") 3 5)
    (proof-step-foreward (get-rule rules "and-i2") 4 5 6)
    ))

; 17.b
(pretty-printer
  (-> (build-proof '[(P ∧ Q) INFER (Q ∧ P)])
    (proof-step-foreward (get-rule rules "and-e1") 1 2)
    (proof-step-foreward (get-rule rules "and-e2") 1 3)
    (proof-step-foreward (get-rule rules "and-i2") 2 3 4)
    ))

; 17.c
(pretty-printer
  (-> (build-proof '[((P ∧ Q) ∧ R) INFER (P ∧ (Q ∧ R))])
    (proof-step-foreward (get-rule rules "and-e1") 1 2)
    (proof-step-foreward (get-rule rules "and-e1") 2 3)
    (proof-step-foreward (get-rule rules "and-e2") 1 4)
    (proof-step-foreward (get-rule rules "and-e2") 2 5)
    (proof-step-foreward (get-rule rules "and-i2") 4 5 6)
    (proof-step-foreward (get-rule rules "and-i1") 3 6 7)
    ))

; 17.d
(pretty-printer
  (-> (build-proof '[(P → (P → Q)) P INFER Q])
    (proof-step-foreward (get-rule rules "impl-e2") 1 2 3)
    (proof-step-foreward (get-rule rules "impl-e1") 2 3 4)
    ))

; 17.e
(pretty-printer
  (-> (build-proof '[(Q → (P → R)) (¬ R) Q INFER (¬ P)])
    (proof-step-foreward (get-rule rules "impl-e2") 1 3 4)
    (proof-step-backward (get-rule rules "not-i") 5 6)
    (proof-step-foreward (get-rule rules "impl-e2") 4 5 6)
    (proof-step-foreward (get-rule rules "not-e2") 2 6 7)
    ))

; 17.f
(pretty-printer
  (-> (build-proof '[INFER ((P ∧ Q) → P)])
    (proof-step-backward (get-rule rules "impl-i") 1 2)
    (proof-step-foreward (get-rule rules "and-e1") 1 2)
    ))

; 17.g
(pretty-printer
  (-> (build-proof '[P INFER ((P → Q) → Q)])
    (proof-step-backward (get-rule rules "impl-i") 2 3)
    (proof-step-foreward (get-rule rules "impl-e1") 1 2 3)
    ))

; 17.h
(pretty-printer
  (-> (build-proof '[((P → R) ∧ (Q → R)) INFER ((P ∧ Q) → R)])
    (proof-step-backward (get-rule rules "impl-i") 2 3)
    (proof-step-foreward (get-rule rules "and-e1") 1 3)
    (proof-step-foreward (get-rule rules "and-e1") 2 4)
    (proof-step-foreward (get-rule rules "impl-e2") 3 4 5)
    ))

; 17.i
(pretty-printer
  (-> (build-proof '[(Q → R) INFER ((P → Q) → (P → R))])
    (proof-step-backward (get-rule rules "impl-i") 2 3)
    (proof-step-backward (get-rule rules "impl-i") 3 4)
    (proof-step-foreward (get-rule rules "impl-e2") 2 3 4)
    (proof-step-foreward (get-rule rules "impl-e2") 1 4 5)
    ))

; predicate logic

(pretty-printer
  (-> (build-proof '[((∀ x (P(x))) ∧ (∀ x (Q(x)))) INFER (∀ x ((P(x)) ∧ (Q(x))))])
    (proof-step-foreward (get-rule rules "and-e1") 1 2)
    (proof-step-foreward (get-rule rules "and-e2") 1 3)
    (proof-step-backward (get-rule rules "all-i") 4 5)
    (proof-step-foreward (get-rule rules "all-e") 2 4 5)
    (proof-step-foreward (get-rule rules "all-e") 3 4 6)
    (proof-step-foreward (get-rule rules "and-i1") 5 6 7)
    ))