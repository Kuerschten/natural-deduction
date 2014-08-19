(ns natural-deduction.core)

(defn- substitution
  "Substitutes a  predicate formula with a new variable.

   E. g. (substitution (predicate-formula all x (P(x))) 'i) => (P(i))"
  [predicate-formula new-var]
  (let [[_ var pred] predicate-formula]
    (if (contains? (set (flatten pred)) new-var) ; TODO aufpassen!!! f(x)-Beispiel
      (throw (IllegalArgumentException. (str "The variable \"" new-var "\" that shall be inserted already exists in \"" pred"\".")))
        (clojure.walk/prewalk-replace {var new-var} pred))))

(defn- rewrite
  "Rewrite a rule form. (It is needed to use apply-rule-rewrite)
   Returns a String."
  [form quoted?]
  (let [rewrited-form (when (coll? form) (map #(rewrite % true) form))]
    (cond
      (= \$ (first (str form))) (if quoted? (list 'list form) form)
      (coll? form) (if quoted? (list 'list (list 'seq (concat '(concat) rewrited-form))) (list 'seq (concat '(concat) (map #(rewrite % true) form))))
      :else (if quoted? (list 'list (list 'quote form)) (list 'quote form)))))

(defn- apply-rule-rewrite
  "Rewrite an unrewrited(!) rule form to function that uses core.logic.
   Returns a String."
  [rule]
  (let [args (:args rule)
        forms (:forms rule)
        vars (vec (set (filter
                         #(and
                            (= \$ (first (str %)))
                            (not (contains? (set args) %)))
                         (flatten forms))))
        equals (map #(list 'clojure.core.logic/== (rewrite (first %) false) (rewrite (second %) false)) forms)]
    (list 'fn args (concat (list 'clojure.core.logic/fresh) (list vars) equals))))

(defn- apply-rule-1step
  "Use an unrewrited(!) rule on terms.
   The rule can be used foreward or backward (flag foreward?).
   Terms is a collection of all terms.
   Return the result of the therms while using the rule."
  [foreward? rule terms]
 (let [movement (if foreward? (:foreward rule) (:backward rule))]
   (when (and
           movement
           (= (inc (count terms)) (count (:args rule))))
     (let [r (apply-rule-rewrite rule)
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
  "Use an unrewrited(!) rule on terms.
   Return the result of the therms while using the rule foreward."
  [rule & terms]
  (apply-rule-1step true rule terms))

(defn apply-rule-backward
  "Use an unrewrited(!) rule on terms.
   Return the result of the therms while using the rule backward."
  [rule & terms]
  (apply-rule-1step false rule terms))
