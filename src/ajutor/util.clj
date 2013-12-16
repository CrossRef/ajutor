(ns ajutor.util)

(defn remove-leading [prefix string]
  (if (.startsWith string prefix)
    (apply str (drop (count prefix) string))
    string ))

(defn add-leading [prefix string]
  (if (.startsWith string prefix)
    string
    (str prefix string)))

(defn now [] (java.util.Date.))