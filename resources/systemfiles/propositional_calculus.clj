{:fix-elements
 (∧ ∨ → ¬ ⊥)

 :rules
 (; SAME
	{:name "same"
	 :precedence [$a]
	 :consequence $b
	 :forms [[$a $b]]
	 :forward true
	 :backward true}
	
  ; AND
	{:name "and-e-left"
	 :precedence [$and]
	 :consequence $a
	 :forms [[$and ($a ∧ $b)]]
	 :forward true}
	
	{:name "and-e-right"
	 :precedence [$and]
	 :consequence $b
	 :forms [[$and ($a ∧ $b)]]
	 :forward true}
	
	{:name "and-i"
	 :precedence [$a $b]
	 :consequence $and
	 :forms [[$and ($a ∧ $b)]]
	 :forward true}
	
	{:name "and-i-backward"
	 :precedence [$ab]
	 :consequence $and
	 :forms [[$and ($a ∧ $b)]
           [$ab (multiple-introductions $a $b)]]
	 :backward true}
	
  ; OR
	{:name "or-e"
	 :precedence [$or $proofs]
	 :consequence $X
	 :forms [[$or ($a ∨ $b)]
	         [$proofs (multiple-introductions ($a ⊢ $X)($b ⊢ $X))]]
	 :backward true}
	
	{:name "or-i-left"
	 :precedence [$a]
	 :consequence $or
	 :forms [[$or ($a ∨ $b)]]
	 :forward true
	 :backward true}
	
	{:name "or-i-right"
	 :precedence [$b]
	 :consequence $or
	 :forms [[$or ($a ∨ $b)]]
	 :forward true
	 :backward true}
	
  ; IMPL
	{:name "impl-e"
	 :precedence [$a $impl]
	 :consequence $b
	 :forms [[$impl ($a → $b)]]
	 :forward true}
	
	{:name "impl-i"
	 :precedence [$proof]
	 :consequence $impl
	 :forms [[$proof ($a ⊢ $b)]
	         [$impl ($a → $b)]]
	 :backward true}
	
  ; NOT
	{:name "not-e"
	 :precedence [$a $not]
	 :consequence $contradiction
	 :forms [[$not (¬ $a)]
	         [$contradiction ⊥]]
	 :forward true
	 :backward true}
	
	{:name "not-i"
	 :precedence [$proof]
	 :consequence $res
	 :forms [[$proof ($a ⊢ ⊥)]
	         [$res (¬ $a)]]
	 :backward true}
	
  ; RAA, ⊥
	{:name "efq"
	 :precedence [$contradiction]
	 :consequence $a
	 :forms [[$contradiction ⊥]]
	 :forward true
	 :backward true}
	
	{:name "raa"
	 :precedence [$proof]
	 :consequence $a
	 :forms [[$proof ((¬ $a) ⊢ ⊥)]]
	 :backward true})}
