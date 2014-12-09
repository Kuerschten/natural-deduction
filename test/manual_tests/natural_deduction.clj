(ns manual-tests.natural-deduction
  (:require [natural-deduction.core :refer :all]))

;; single tests

(def master-file (read-master-file "resources/systemfiles/LfM.clj"))

stop

; forward
(pretty-printer
  (-> (build-proof '[a b INFER (b ∧ a)])
    (proof-step-forward (get-rule master-file "and-i") 1 2 3)
    (choose-option 3 2)))

(pretty-printer
  (-> (build-proof '[a b INFER c])
    (proof-step-forward (get-rule master-file "and-i") 1 2 3)
    (choose-option 3 1)))

(pretty-printer (proof-step-forward
                  (build-proof '[a (a → b) INFER b])
                  (get-rule master-file "impl-e")
                  1 2 3))

; backward
(pretty-printer (proof-step-backward
                  (build-proof '[INFER (a → b)])
                  (get-rule master-file "impl-i")
                  1 2))

(pretty-printer (proof-step-backward
                  (build-proof '[INFER (a ∧ b)])
                  (get-rule master-file "and-i-backward")
                  1 2))

(pretty-printer
  (-> (build-proof '[INFER (¬ a)])
    (proof-step-backward (get-rule master-file "not-i") 1 2)))

(pretty-printer
  (-> (build-proof '[a INFER (a ∨ b)])
    (proof-step-backward (get-rule master-file "or-i-left") 2 3)))

; inside
(pretty-printer (proof-step-backward
                  (build-proof '[(a ∨ b) INFER X])
                  (get-rule master-file "or-e")
                  1 2 3))

(pretty-printer (proof-step-backward
                  (build-proof '[(∃ x (P(x))) INFER X])
                  (get-rule master-file "exists-e")
                  1 2 3))

(pretty-printer (proof-step-backward
                  (build-proof '[(actual i) INFER (∃ x (P(x)))])
                  (get-rule master-file "exists-i")
                  1 2 3))

(pretty-printer (proof-step-backward
                  (build-proof '[(actual i) (P (i)) INFER (∃ x (P(x)))])
                  (get-rule master-file "exists-i")
                  1 3 4))

; proofs
(def master-file (read-master-file "resources/systemfiles/LfM.clj"))

; propositional calculus

; https://homepages.thm.de/~hg11260/mat/lfm-al-e.pdf

; 16.a
(pretty-printer
  (-> (build-proof '[P INFER (¬ (¬ P))])
    (proof-step-backward (get-rule master-file "not-i")  2 3)
    (proof-step-forward (get-rule master-file "not-e")  1 2 3)
    ))

; 16.b
(pretty-printer
  (-> (build-proof '[(¬ (¬ P)) INFER P])
    (proof-step-backward (get-rule master-file "raa")  2 3)
    (proof-step-forward (get-rule master-file "not-e") 1 2 3)
    ))

; 16.c
(pretty-printer
  (-> (build-proof '[(P → Q) (¬ Q) INFER (¬ P)])
    (proof-step-backward (get-rule master-file "not-i") 3 4)
    (proof-step-forward (get-rule master-file "impl-e") 1 3 4)
    (proof-step-forward (get-rule master-file "not-e") 2 4 5)
    ))

; 16.d (TND)
(pretty-printer
  (-> (build-proof '[INFER (P ∨ (¬ P))])
    (proof-step-backward (get-rule master-file "raa") 1 2)
    (proof-step-backward (get-rule master-file "not-e") 1 2 3)
    (choose-option 3 2)
    (proof-step-backward (get-rule master-file "or-i-right") 2 3)
    (proof-step-backward (get-rule master-file "not-i") 2 3)
    (proof-step-forward (get-rule master-file "or-i-left") 2 3)
    (unify 3 'new11 '(¬ P))
    (proof-step-forward (get-rule master-file "not-e") 1 3 4)
    ))

; 16.e
(pretty-printer
  (-> (build-proof '[(P → Q) INFER ((¬ Q) → (¬ P))])
    (proof-step-backward (get-rule master-file "impl-i") 2 3)
    (proof-step-backward (get-rule master-file "not-i") 3 4)
    (proof-step-forward (get-rule master-file "impl-e") 1 3 4)
    (proof-step-forward (get-rule master-file "not-e") 2 4 5)
    ))

; 16.f
(pretty-printer
  (-> (build-proof '[(P → Q) ((¬ P) → Q) INFER Q])
    (proof-step-forward (theorem2rule master-file "TND") 3)
    (unify 3 'new6 'P)
    (proof-step-backward (get-rule master-file "or-e") 3 4 5)
    (proof-step-forward (get-rule master-file "impl-e") 1 4 5)
    (proof-step-forward (get-rule master-file "impl-e") 2 6 7)
    ))

; 17.a
(pretty-printer
  (-> (build-proof '[((P ∧ Q) ∧ R) (S ∧ T) INFER (Q ∧ S)])
    (proof-step-forward (get-rule master-file "and-e-left") 1 3)
    (proof-step-forward (get-rule master-file "and-e-left") 2 4)
    (proof-step-forward (get-rule master-file "and-e-right") 3 5)
    (proof-step-forward (get-rule master-file "and-i") 4 5 6)
    (choose-option 6 2)
    ))

; 17.b
(pretty-printer
  (-> (build-proof '[(P ∧ Q) INFER (Q ∧ P)])
    (proof-step-forward (get-rule master-file "and-e-left") 1 2)
    (proof-step-forward (get-rule master-file "and-e-right") 1 3)
    (proof-step-forward (get-rule master-file "and-i") 2 3 4)
    (choose-option 4 2)
    ))

; 17.c
(pretty-printer
  (-> (build-proof '[((P ∧ Q) ∧ R) INFER (P ∧ (Q ∧ R))])
    (proof-step-forward (get-rule master-file "and-e-left") 1 2)
    (proof-step-forward (get-rule master-file "and-e-left") 2 3)
    (proof-step-forward (get-rule master-file "and-e-right") 1 4)
    (proof-step-forward (get-rule master-file "and-e-right") 2 5)
    (proof-step-forward (get-rule master-file "and-i") 4 5 6)
    (choose-option 6 2)
    (proof-step-forward (get-rule master-file "and-i") 3 6 7)
    (choose-option 7 1)
    ))

; 17.d
(pretty-printer
  (-> (build-proof '[(P → (P → Q)) P INFER Q])
    (proof-step-forward (get-rule master-file "impl-e") 1 2 3)
    (proof-step-forward (get-rule master-file "impl-e") 2 3 4)
    ))

; 17.e
(pretty-printer
  (-> (build-proof '[(Q → (P → R)) (¬ R) Q INFER (¬ P)])
    (proof-step-forward (get-rule master-file "impl-e") 1 3 4)
    (proof-step-backward (get-rule master-file "not-i") 5 6)
    (proof-step-forward (get-rule master-file "impl-e") 4 5 6)
    (proof-step-forward (get-rule master-file "not-e") 2 6 7)
    ))

; 17.f
(pretty-printer
  (-> (build-proof '[INFER ((P ∧ Q) → P)])
    (proof-step-backward (get-rule master-file "impl-i") 1 2)
    (proof-step-forward (get-rule master-file "and-e-left") 1 2)
    ))

; 17.g
(pretty-printer
  (-> (build-proof '[P INFER ((P → Q) → Q)])
    (proof-step-backward (get-rule master-file "impl-i") 2 3)
    (proof-step-forward (get-rule master-file "impl-e") 1 2 3)
    ))

; 17.h
(pretty-printer
  (-> (build-proof '[((P → R) ∧ (Q → R)) INFER ((P ∧ Q) → R)])
    (proof-step-backward (get-rule master-file "impl-i") 2 3)
    (proof-step-forward (get-rule master-file "and-e-left") 1 3)
    (proof-step-forward (get-rule master-file "and-e-left") 2 4)
    (proof-step-forward (get-rule master-file "impl-e") 3 4 5)
    ))

; 17.i
(pretty-printer
  (-> (build-proof '[(Q → R) INFER ((P → Q) → (P → R))])
    (proof-step-backward (get-rule master-file "impl-i") 2 3)
    (proof-step-backward (get-rule master-file "impl-i") 3 4)
    (proof-step-forward (get-rule master-file "impl-e") 2 3 4)
    (proof-step-forward (get-rule master-file "impl-e") 1 4 5)
    ))

; 17.j
(pretty-printer
  (-> (build-proof '[(P → Q) (R → S) INFER ((P ∨ R) → (Q ∨ S))])
    (proof-step-backward (get-rule master-file "impl-i") 3 4)
    (proof-step-backward (get-rule master-file "or-e") 3 4 5)
    (proof-step-forward (get-rule master-file "impl-e") 1 4 5)
    (proof-step-forward (get-rule master-file "or-i-left") 5 6)
    (unify 6 'new15 'S)
    (proof-step-forward (get-rule master-file "impl-e") 2 7 8)
    (proof-step-forward (get-rule master-file "or-i-right") 8 9)
    (unify 9 'new18 'Q)
    ))

; 18.a
(pretty-printer
  (-> (build-proof '[INFER (((P → Q) → P) → P)])
    (proof-step-backward (get-rule master-file "impl-i") 1 2)
    (proof-step-backward (get-rule master-file "raa") 2 3)
    (proof-step-backward (get-rule master-file "not-e") 2 3 4)
    (choose-option 4 2)
    (proof-step-backward (get-rule master-file "impl-e") 1 3 4)
    (choose-option 4 2)
    (proof-step-backward (get-rule master-file "impl-i") 3 4)
    (proof-step-forward (get-rule master-file "not-e") 2 3 4)
    (proof-step-forward (get-rule master-file "efq") 4 5)
    (unify 5 'new15 'Q)
    ))

; predicate logic

(pretty-printer
  (-> (build-proof '[((∀ x (P(x))) ∧ (∀ x (Q(x)))) INFER (∀ x ((P(x)) ∧ (Q(x))))])
    (proof-step-forward (get-rule master-file "and-e-left") 1 2)
    (proof-step-forward (get-rule master-file "and-e-right") 1 3)
    (proof-step-backward (get-rule master-file "all-i") 4 5)
    (proof-step-forward (get-rule master-file "all-e") 2 4 5)
    (proof-step-forward (get-rule master-file "all-e") 3 4 6)
    (proof-step-forward (get-rule master-file "and-i") 5 6 7)
    (choose-option 7 1)
    ))

(pretty-printer
  (-> (build-proof '[(∀ x (∀ y (P(x y)))) INFER (∀ u (∀ v (P(u v))))])
    (proof-step-backward (get-rule master-file "all-i") 2 3)
    (proof-step-backward (get-rule master-file "all-i") 3 4)
    (proof-step-forward (get-rule master-file "all-e") 1 2 4)
    (proof-step-forward (get-rule master-file "all-e") 3 4 5)
  ))