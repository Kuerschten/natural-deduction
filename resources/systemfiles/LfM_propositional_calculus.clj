; https://homepages.thm.de/~hg11260/mat/lfm-al-e.pdf

{:name "16.d"
 :theorem [INFER (P ∨ (¬ P))]
 :proofed [[{:body (¬ (P ∨ (¬ P))), :hash 3, :rule :assumption}
            [{:body P, :hash 8, :rule :assumption}
             {:body (P ∨ (¬ P)), :hash 12, :rule ("or-i1" 8)}
             {:body ⊥, :hash 10, :rule ("not-e2" 3 12)}]
            {:body (¬ P), :hash 7, :rule ("not-i" (between 8 10))}
            {:body (P ∨ (¬ P)), :hash 6, :rule ("or-i2" 7)}
            {:body ⊥, :hash 5, :rule ("not-e2" 3 6)}]
           {:body (P ∨ (¬ P)), :hash 2, :rule ("raa" (between 3 5))}]}

{:name "16.f"
 :theorem [(P → Q) ((¬ P) → Q) INFER Q]}