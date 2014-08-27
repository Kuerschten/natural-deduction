:operators
(∧ ∨ → ¬ ⊥)

:rules
( ; SAME
	{:name "same"
	 :args [$a $b]
	 :forms [[$a $b]]
	 :foreward $b
	 :backward $a}
	
	; AND
	{:name "and-e1"
	 :args [$and $a]
	 :forms [[$and ($a ∧ $b)]]
	 :foreward $a}
	
	{:name "and-e2"
	 :args [$and $b]
	 :forms [[$and ($a ∧ $b)]]
	 :foreward $b}
	
	{:name "and-i1"
	 :args [$a $b $and]
	 :forms [[$and ($a ∧ $b)]]
	 :foreward $and}
	
	{:name "and-i2"
	 :args [$a $b $and]
	 :forms [[$and ($b ∧ $a)]]
	 :foreward $and}
	
	{:name "and-i1-backward"
	 :args [$a $and]
	 :forms [[$and ($a ∧ $b)]]
	 :backward $a}
	
	{:name "and-i2-backward"
	 :args [$b $and]
	 :forms [[$and ($a ∧ $b)]]
	 :backward $b}
	
	; OR
	{:name "or-e"
	 :args [$or $proofs $X]
	 :forms [[$or ($a ∨ $b)]
	         [$proofs (($a ⊢ $X)($b ⊢ $X))]]
	 :foreward $proofs}
	
	{:name "or-i1"
	 :args [$a $or]
	 :forms [[$or ($a ∨ $b)]]
	 :foreward $or
	 :backward $a}
	
	{:name "or-i2"
	 :args [$b $or]
	 :forms [[$or ($a ∨ $b)]]
	 :foreward $or
	 :backward $b}
	
	; IMPL
	{:name "impl-e1"
	 :args [$a $impl $b]
	 :forms [[$impl ($a → $b)]]
	 :foreward $b}
	
	{:name "impl-e2"
	 :args [$impl $a $b]
	 :forms [[$impl ($a → $b)]]
	 :foreward $b}
	
	{:name "impl-i"
	 :args [$proof $impl]
	 :forms [[$proof ($a ⊢ $b)]
	         [$impl ($a → $b)]]
	 :backward $proof}
	
	; NOT
	{:name "not-e1"
	 :args [$a $not $contradiction]
	 :forms [[$not (¬ $a)]
	         [$contradiction ⊥]]
	 :foreward $contradiction
	 :backward $not}
	
	{:name "not-e2"
	 :args [$not $a $contradiction]
	 :forms [[$not (¬ $a)]
	         [$contradiction ⊥]]
	 :foreward $contradiction
	 :backward $a}
	
	{:name "not-i"
	 :args [$proof $res]
	 :forms [[$proof ($a ⊢ ⊥)]
	         [$res (¬ $a)]]
	 :backward $proof}
	
	; RAA, ⊥
	{:name "efq"
	 :args [$contradiction $a]
	 :forms [[$contradiction ⊥]]
	 :foreward $a
	 :backward $contradiction}
	
	{:name "raa"
	 :args [$proof $a]
	 :forms [[$proof ((¬ $a) ⊢ ⊥)]]
	 :backward $proof})
