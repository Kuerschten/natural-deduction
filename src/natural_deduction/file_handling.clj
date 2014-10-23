(ns natural-deduction.core)

(defn- path-conformer
  [file-path]
  (clojure.string/replace file-path "\\" "/"))

(defn load-rules
  "Load a file and returns a hashmap with all rules."
  [file-path]
  (read-string (str "(" (slurp (path-conformer file-path)) ")")))

(defn read-masterfile
  [file-path]
  (let [masterfile (read-string (slurp (path-conformer file-path)))
        proofsystems (map
                       #(read-string (slurp (path-conformer (apply str (interpose "/" (conj (vec (butlast (clojure.string/split file-path #"/"))) %))))))
                       (:proofsystems masterfile))
        fix-elements (set (conj (apply concat (map :fix-elements proofsystems)) 'substitution '⊢ 'INFER))
        rules (apply list (distinct (apply concat (map :rules proofsystems))))
        hypotheses (apply concat (map
                                 #(read-string (slurp (path-conformer (apply str (interpose "/" (conj (vec (butlast (clojure.string/split file-path #"/"))) %))))))
                                 (:hypotheses masterfile)))]
    {:fix-elements fix-elements
     :rules rules
     :hypotheses hypotheses}))

(defn read-theorems
  [file-path master-file-hash-map]
  (let [proofed-theorems (apply list (read-string (slurp (path-conformer file-path))))
        replace-map (zipmap (map #(dissoc % :proof) proofed-theorems) proofed-theorems)
        new-theorems (postwalk-replace replace-map (:hypotheses master-file-hash-map))]
    (assoc master-file-hash-map :hypotheses new-theorems)))

(defn save-theorems
  [file-path theorems]
  (let [proofed-theorems-str (with-out-str (pp/pprint (filter :proof theorems)))]
    (spit (path-conformer file-path) proofed-theorems-str)))
