(ns natural-deduction.core)

(defn- predicate?
  [elem fix-elements]
  (and
    (coll? elem)
    (symbol? (first elem))
    (not (contains? (set fix-elements) (first elem)))
    (coll? (second elem))
    (= 2 (count elem))))

(defn theorem2rule
  [theorem fix-elements]
  (let [raw-theorem (prewalk #(if (predicate? % fix-elements)
                                (symbol (str %))
                                %)
                             (:hypothesis theorem))
        proof (:proof theorem)
        bodies (set (map :body (flatten proof)))
        name (:name theorem)]
    (if (and
         proof
         (not (contains? bodies :todo)))
      
     ; theorem is proofed -> reform raw-theorem
     (let [replacement-map (zipmap (distinct (filter #(not (contains? fix-elements %)) (flatten raw-theorem))) (map #(read-string (str "$" %)) (range)))
           forms (postwalk-replace
                   replacement-map
                   (vec (map
                          vector
                          (map #(read-string (str "$" %)) (range (count replacement-map) (+ (count replacement-map) (count raw-theorem))))
                          (filter
                            #(and (not= 'INFER %) (not= '⊢ %))
                            raw-theorem)
                          )))
           args (map first forms)]
      {:name name
       :precedence (vec (butlast args))
       :consequence (last args)
       :forms (vec forms)
       :foreward (not (nil? (last (map first forms))))
       :backward (not (nil? (last (butlast (map first forms)))))
       }
      )
      
     ; not proofed theorem
     (throw (IllegalArgumentException. "Theorem is not proofed now.")))))

(defn- substitution
  "Substitutes a  predicate formula with a new variable.

   E. g. (substitution '(P(x)) 'x 'i) => (P(i))"
  [predicate-formula old-var new-var]
  (if (contains? (set (flatten predicate-formula)) new-var) ; TODO attention!!! f(x)-example
    (throw (IllegalArgumentException. (str "The variable \"" new-var "\" that shall be inserted already exists in \"" predicate-formula"\".")))
    (clojure.walk/prewalk-replace {old-var new-var} predicate-formula)))

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
             (let [[_ predicate-formula old-var new-var] x]
               (substitution predicate-formula old-var new-var))
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
