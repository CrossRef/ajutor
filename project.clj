(defproject ajutor "0.1.0-SNAPSHOT"
  :description "Helpers in Clojure for CrossRef things"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.6"]
                 [clj-http "0.7.7"]
                 [com.novemberain/monger "1.5.0"]
                 [ring/ring-json "0.2.0"]]
  :plugins [[lein-ring "0.8.8"]]
  :ring {:handler ajutor.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
