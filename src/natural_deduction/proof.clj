(ns natural-deduction.core)

(def counter (atom 0))

(defn- new-number
  []
  (swap! counter inc))

(defn build-proof
  "Get a nested vector as proof and transform it.

   Every elemtent becomes a hash-set with body, a hash number and a rule:
   :body is the old element
   :hash is a number to ident this element
   :rule is the rule that returns this entry (in this case rule is :premise before infer)

   To get a proof you need an infer (\"⊢\" or \"INFER\").
   An infer gets the body :todo

   E.g. [a ⊢ b] => [#{:body a, :hash 1, :rule :premise} #{:body :todo, :hash 2, :rule nil} #{:body b, :hash 2, :rule nil}]"
  [proof]
  (let [flag (atom false)]
    (reset! counter 0)
    (clojure.walk/postwalk
      (fn [x]
        (cond
          (vector? x) x
          (list? x) {:body (apply list (map :body x)) :hash (new-number) :rule (:rule (first x))}
          :else {:body (if (or (= x '⊢) (= x 'INFER)) (do (reset! flag true) :todo) x)
                 :hash (new-number)
                 :rule (when (not @flag) :premise)}
          ))
      proof)))

(defn build-pretty-string
  "Get a transformed proof element and return a pretty String of this element.
   A body with :todo becomes a \"...\"

   E.g.  {:body a, :hash 1, :rule :premise} => \"a (#1 premise)\""
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
    (doseq [elem proof]
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
          (println (build-pretty-string elem)))))))

(defn load-rules
  "Load a file and returns a hashmap with all rules."
  [file]
  (read-string (str "#{" (slurp (clojure.string/replace file "\\" "/")) "}")))

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

(defn proof-step
  [rule proof foreward? & hashes]
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
            new-res (when res (prewalk-replace (zipmap news (map (fn [_] (gensym "P")) news)) res))]
        (when res (cond
                    ; a ... -> a b ...
                    ; TODO immer?
                    ; mir fällt grad kein plausiebles gegenbeispiel ein...
                    (= todo (last elems)) 
                    (let [b  {:body new-res
                              :hash (new-number)
                              :rule (cons (symbol rule) (map #(symbol (str "#" %)) (butlast hashes)))}]
                      b)
                
                    ; ... a -> ... b a ;
                    ; TODO immer?
                    ; ... X -> [(not X) ... (contradiction)] X
                    (= todo (first elems))
                    (let [b {:body new-res
                                    :hash (new-number)
                                    :rule nil}
                          old-a (last elems)
                          a (assoc old-a :rule (list (symbol rule)  (symbol (str "#" (:hash b)))))]
                      (list b a))
                
                    ; a ... b -> a c b
                    :else :acb
    ))))))
