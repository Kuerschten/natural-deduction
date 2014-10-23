{:operators
 (∧ ∨ → ¬ ⊥)

 :rules
 (; SAME
	{:name "same"
	 :args [$a $b]
	 :forms [[$a $b]]
	 :foreward true
	 :backward true}
	
  ; AND
	{:name "and-e-left"
	 :args [$and $a]
	 :forms [[$and ($a ∧ $b)]]
	 :foreward true}
	
	{:name "and-e-right"
	 :args [$and $b]
	 :forms [[$and ($a ∧ $b)]]
	 :foreward true}
	
	{:name "and-i"
	 :args [$a $b $and]
	 :forms [[$and ($a ∧ $b)]]
	 :foreward true}
	
	{:name "and-i-backward"
	 :args [$ab $and]
	 :forms [[$and ($a ∧ $b)]
           [$ab (multiple-introductions $a $b)]]
	 :backward true}
	
  ; OR
	{:name "or-e"
	 :args [$or $proofs $X]
	 :forms [[$or ($a ∨ $b)]
	         [$proofs (multiple-introductions ($a ⊢ $X)($b ⊢ $X))]]
	 :backward true}
	
	{:name "or-i-left"
	 :args [$a $or]
	 :forms [[$or ($a ∨ $b)]]
	 :foreward true
	 :backward true}
	
	{:name "or-i-right"
	 :args [$b $or]
	 :forms [[$or ($a ∨ $b)]]
	 :foreward true
	 :backward true}
	
  ; IMPL
	{:name "impl-e"
	 :args [$a $impl $b]
	 :forms [[$impl ($a → $b)]]
	 :foreward true}
	
	{:name "impl-i"
	 :args [$proof $impl]
	 :forms [[$proof ($a ⊢ $b)]
	         [$impl ($a → $b)]]
	 :backward true}
	
  ; NOT
	{:name "not-e"
	 :args [$a $not $contradiction]
	 :forms [[$not (¬ $a)]
	         [$contradiction ⊥]]
	 :foreward true
	 :backward true}
	
	{:name "not-i"
	 :args [$proof $res]
	 :forms [[$proof ($a ⊢ ⊥)]
	         [$res (¬ $a)]]
	 :backward true}
	
  ; RAA, ⊥
	{:name "efq"
	 :args [$contradiction $a]
	 :forms [[$contradiction ⊥]]
	 :foreward true
	 :backward true}
	
	{:name "raa"
	 :args [$proof $a]
	 :forms [[$proof ((¬ $a) ⊢ ⊥)]]
	 :backward true})}
