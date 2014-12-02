(ns natural-deduction.core-test)

(deftest build-proof-test
  (let [a {:body 'a, :hash 1, :rule :premise}
        b {:body 'b, :hash 2, :rule :premise}
        c {:body 'c, :hash 3, :rule nil}]
    
    (testing "Build Proof"
             (is (= [a] (build-proof '[a])))
             (is (= [a b] (build-proof '[a b])))
             (is (= [a [b]] (build-proof '[a [b]])))
             (is (= [a {:body :todo, :hash 2, :rule nil} c] (build-proof '[a ⊢ c]))))))
             