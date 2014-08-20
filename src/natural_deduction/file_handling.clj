(ns natural-deduction.core)

(defn load-rules
  "Load a file and returns a hashmap with all rules."
  [file]
  (read-string (str "#{" (slurp (clojure.string/replace file "\\" "/")) "}")))
