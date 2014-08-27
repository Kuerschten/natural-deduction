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
  (let [masterfile (read-string (str "{" (slurp (path-conformer file-path)) "}"))
        proofsystems (map
                       #(read-string (str "{" (slurp (path-conformer (apply str (interpose "/" (conj (vec (butlast (clojure.string/split file-path #"/"))) %))))) "}"))
                       (:proofsystems masterfile))
        operators (set (conj (apply concat (map :operators proofsystems)) 'substitution '⊢ 'INFER))
        rules (apply list (distinct (apply concat (map :rules proofsystems))))
        theorems (apply concat (map
                                 #(read-string (str "(" (slurp (path-conformer (apply str (interpose "/" (conj (vec (butlast (clojure.string/split file-path #"/"))) %))))) ")"))
                                 (:theorems masterfile)))]
    {:operators operators
     :rules rules
     :theorems theorems}))

(defn read-proofed-theorems
  [file-path master-file-set]
  (let [proofed-theorems (apply list (read-string (slurp (path-conformer file-path))))
        replace-map (zipmap (map #(dissoc % :proof) proofed-theorems) proofed-theorems)
        new-theorems (postwalk-replace replace-map (:theorems master-file-set))]
    (assoc master-file-set :theorems new-theorems)))

(defn save-proofed-theorems
  [file-path theorems]
  (let [proofed-theorems-str (with-out-str (pp/pprint (filter :proof theorems)))]
    (spit (path-conformer file-path) proofed-theorems-str)))
