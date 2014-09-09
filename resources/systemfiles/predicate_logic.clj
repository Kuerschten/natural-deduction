:operators
(∀ ∃ actual)

:rules
( ; ALL
  {:name "all-i"
	 :args [$proof $all]
	 :forms [[$proof ((actual $i) ⊢ (substitution $predicate-formula $x $i))]
	         [$all (∀ $x $predicate-formula)]]
	 :backward $proof}
	
	{:name "all-e"
	 :args [$all $actual $substitute]
	 :forms [[$all (∀ $x $predicate-formula)]
	         [$actual (actual $t)]
	         [$substitute (substitution $predicate-formula $x $t)]]
	 :foreward $substitute}
	
	; EXISTS
	{:name "exists-i"
	 :args [$actual $substitute $exists]
	 :forms [[$actual (actual $t)]
	         [$substitute (substitution $predicate-formula $x $t)]
	         [$exists (∃ $x $predicate-formula)]]
	 :backward $substitute}
	
	{:name "exists-e"
	 :args [$exists $proof $X]
	 :forms [[$exists (∃ $x $predicate-formula)]
	         [$proof ((actual $x0) (substitution $predicate-formula $x $x0) ⊢ $X)]]
	 :backward true})
