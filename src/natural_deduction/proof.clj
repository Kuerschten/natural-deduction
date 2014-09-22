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

(defn- hash2line
  [proof hash]
  (let [fp (vec (flatten proof))
        elem (first (filter #(= (:hash %) hash) fp))]
    (inc (.indexOf fp elem))))

(defn- line2hash
  [proof line]
  (:hash (get (vec (flatten proof)) (dec line))))

(defn- build-pretty-string
  "Get a transformed proof element and return a pretty String of this element.
   A body with :todo becomes a \"...\"

   E.g.  {:body a, :hash 1, :rule :premise} => \"a (:premise)\""
  [elem proof]
  (let [r (postwalk #(if (number? %)
                        (hash2line proof %)
                        %)
                        (:rule elem))]
    (str (if (= (:body elem) :todo)
           "..."
           (:body elem))
         (when r (str "    " r)))))

(defn pretty-printer
  "Gets a transformed proof and print it on the stdout.
   Returns nil."
  ([proof]
    (pretty-printer proof 0 proof))
  
  ([proof lvl complete-proof]
    (let [p (postwalk #(if (and (seq? %) (not (list? %))) (apply list %) %) proof)]
      (doseq [elem p]
        (if (vector? elem)
          (do
            (print "      ")
	          (dotimes [_ lvl] (print "| "))
	          (dotimes [_ (- 40 lvl)] (print "--"))
	          (println)
	          (pretty-printer elem (inc lvl) complete-proof)
            (print "      ")
	          (dotimes [_ lvl] (print "| "))
	          (dotimes [_ (- 40 lvl)] (print "--"))
	          (println))
	        (let [line (hash2line complete-proof (:hash elem))]
            (print (pp/cl-format nil "~4d: " line))
	          (dotimes [_ lvl] (print "| "))
	          (println (build-pretty-string elem complete-proof))))))))

(defn get-rules
  [rules rule-name]
  (clojure.set/select #(= (:name %) rule-name) (set rules)))

(defn get-rule
  [rules rule-name]
  (first (get-rules rules rule-name)))

(defn get-theorem
  [theorems theorem-name]
  (first (clojure.set/select #(= (:name %) theorem-name) (set theorems))))

(defn show-all-foreward-rules
  "Prints all loaded rules that runs foreward."
  [rules]
  (pp/print-table (map #(hash-map 'name (:name %) 'arguments (:args %) 'result (:foreward %)) (filter :foreward rules))))

(defn show-all-backward-rules
  "Prints all loaded rules that runs backward."
  [rules]
  (pp/print-table (map #(hash-map 'name (:name %) 'arguments (:args %) 'result (:backward %)) (filter :backward rules))))

(defn- update-proof
  [proof line new-step]
  (let [hash (line2hash proof line)
        old-step (first (filter #(= hash (:hash %)) (flatten proof)))
        new-proof (postwalk-replace {old-step new-step} proof)
        new-inner-proof (inner-proof new-step new-proof)
        new-step-index (.indexOf new-inner-proof new-step)]
    (if (and
          (= :todo (:body (get new-inner-proof (inc new-step-index))))
          (= (:body new-step) (:body (get new-inner-proof (+ 2 new-step-index)))))
      ; (interim) solution
      (let [new-hash (:hash (get (vec new-inner-proof) (+ 2 new-step-index)))
            new-result (assoc new-step :hash new-hash)]
        (prewalk-replace
          {new-inner-proof (vec (filter #(and (not= (:hash %) (:hash (get (vec new-inner-proof) (inc new-step-index))))
                                              (not= (:hash %) (:hash (get (vec new-inner-proof) (+ 2 new-step-index)))))
                                        new-inner-proof))
           new-step new-result}
          new-proof))
      
      ; new insertion
      new-proof)))

(defn unify
  [proof line old new]
  (let [hash (line2hash proof line)
        old-step (first (filter #(= hash (:hash %)) (flatten proof)))
        new-step (assoc old-step :body (postwalk-replace {old new} (:body old-step)))]
    (update-proof proof line new-step)))

(defn choose-option
  [proof line option]
  (let [hash (line2hash proof line)
        old-step (first (filter #(= hash (:hash %)) (flatten proof)))
        new-step (assoc old-step :body (get (:body old-step) option))]
    (update-proof proof line new-step)))

(defn- proof-step
  [proof rule foreward? lines]
  (let [hashes (map #(line2hash proof %) lines)
        elems (flatten (filter
                         (fn [x] (some
                                   (fn [y] (= (:hash x) y))
                                   hashes))
                         (flatten proof)))
        todo (first (filter #(= (:body %) :todo) elems))
        args (map :body (filter #(not= todo %) elems))
        elemts-in-scope? (every? true? (map
                                         (fn [x] (some
                                                   (fn [y] (= x y))
                                                   (scope-from proof todo)))
                                         elems))
        todo-index (.indexOf elems todo)]
    (cond
      (not= (count hashes) (count elems)) (throw (IllegalArgumentException. "Double used or wrong lines."))
      (not= (dec (count elems)) (count args)) (throw (IllegalArgumentException. "Wrong number of proof obligations (\"...\") is chosen. Please choose one proof obligation."))
      (not elemts-in-scope?) (throw (IllegalArgumentException. "At least one element is out of scope."))
      
      :else ;; Build next proof
    (let [rules (map #(if  (:backward %) (assoc % :backward (last (butlast (:args %)))) %)
                     (map #(if (:foreward %) (assoc % :foreward (last (:args %))) %)
                       (let [args (:args rule)]
                                 (map #(assoc rule :args (conj (vec %) (last args)))
                                      (combo/permutations (butlast args))))))
          res (filter #(not= nil %) (map #(apply-rule-1step foreward? % args) rules))
          news (when res (filter #(re-find #"_[0-9]+" (str %)) (flatten res))) ; Elements like _0 are new elements.
          nres (if (coll? res) (list* (when res (prewalk-replace (zipmap news (map (fn [_] (with-meta (symbol (str "new" (new-number))) {:unifiable? true})) news)) res))) res)
          new-res (if (> (count nres) 1)
                    (apply hash-map (mapcat #(list (inc %1) %2) (range) nres))
                    (first nres))
            todo-siblings (inner-proof todo proof)
            todo-siblings-before (subvec todo-siblings 0 (.indexOf todo-siblings todo))
            todo-siblings-after (subvec todo-siblings (inc (.indexOf todo-siblings todo)))]
      (when new-res (cond
           (= todo (last elems))
           ; foreward
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
                          proof)))
                    
           :else
           ;backward
           (if (and (coll? new-res) (or (contains? (set new-res) '⊢) (contains? (set new-res) 'INFER)))
             ; ... a -> b a (sub-proof)
             (let [b (build-subproof (vec new-res))
                   a (assoc (first todo-siblings-after) :rule (cons (:name rule) (concat
                                                                                   (butlast (butlast hashes))
                                                                                   (list (list 'between (:hash (first b)) (:hash (last b)))))))]
               (postwalk-replace
                 {todo b,
                  (first todo-siblings-after) a}
                 proof))
             
             ; other backward
             (let [old-a (last elems)]
               (if (and (seq? new-res) (= (first new-res) 'multiple-introductions))
                 ; multiple introduction
                 (let [build-multi (fn [elem]
                                     (if (and
                                           (coll? elem)
                                           (or
                                             (contains? (set elem) 'INFER)
                                             (contains? (set elem) '⊢)))
                                       (list (build-subproof (vec elem)))
                                       (list
                                         (hash-map
                                           :body :todo
                                           :hash (new-number)
                                           :rule nil)
                                         (hash-map
                                           :body elem
                                           :hash (new-number)
                                           :rule nil))))
                       multi-b (doall (apply concat (map build-multi (rest new-res))))
                       a (assoc old-a :rule (cons (:name rule) (concat
                                                                 (butlast (butlast hashes))
                                                                 (map
                                                                   #(if (vector? %)
                                                                      (list 'between (:hash (first %)) (:hash (last %)))
                                                                      (:hash %))
                                                                   (filter #(not= :todo (:body %)) multi-b)))))]
                   (postwalk-replace
                     {todo-siblings (postwalk-replace {old-a a} (vec (concat todo-siblings-before multi-b todo-siblings-after)))}
                     proof))
                 
                 ; single element
                 (if (= new-res (:body (last todo-siblings-before)))
                   ; (interim) solution
                   (let [a (assoc old-a :rule (cons (:name rule) (concat
                                                                   (butlast (butlast hashes))
                                                                   (list (:hash (last todo-siblings-before))))))]
                     (postwalk-replace
                       {todo-siblings (postwalk-replace {old-a a} (vec (concat todo-siblings-before todo-siblings-after)))}
                       proof))
                   
                   ; new insertion
                   (let [b {:body new-res
                            :hash (new-number)
                            :rule nil}
                         a (assoc old-a :rule (cons (:name rule) (concat
                                                                   (butlast (butlast hashes))
                                                                   (list (:hash b)))))]
                     (postwalk-replace
                       {todo-siblings (postwalk-replace {old-a a} (vec (concat todo-siblings-before (list todo b) todo-siblings-after)))}
                       proof))))))))))))

(defn proof-step-foreward
  [proof rule & hashes]
  (proof-step proof rule true hashes))

(defn proof-step-backward
  [proof rule & hashes]
  (proof-step proof rule false hashes))
