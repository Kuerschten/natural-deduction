(ns natural-deduction.core)

(defn scope-from
  "Takes a proof and an element in this proof.
   The proof is a nested vector like [a [b c] d].
   The result is a set of the scope of the element in this proof.
   e.g. (scope-from [a [b c] d] a) => #{a d}"
  ([proof elem]
  (scope-from proof [] elem false))
  
  ([proof scope elem inner?]
  (let [new-scope (if inner?
                    scope
                    (conj scope proof))
        f (first proof)
        n (next proof)]
    (if f
      (if (vector? f)
        (let [s (scope-from (vec f) new-scope elem false)]
          (if s
            s
            (if n
              (scope-from (vec n) new-scope elem true)
              nil)))
        (if (= f elem)
          (set (flatten (map #(filter (complement vector?) %) new-scope)))
          (scope-from (vec n) new-scope elem true)))
    nil))))

(defn inner-proof
  "Takes a proof and an element in this proof.
   The proof is a nested vector like [a [b c] d].
   The result is a the inner vector of this proof that directly contains the element.
   e.g. (inner-proof e [a [b [c d] e] f]) => [b [c d] e]"
  [elem proof]
  (when (coll? proof)
    (if (contains? (set proof) elem)
      proof
      (first (filter (complement nil?) (map (partial inner-proof elem) proof))))))
