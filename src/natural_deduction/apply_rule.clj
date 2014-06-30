(ns natural-deduction.core)

(defn- substitution
  "Substitutes a  predicate formula with a new variable.

   E. g. (substitution (predicate-formula all x (P(x))) 'i) => (P(i))"
  [predicate-formula new-var]
  (let [[pre _ var pred] predicate-formula]
    (when (= pre 'predicate-formula)
      (if (contains? (set (flatten pred)) new-var) ; TODO aufpassen!!! f(x)-Beispiel
        (throw (IllegalArgumentException. (str "The variable \"" new-var "\" that shall be inserted already exists in \"" pred"\".")))
        (clojure.walk/prewalk-replace {var new-var} pred)))))

(defn- reform
  "Reform a rule form. (It is needed to use apply-rule-reform)
   Returns a String."
  [form quoted?]
  (let [reformed-form (when (coll? form) (map #(reform % true) form))]
    (cond
      (= \$ (first (str form))) (if quoted? (list 'list form) form)
      (coll? form) (if quoted? (list 'list (list 'seq (concat '(concat) reformed-form))) (list 'seq (concat '(concat) (map #(reform % true) form))))
      :else (if quoted? (list 'list (list 'quote form)) (list 'quote form)))))

(defn- apply-rule-reform
  "Reform an unreformed(!) rule form to function that uses core.logic.
   Returns a String."
  [rule]
  (let [args (:args rule)
        forms (:forms rule)
        vars (vec (set (filter
                         #(and
                            (= \$ (first (str %)))
                            (not (contains? (set args) %)))
                         (flatten forms))))
        equals (map #(list 'clojure.core.logic/== (reform (first %) false) (reform (second %) false)) forms)]
    (list 'fn args (concat (list 'clojure.core.logic/fresh) (list vars) equals))))

(defn- apply-rule-1step
  "Use an unreformed(!) rule on terms.
   The rule can be used foreward or backward (flag foreward?).
   Terms is a collection of all terms.
   Return the result of the therms while using the rule."
  [foreward? rule terms]
 (let [movement (if foreward? (:foreward rule) (:backward rule))]
   (when (and
           movement
           (= (inc (count terms)) (count (:args rule))))
     (let [r (apply-rule-reform rule)
           h1 (replace {movement 'q} (:args rule))
           args (replace (zipmap (filter #(not= 'q %) h1) (map #(list 'quote %) terms)) h1)
           function (list 'clojure.core.logic/run 1 '[q] (concat (list r) args))
           res (first (eval function))]
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
