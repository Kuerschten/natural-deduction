(ns natural-deduction.core)

(defn substitution
  "Substitutes a  predicate formula with a new variable.

   E. g. (substitution (predicate-formula all x (P(x))) 'i) => (P(i))"
  [predicate-formula new-var]
  (let [[pre _ var pred] predicate-formula]
    (when (= pre 'predicate-formula)
      (if (contains? (set (flatten pred)) new-var) ; TODO aufpassen!!! f(x)-Beispiel
        (throw (IllegalArgumentException. (str "The variable \"" new-var "\" that shall be inserted already exists in \"" pred"\".")))
        (clojure.walk/prewalk-replace {var new-var} pred)))))

(defn reform
  "Reform a rule form. (It is needed to use apply-rule-reform)
   Returns a String."
  [form quoted?]
  (cond
    (= \$ (first (str form))) (str (when quoted? "~") form)
    (coll? form) (str (when (not quoted?) "`")"(" (apply str (interpose " " (map #(reform % true) form))) ")")
    :else (str (when quoted? "~") "'" form)))

(defn apply-rule-reform
  "Reform an unreformed(!) rule form to function that uses core.logic.
   Returns a String."
  [rule]
  (let [args (:args rule)
        forms (:forms rule)
        vars (vec (set (filter
                         #(and
                            (= \$ (first (str %)))
                            (not (contains? (set args) %)))
                         (flatten forms))))]
    (str "(fn "
         args " "
         "(clojure.core.logic/fresh " vars " "
         (apply str (map #(str "(clojure.core.logic/== " (reform (first %) false) " " (reform (second %) false) ")") forms))
         "))")))

(defn apply-rule-1step
  "Use an unreformed(!) rule on terms.
   The rule can be used foreward or backward (flag foreward?).
   Terms is a collection of all terms.
   Return the result of the therms while using the rule."
  [foreward? rule terms]
  (let [movement (if foreward? (:foreward rule) (:backward rule))]
    (when (and
            movement
            (= (inc (count terms)) (count (:args rule))))
      (let [index (atom -1)
            r (apply-rule-reform rule)
            args (apply str (interpose " " (map
                                             (fn [x]
                                               (if (= x movement)
                                                 'q
                                                 (str "'" (nth terms (swap! index inc)))))
                                             (:args rule))))
            function (str "(clojure.core.logic/run 1 [q] (" r " " args "))")
            res (first (eval (read-string function)))]
        (postwalk
          (fn [x]
            (if (and (coll? x) (= (first x) 'substitution))
              (let [[_ form var] x]
                (substitution form var))
              x))
          res)))))

(defn apply-rule-foreward
  "Use an unreformed(!) rule on terms.
   Return the result of the therms while using the rule foreward."
  [rule & terms]
  (apply-rule-1step true rule terms))

(defn apply-rule-backward
  "Use an unreformed(!) rule on terms.
   Return the result of the therms while using the rule backward."
  [rule & terms]
  (apply-rule-1step false rule terms))
