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
    
