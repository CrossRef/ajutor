(ns ajutor.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ajutor.persistence :as persistence]
            [ajutor.external :as external])
  
  (:use ring.middleware.json)
  (:use ring.util.response)
  
  (:use ajutor.util)
  (:use ajutor.doi)
  
  (:require [ajutor.doi :refer :all])
)


(defn heartbeat [request]
  "heartbeat")

(defn response-for-status [status doi]  
  (case status
    :crossref {:status 200
                 :headers {"Content-Type" "text/json" "DOI-RA" "CrossRef"}
                 :body [{"DOI" doi "RA" "CrossRef"}]}
    
    :does-not-exist {:status 200
                 :headers {"Content-Type" "text/json" "DOI-RA" "Does not exist"}
                 :body [{"DOI" doi "RA" "Does not exist"}]}
    
    :other-ra {:status 200
                 :headers {"Content-Type" "text/json" "DOI-RA" "Other"}
                 :body [{"DOI" doi "RA" "Other"}]}
    
    :invalid {:status 200
                 :headers {"Content-Type" "text/json" "DOI-RA" "Invalid"}
                 :body [{"DOI" doi "status" "Invalid DOI"}]}
    
    :not-available {:status 500
             :headers {"Content-Type" "text/json" "DOI-RA" "Not available"}}))

(defn fetch-ra [doi]
    (fn [x] (let [normalised-doi (normalise-doi doi) ; format used in DB.
                  non-url-doi (non-url-doi doi) ; format used by external API.
                  status (persistence/lookup-doi normalised-doi)]
          (case status          
          :crossref (response-for-status status non-url-doi)
          :other-ra (response-for-status status non-url-doi)
          :invalid (response-for-status status non-url-doi)
          :not-available (response-for-status status non-url-doi)
          
          ; The DOI does not exist. This will expire to not-found after timeout.
          :does-not-exist (response-for-status status non-url-doi)
          
          ; The DOI was not found in the database. 
          :not-found (let [status (external/lookup-ra non-url-doi)]
                            (persistence/update-doi normalised-doi status)
                            (response-for-status status non-url-doi))
          (response-for-status :invalid non-url-doi)))))

(defn home [request]
  "DOI Registration Agency Proxy")

(defroutes app-routes
  (GET "/" [] home)
  (GET "/heartbeat" [] heartbeat)
  (GET "/ra/*" {{doi :*} :params} (wrap-json-response (fetch-ra doi)))
    
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))

