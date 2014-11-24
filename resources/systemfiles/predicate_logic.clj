{:terminals
 (∀ ∃ actual)
 
 :rules
 (; ALL
  {:name "all-i"
	 :precedence [$proof]
	 :consequence $all
	 :forms [[$proof ((actual $i) ⊢ (substitution $predicate $x $i))]
	         [$all (∀ $x $predicate)]]
	 :backward true}
	
	{:name "all-e"
	 :precedence [$all $actual]
	 :consequence $substitute
	 :forms [[$all (∀ $x $predicate)]
	         [$actual (actual $t)]
	         [$substitute (substitution $predicate $x $t)]]
	 :forward true}
  ; EXISTS
  {:name "exists-i"
	 :precedence [$actual $substitute]
	 :consequence $exists
	 :forms [[$actual (actual $t)]
	         [$substitute (substitution $predicate $x $t)]
	         [$exists (∃ $x $predicate)]]
	 :backward true}
	
	{:name "exists-e"
	 :precedence [$exists $proof]
	 :consequence $X
	 :forms [[$exists (∃ $x $predicate)]
	         [$proof ((actual $x0) (substitution $predicate $x $x0) ⊢ $X)]]
	 :backward true})}
