(ns natural-deduction.core)

(def counter (atom 0))

(defn- new-number
  []
  (swap! counter inc))

(defn- build-subproof
  ([proof]
  (build-subproof proof :assumption))
  
  ([proof rule]
  (let [nr (inc @counter)
        flag (atom false)
        pre  (clojure.walk/postwalk
               (fn [x]
                 (cond
                   (vector? x) x
                   (seq? x) {:body (apply list (map :body x)) :hash (new-number) :rule (:rule (first x))}
                   :else {:body (if (or (= x '⊢) (= x 'INFER)) (do (reset! flag true) :todo) x)
                          :hash (new-number)
                          :rule (when (not @flag) rule)}))
               proof)
        p (postwalk-replace (apply hash-map (flatten (map
                                                       #(list %1 (assoc %1 :hash (+ %2 nr)))
                                                       (flatten pre) (range))))
                            pre)]
    (reset! counter (:hash (last (flatten p))))
    p)))
    

(defn build-proof
  "Get a nested vector as proof and transform it.

   Every elemtent becomes a hash-set with body, a hash number and a rule:
   :body is the old element
   :hash is a number to ident this element
   :rule is the rule that returns this entry (in this case rule is :premise before infer)

   To get a proof you need a proof obligation (\"⊢\" or \"INFER\").
   An proof obligation gets the body :todo

   E.g. [a ⊢ b] => [#{:body a, :hash 1, :rule :premise} #{:body :todo, :hash 2, :rule nil} #{:body b, :hash 3, :rule nil}]"
  [proof]
  {:pre [(vector? proof)]}
  (do
    (reset! counter 0)
    (build-subproof proof :premise)))

(defn- get-line
  [proof elem]
  (inc (.indexOf (flatten proof) elem)))

(defn- build-pretty-string
  "Get a transformed proof element and return a pretty String of this element.
   A body with :todo becomes a \"...\"

   E.g.  {:body a, :hash 1, :rule :premise} => \"a (#1 :premise)\""
  [elem]
  (let [r (:rule elem)]
    (str (if (= (:body elem) :todo)
           "..."
           (:body elem))
         "\t(#"
         (:hash elem)
         (when r (str "\t" r))
         ")")))

(defn pretty-printer
  "Gets a transformed proof and print it on the stdout.
   Returns nil."
  ([proof]
    (pretty-printer proof 0))
  
  ([proof lvl]
    (let [p (postwalk #(if (and (seq? %) (not (list? %))) (apply list %) %) proof)]
      (doseq [elem p]
        (if (vector? elem)
          (do
	          (dotimes [_ lvl] (print "| "))
	          (dotimes [_ (- 40 lvl)] (print "--"))
	          (println)
	          (pretty-printer elem (inc lvl))
	          (dotimes [_ lvl] (print "| "))
	          (dotimes [_ (- 40 lvl)] (print "--"))
	          (println))
	        (do
	          (dotimes [_ lvl] (print "| "))
	          (println (build-pretty-string elem))))))))

(defn load-rules
  "Load a file and returns a hashmap with all rules."
  [file]
  (read-string (str "#{" (slurp (clojure.string/replace file "\\" "/")) "}")))

(defn get-rule
  [rules rule-name]
  (first (clojure.set/select #(= (:name %) rule-name) rules)))

(defn show-all-foreward-rules
  "Prints all loaded rules that runs foreward."
  [rules]
  (doseq
    [r (filter :foreward rules)]
    (println
      (str (:name r)
           "\t\targuments: " (apply str (interpose ", " (:args r)))
           "\t\tresult: " (:foreward r)))))

(defn show-all-backward-rules
  "Prints all loaded rules that runs backward."
  [rules]
  (doseq
    [r (filter :backward rules)]
    (println
      (str (:name r)
           "\t\targuments: " (apply str (interpose ", " (:args r)))
           "\t\tresult: " (:backward r)))))

(defn unify
  [proof hash old new]
  (let [old-step (first (filter #(= hash (:hash %)) (flatten proof)))
        new-step (assoc old-step :body (postwalk-replace {old new} (:body old-step)))
        new-proof (postwalk-replace {old-step new-step} proof)
        new-inner-proof (inner-proof new-step new-proof)
        new-step-index (.indexOf new-proof new-step)]
    (if (and
          (= :todo (:body (get new-inner-proof (inc new-step-index))))
          (= (:body new-step) (:body (get new-inner-proof (+ 2 new-step-index)))))
      ; (sub)proof
      (postwalk-replace
        {new-inner-proof (vec (filter #(and (not= (:hash %) (inc new-step-index)) (not= (:hash %) (+ 2 new-step-index))) new-inner-proof))}
        new-proof)
      
      ; new insertion
      new-proof
      )))

(defn- proof-step
  [proof rule foreward? hashes]
  (let [elems (flatten (filter
                         (fn [x] (some
                                   (fn [y] (= (:hash x) y))
                                   hashes))
                         (flatten proof)))
        todo (first (filter #(= (:body %) :todo) elems))
        args (map :body (filter #(not= todo %) elems))
        scope (scope-from proof todo)
        elemts-in-scope? (every? true? (map
                                         (fn [x] (some
                                                   (fn [y] (= x y))
                                                   scope))
                                         elems))
        rule-return-index (when rule (.indexOf (:args rule) (if foreward? (:foreward rule) (:backward rule))))
        todo-index (.indexOf elems todo)]
    (cond
      (not= (count hashes) (count elems)) (throw (IllegalArgumentException. "Double used or wrong hashes."))
      (not= (dec (count elems)) (count args)) (throw (IllegalArgumentException. "Wrong number of proof obligations (\"...\") is chosen. Please choose one proof obligation."))
      (not elemts-in-scope?) (throw (IllegalArgumentException. "At least one element is out of scope."))
      (not= rule-return-index todo-index) (throw (IllegalArgumentException. "Order does not fit."))
      
      :else ;; Build next proof
    (let [res (apply-rule-1step foreward? rule args)
            news (when res (filter #(re-find #"_[0-9]+" (str %)) (flatten res))) ; Elements like _0 are new elements.
          new-res (if (coll? res) (list* (when res (prewalk-replace (zipmap news (map (fn [_] (with-meta (symbol (str "new" (new-number))) {:unifiable? true})) news)) res))) res)
            todo-siblings (inner-proof todo proof)
            todo-siblings-before (subvec todo-siblings 0 (.indexOf todo-siblings todo))
            todo-siblings-after (subvec todo-siblings (inc (.indexOf todo-siblings todo)))]
        (when res (cond
                  ; a ... -> a b ...
                  ; a ... b -> a b ((interim) solution)
                  ; every time?
                  ; b has only one element?
                  ; i do not know any counterexample now...
                  (= todo (last elems))
                  (let [b  {:body new-res
                              :hash nil
                              :rule (cons (:name rule) (butlast hashes))}]
                      (if (= new-res (:body (first todo-siblings-after)))
                        ; a ... b -> a b ((interim) solution)
                     (postwalk-replace
                          {todo-siblings (vec (concat todo-siblings-before (list (assoc (first todo-siblings-after) :rule (:rule b))) (next todo-siblings-after)))}
                          proof)
                        ; a ... -> a b ...
                     (postwalk-replace
                          {todo-siblings (vec (concat todo-siblings-before (list (assoc b :hash (new-number)) todo) todo-siblings-after))}
                          proof)
                        ))
                    
                  ; ... a -> b a (sub-proof)
                  ; ... a -> ... b a
                  ; b ... a -> b a ((interim) solution)
                  (= todo (first elems))
                  (if (and (coll? new-res) (or (contains? (set new-res) '⊢) (contains? (set (new-res)) 'INFER)))
                    ; ...a -> b a (sub-proof)
                    (let [b (build-subproof (vec new-res))
                          a (assoc (first todo-siblings-after) :rule (cons (:name rule) (list (list 'between (:hash (first b)) (:hash (last b))))))]
                      (postwalk-replace
                        {todo b,
                         (first todo-siblings-after) a}
                        proof))
                     
                   ; single element
                   (let [old-a (last elems)]
                        (if (= new-res (:body (last todo-siblings-before)))
                          ; (interim) solution
                       (let [a (assoc old-a :rule (cons (:name rule) (list (:hash (last todo-siblings-before)))))]
                            (postwalk-replace
                              {todo-siblings (postwalk-replace {old-a a} (vec (concat todo-siblings-before todo-siblings-after)))}
                              proof))
                         
                       ; new insertion
                       (let [b {:body new-res
                                   :hash (new-number)
                                   :rule nil}
                                a (assoc old-a :rule (cons (:name rule) (list (:hash b))))]
                            (postwalk-replace
                              {todo-siblings (postwalk-replace {old-a a} (vec (concat todo-siblings-before (list todo b) todo-siblings-after)))}
                              proof)))))
                    
                  ; a ... b -> a c b
                  ; * one sub-proof
                  ; * multiple sub-proofs
                  ; * singel element foreward/backward (sub) solution
                  ;
                  ; a ... b -> a ... c b
                  ; * single element backward
                  ;
                  ; a ... b-> a c ... b
                  ; * single element foreward
                  :else
                  (let [proofs (count (filter #(or (= '⊢ %) (= 'INFER %)) (flatten new-res)))
                        old-b (last elems)]
                    (case proofs
                      ; insertion
                      0
                      (if foreward?
                        ; foreward insertion
                        (if (= new-res (:body (first todo-siblings-after)))
                          ; (interim) solution
                          (throw (UnsupportedOperationException. "foreward solution (inside insertion) must get implemented"))
                          
                          ; new insertion
                          (throw (UnsupportedOperationException. "foreward new insertion (inside insertion) must get implemented"))
                          )
                          
                        ; backward insertion
                        (if (= new-res (:body (last todo-siblings-before)))
                          ; (interim) solution
                          (throw (UnsupportedOperationException. "backward solution (inside insertion) must get implemented"))
                            
                          ; new insertion
                          (let [c {:body new-res
                                   :hash (new-number)
                                   :rule nil}
                                old-b (last todo-siblings)
                                new-b (assoc old-b :rule (concat (list (:name rule)) (conj (vec (butlast (butlast hashes))) (:hash c))))]
                            (postwalk-replace
                              {todo-siblings (vec (concat todo-siblings-before (list todo c) (postwalk-replace {old-b new-b} todo-siblings-after)))}
                              proof)
                            )
;                          (throw (UnsupportedOperationException. "backward new insertion (inside insertion) must get implemented"))
                      ))
                        
                      ; one proof
                      1
                      (let [sub-proof (build-subproof (vec new-res))
                            b (assoc old-b :rule (list* (concat (list (:name rule)) (subvec (vec hashes) 0 todo-index) (list (list 'between (:hash (first sub-proof)) (:hash (last sub-proof)))))))]
                        (postwalk-replace
                          {todo-siblings (vec (concat todo-siblings-before (list sub-proof) (postwalk-replace {old-b b} todo-siblings-after)))}
                          proof))
                        
                      ; multiple proofs
                      (let [sub-proofs (map #(build-subproof (vec %)) new-res)
                            b (assoc old-b :rule (list* (concat (list (:name rule)) (subvec (vec hashes) 0 todo-index) (map (fn [e] (list 'between (:hash (first e)) (:hash (last e)))) sub-proofs))))]
	                      (postwalk-replace
	                        {todo-siblings (vec (concat todo-siblings-before sub-proofs (postwalk-replace {old-b b} todo-siblings-after)))}
	                        proof)
                      )))
    ))))))

(defn proof-step-foreward
  [proof rule & hashes]
  (proof-step proof rule true hashes))

(defn proof-step-backward
  [proof rule & hashes]
  (proof-step proof rule false hashes))
