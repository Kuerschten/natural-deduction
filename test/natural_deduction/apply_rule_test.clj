(ns natural-deduction.core-test)

#_(deftest substitution-test
  (testing "Substitution"
           (is (= '(P(i)) (substitution '(predicate-formula all x (P(x))) 'i)))
           (is (= '(P(i y)) (substitution '(predicate-formula all x (P(x y))) 'i)))
           (is (= '(and a b) (substitution '(predicate-formula all x (and a b)) 'i)))
           (is (= '(and i i) (substitution '(predicate-formula all x (and x x)) 'i)))
           (is (thrown? IllegalArgumentException (substitution '(predicate-formula all x (P(x))) 'x)))))

#_(deftest reform-test
  (let [rule '{:args [$a $or]
               :forms [[$or ($a ∨ $b)]]
               :foreward $or
               :backward $a}]
    (testing "Reform"
             (is (= (read-string "`((~':args (~$a ~$or)) (~':forms ((~$or (~$a ~'∨ ~$b)))) (~':foreward ~$or) (~':backward ~$a))") (reform rule false))))))

#_(deftest apply-rule-reform-test
  (let [rule '{:args [$a $or]
               :forms [[$or ($a ∨ $b)]]
               :foreward $or
               :backward $a}]
    (testing "Apply Rule"
             (is (= (read-string "(fn [$a $or] (clojure.core.logic/fresh [$b] (clojure.core.logic/== $or `(~$a ~'∨ ~$b))))") (apply-rule-reform rule))))))

#_(deftest apply-rule-1step-test
  (let [rule '{:args [$a $or]
               :forms [[$or ($a ∨ $b)]]
               :foreward $or
               :backward $a}]
    (testing "Apply Rule 1 Step - foreward"
             (is (= '(p1 ∨ _0) (apply-rule-1step true rule '(p1)))))
    
    (testing "Apply Rule 1 Step - foreward"
             (is (= 'p1 (apply-rule-1step false rule '((p1 ∨ p2))))))))

(deftest apply-rule-foreward-test
  (let [rule '{:args [$a $or]
               :forms [[$or ($a ∨ $b)]]
               :foreward $or
               :backward $a}]
    (testing "Apply Rule Foreward"
             (is (= '(p1 ∨ _0) (apply-rule-foreward rule 'p1))))))

(deftest apply-rule-backward-test
  (let [rule '{:args [$a $or]
               :forms [[$or ($a ∨ $b)]]
               :foreward $or
               :backward $a}]
    (testing "Apply Rule Backward"
             (is (= 'p1 (apply-rule-backward rule '(p1 ∨ p2)))))))
