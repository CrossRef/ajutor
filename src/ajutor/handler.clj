(ns ajutor.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ajutor.persistence :as persistence]
            )
  
  (:use ring.middleware.json)
  (:use ring.util.response)
  
  (:use ajutor.external)
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
             :headers {"Content-Type" "text/json" "DOI-RA" "Not available"}
             }
    
             ))

(defn fetch-ra [doi]
    (fn [x] (let [status (persistence/lookup-doi (normalise-doi doi))]
          (case status          
          :crossref (response-for-status status doi)
          :other-ra (response-for-status status doi)
          :invalid (response-for-status status doi)
          :not-available (response-for-status status doi)
          
          ; The DOI does not exist. This will expire to not-found after timeout.
          :does-not-exist (response-for-status status doi)
          
          ; The DOI was not found in the database. 
          :not-found (let [status (lookup-ra-from-service doi)]
                            (persistence/update-doi doi status)
                            (response-for-status status doi))
          (response-for-status :invalid doi)
          )
      ))
  )

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

