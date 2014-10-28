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
                                 (:hypotheses masterfile)))
        theorems-file (path-conformer (apply str (interpose "/" (conj (vec (butlast (clojure.string/split file-path #"/"))) (:theorems masterfile)))))
        theorems (try
                   (read-string (slurp theorems-file))
                   (catch Exception e (if (= java.io.FileNotFoundException (class e)) nil e)))]
    {:fix-elements fix-elements
     :rules rules
     :hypotheses hypotheses
     :theorems theorems
     :theorems-file theorems-file}))

(defn read-theorems
  [file-path master-file-hash-map]
  (let [new-theorems (read-string (slurp (path-conformer file-path)))
        old-theorems (:theorems master-file-hash-map)
        theorems (clojure.set/union new-theorems old-theorems)]
    (assoc master-file-hash-map :theorems theorems)))

(defn save-theorems
  ([master-file-hash-map]
   (let [file (:theorems-file master-file-hash-map)]
     (if file
       (save-theorems file master-file-hash-map)
       (throw (NoSuchFieldException. "No theorem file in master file.")))))
  
  ([file-path master-file-hash-map]
   (let [theorems (:theorems master-file-hash-map)]
     (spit (path-conformer file-path) theorems))))
