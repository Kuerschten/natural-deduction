; SAME
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
{:name "impl-e"
 :args [$a $impl $b]
 :forms [[$impl ($a → $b)]]
 :foreward $b}

{:name "impl-i"
 :args [$proof $impl]
 :forms [[$proof ($a ⊢ $b)]
         [$impl ($a → $b)]]
 :backward $proof}

; NOT
{:name "not-e"
 :args [$and $contradiction]
 :forms [[$and ($a ∧ (¬ $a))]
         [$contradiction ⊥]]
 :foreward $contradiction
 :backward $and}

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
 :backward $proof}

; ALL
{:name "all-i"
 :args [$proof $all]
 :forms [[$proof ((var $i) ⊢ (substitution $all $i))]
         [$all (predicate-formula ∀ $x $predicate-formula)]]
 :backward $proof}

{:name "all-e"
 :args [$all $var $substitute]
 :forms [[$all (predicate-formula ∀ $x $predicate-formula)]
         [$var (var $t)]
         [$substitute (substitution $all $t)]]
 :foreward $substitute}

; EXISTS
{:name "exists-i"
 :args [$var $substitute $exists]
 :forms [[$var (var $t)]
         [$substitute (substitution $exists $t)]
         [$exists (predicate-formula ∃ $x $predicate-formula)]]
 :backward $substitute}

{:name "exists-e"
 :args [$exists $proof $X]
 :forms [[$exists (predicate-formula ∃ $x $predicate-formula)]
         [$proof ((var $x0) (substitution $exists $x0) ⊢ $X)]]
 :foreward $proof}
 