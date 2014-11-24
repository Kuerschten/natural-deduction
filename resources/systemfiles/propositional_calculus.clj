{:terminals
 (∧ ∨ → ¬ ⊥)

 :rules
 (; SAME
	{:name "same"
	 :premise [$a]
	 :consequence $b
	 :forms [[$a $b]]
	 :forward true
	 :backward true}
	
  ; AND
	{:name "and-e-left"
	 :premise [$and]
	 :consequence $a
	 :forms [[$and ($a ∧ $b)]]
	 :forward true}
	
	{:name "and-e-right"
	 :premise [$and]
	 :consequence $b
	 :forms [[$and ($a ∧ $b)]]
	 :forward true}
	
	{:name "and-i"
	 :premise [$a $b]
	 :consequence $and
	 :forms [[$and ($a ∧ $b)]]
	 :forward true}
	
	{:name "and-i-backward"
	 :premise [$ab]
	 :consequence $and
	 :forms [[$and ($a ∧ $b)]
           [$ab (multiple-introductions $a $b)]]
	 :backward true}
	
  ; OR
	{:name "or-e"
	 :premise [$or $proofs]
	 :consequence $X
	 :forms [[$or ($a ∨ $b)]
	         [$proofs (multiple-introductions ($a ⊢ $X)($b ⊢ $X))]]
	 :backward true}
	
	{:name "or-i-left"
	 :premise [$a]
	 :consequence $or
	 :forms [[$or ($a ∨ $b)]]
	 :forward true
	 :backward true}
	
	{:name "or-i-right"
	 :premise [$b]
	 :consequence $or
	 :forms [[$or ($a ∨ $b)]]
	 :forward true
	 :backward true}
	
  ; IMPL
	{:name "impl-e"
	 :premise [$a $impl]
	 :consequence $b
	 :forms [[$impl ($a → $b)]]
	 :forward true}
	
	{:name "impl-i"
	 :premise [$proof]
	 :consequence $impl
	 :forms [[$proof ($a ⊢ $b)]
	         [$impl ($a → $b)]]
	 :backward true}
	
  ; NOT
	{:name "not-e"
	 :premise [$a $not]
	 :consequence $contradiction
	 :forms [[$not (¬ $a)]
	         [$contradiction ⊥]]
	 :forward true
	 :backward true}
	
	{:name "not-i"
	 :premise [$proof]
	 :consequence $res
	 :forms [[$proof ($a ⊢ ⊥)]
	         [$res (¬ $a)]]
	 :backward true}
	
  ; RAA, ⊥
	{:name "efq"
	 :premise [$contradiction]
	 :consequence $a
	 :forms [[$contradiction ⊥]]
	 :forward true
	 :backward true}
	
	{:name "raa"
	 :premise [$proof]
	 :consequence $a
	 :forms [[$proof ((¬ $a) ⊢ ⊥)]]
	 :backward true})}
