{:fix-elements
 (∀ ∃ actual)
 
 :rules
 (; ALL
  {:name "all-i"
	 :args [$proof $all]
	 :forms [[$proof ((actual $i) ⊢ (substitution $predicate $x $i))]
	         [$all (∀ $x $predicate)]]
	 :backward true}
	
	{:name "all-e"
	 :args [$all $actual $substitute]
	 :forms [[$all (∀ $x $predicate)]
	         [$actual (actual $t)]
	         [$substitute (substitution $predicate $x $t)]]
	 :foreward true}
  ; EXISTS
  {:name "exists-i"
	 :args [$actual $substitute $exists]
	 :forms [[$actual (actual $t)]
	         [$substitute (substitution $predicate $x $t)]
	         [$exists (∃ $x $predicate)]]
	 :backward true}
	
	{:name "exists-e"
	 :args [$exists $proof $X]
	 :forms [[$exists (∃ $x $predicate)]
	         [$proof ((actual $x0) (substitution $predicate $x $x0) ⊢ $X)]]
	 :backward true})}
