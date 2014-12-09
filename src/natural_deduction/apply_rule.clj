(ns natural-deduction.core)

(defn- predicate?
  [elem terminals]
  (and
    (coll? elem)
    (symbol? (first elem))
    (not (contains? (set terminals) (first elem)))
    (coll? (second elem))
    (= 2 (count elem))))

(defn theorem2rule
  [master-file-hash-map theorem-name]
  (let [theorem (get-theorem master-file-hash-map theorem-name)
        terminals (:terminals master-file-hash-map)
        raw-theorem (prewalk #(if (predicate? % terminals)
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
     (let [replacement-map (zipmap (distinct (filter #(not (contains? terminals %)) (flatten raw-theorem))) (map #(read-string (str "$" %)) (range)))
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
       :premise (vec (butlast args))
       :consequence (last args)
       :forms (vec forms)
       :forward (not (nil? (last (map first forms))))
       :backward (not (nil? (last (butlast (map first forms)))))
       }
      )
      
     ; not proofed theorem
     (throw (IllegalArgumentException. "Theorem is not proofed now.")))))

(defn- substitution
  "Substitutes a  predicate formula with a new variable.

   E. g. (substitution '(P(x)) 'x 'i) => (P(i))"
  [predicate-formula old new]
  (if (contains? (set (flatten predicate-formula)) new) ; TODO attention!!! f(x)-example
    (throw (IllegalArgumentException. (str "The variable \"" new "\" that shall be inserted already exists in \"" predicate-formula"\".")))
    (clojure.walk/prewalk-replace {old new} predicate-formula)))

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
  (let [args (conj (:premise rule) (:consequence rule))
        forms (:forms rule)
        vars (vec (set (filter
                         #(and
                            (= \$ (first (str %)))
                            (not (contains? (set args) %)))
                         (flatten forms))))
        equals (map #(list 'clojure.core.logic/== (rewrite (first %) false) (rewrite (second %) false)) forms)]
    (list 'fn args (concat (list 'clojure.core.logic/fresh) (list vars) equals))))

(defn- apply-rule-1step
  "Use an unrewrited(!) rule on formulas.
   The rule can be used forward or backward (flag forward?).
   formulas is a collection of all formulas.
   Return the result of the therms while using the rule."
  [forward? rule formulas]
 (let [movement (if forward? (:consequence rule) (last (:premise rule)))
       args (conj (:premise rule) (:consequence rule))]
   (when (and
           movement
           (= (inc (count formulas)) (count args)))
     (let [r (apply-rule-rewrite rule)
           h1 (replace {movement 'q} args)
           new-formulas (replace (zipmap (filter #(not= 'q %) h1) (map #(list 'quote %) formulas)) h1)
           function (list 'clojure.core.logic/run 1 '[q] (concat (list r) new-formulas))
           res (first (eval function))]
       (postwalk
         (fn [x]
           (if (and (coll? x) (= (first x) 'substitution))
             (let [[_ predicate-formula old new] x]
               (substitution predicate-formula old new))
             x))
         res)))))

(defn apply-rule-forward
  "Use an unrewrited(!) rule on formulas.
   Return the result of the therms while using the rule forward."
  [rule & formulas]
  (apply-rule-1step true rule formulas))

(defn apply-rule-backward
  "Use an unrewrited(!) rule on formulas.
   Return the result of the therms while using the rule backward."
  [rule & formulas]
  (apply-rule-1step false rule formulas))
