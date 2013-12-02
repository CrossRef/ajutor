(ns ajutor.util)

(defn remove-leading [prefix string]
  (if (.startsWith string prefix)
    (apply str (drop (count prefix) string))
    string ))


(defn now [] (java.util.Date.))


