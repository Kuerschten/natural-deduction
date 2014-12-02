(ns natural-deduction.core-test)

(deftest apply-rule-foreward-test
  (let [rule '{:premise [$a]
               :consequence $or
               :forms [[$or ($a ∨ $b)]]
               :foreward $or
               :backward $a}]
    (testing "Apply Rule Foreward"
             (is (= '(p1 ∨ _0) (apply-rule-forward rule 'p1))))))

(deftest apply-rule-backward-test
  (let [rule '{:premise [$a]
               :consequence $or
               :forms [[$or ($a ∨ $b)]]
               :foreward $or
               :backward $a}]
    (testing "Apply Rule Backward"
             (is (= 'p1 (apply-rule-backward rule '(p1 ∨ p2)))))))
