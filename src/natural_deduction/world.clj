(ns natural-deduction.core)

(def worlds (atom []))
(def counter (atom 0))
(def rules (atom nil))

(defn build-world
  "Get a nested vector as world and transform it.

   Every elemtent becomes a hash-set with body, a hash number and a rule:
   :body is the old element
   :hash is a number to ident this element
   :rule is the rule that returns this entry (in this case rule is :premise before infer)

   To get a proof you need an infer (\"⊢\" or \"INFER\").
   An infer gets the body :todo

   E.g. [a ⊢ b] => [#{:body a, :hash 1, :rule :premise} #{:body :todo, :hash 2, :rule nil} #{:body b, :hash 2, :rule nil}]"
  [world]
  (let [flag (atom false)]
    (reset! counter 0)
    (clojure.walk/postwalk
      (fn [x]
        (if (vector? x)
          x
          {:body (if (or (= x '⊢) (= x 'INFER)) (do (reset! flag true) :todo) x)
           :hash (swap! counter inc)
           :rule (when (not @flag) :premise)}
          ))
      world)))

(defn build-pretty-string
  "Get a transformed world element and return a pretty String of this element.
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
  "Gets a transformed world and print it on the stdout.
   Returns nil."
  ([world]
    (pretty-printer world 0))
  
  ([world lvl]
    (doseq [elem world]
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

(defn undo!
  "Go one step backward in the history.
   Go no step backward if the initial state is arrived."
  []
  (do
    (when (second @worlds) (reset! worlds (vec (drop-last @worlds))))
    (pretty-printer (last @worlds))))

(defn set-world!
  "Gets an nested vector and initialize the world with it.
   It is importand to give a untransformed vector!!!
   The transformation (build-world) works internal."
  [new-world]
  (do
    (reset! counter 0)
    (reset! worlds [(vec (build-world new-world))])
    (pretty-printer (last @worlds))))

(defn show-world
  []
  (pretty-printer (last @worlds)))

(defn load-rule!
  "Load a file with rules to use they in the world."
  [file]
  (reset! rules (read-string (str "#{" (slurp (clojure.string/replace file "\\" "/")) "}"))))

(defn show-all-foreward-rules
  []
  (doseq
    [r (filter :foreward @rules)]
    (println
      (str (:name r)
           "\t\targuments: " (apply str (interpose ", " (:args r)))
           "\t\tresult: " (:foreward r)))))

(defn show-all-backward-rules
  []
  (doseq
    [r (filter :backward @rules)]
    (println
      (str (:name r)
           "\t\targuments: " (apply str (interpose ", " (:args r)))
           "\t\tresult: " (:backward r)))))

(defn apply-rule!
  [rule foreward? & hashes]
  (let [elems (flatten (filter
                         (fn [x] (some
                                   (fn [y] (= (:hash x) y))
                                   hashes))
                         (flatten (last @worlds))))
        todo (first (filter #(= (:body %) :todo) elems))
        args (filter #(not= todo %) elems)
        scope (scope-from (last @worlds) todo)
        elemts-in-scope? (every? true? (map
                                         (fn [x] (some
                                                   (fn [y] (= x y))
                                                   scope))
                                         elems))
        rul (first (filter #(= rule (:name %)) @rules))
        rule-return-index (when rul (.indexOf (:args rul) (if foreward? (:foreward rul) (:backward rul))))
        todo-index (.indexOf elems todo)]
    (cond
      (not rul) (println "This rule does not exist.")
      (not= (count hashes) (count elems)) (println "Double used or wrong hashes.")
      (not= (dec (count elems)) (count args)) (println "Wrong number of \"...\" is chosen. Please choose one \"...\".")
      (not elemts-in-scope?) (println "At least one element is out of scope.")
      (not= rule-return-index todo-index) (println "Order does not fit.")
       ;TODO anwenden auf welt
      :else (let [res (apply-rule-1step foreward? rul args)
                  news (filter #(re-find #"_[0-9]+" (str %)) (flatten res)) ; Elements like _0 are new elements.
                  new-res (prewalk-replace (zipmap news (map #(symbol (str "P" %)) (range))) res)] ;TODO neue Elemente korrekt bilden.
              (when res
                new-res)
    ))))
