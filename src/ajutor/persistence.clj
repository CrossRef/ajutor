(ns ajutor.persistence
  (:require [monger.core :as mg]
            [monger.collection :as mc])
  (:import
      [com.mongodb MongoOptions ServerAddress]
      [org.bson.types ObjectId] [com.mongodb DB WriteConcern])
    (:use
      [monger.core :only [connect! connect set-db! get-db]]
      [monger.collection :only [insert find-one update]])
    (:use ajutor.util)
    (:use ajutor.doi)
  )

; Status that a DOI can have. 
(def doi-status-other-ra 1)
(def doi-status-crossref 2)
(def doi-status-does-not-exist 3)
(def doi-status-invalid 4)

; Mongo collection
(def ra-db "ology") ; legacy name, existing database
(def ra-collection "dois")

(mg/connect!)
(mg/set-db! (mg/get-db ra-db))

(mc/ensure-index ra-collection (array-map :doi 1) {:unique true })

; Index DOIs by their status.
(mc/ensure-index ra-collection {:status 1})

; For DOIs that are not recognised, remove them after 1 day.
(mc/ensure-index ra-collection {:does-not-exist-at 1} {:expireAfterSeconds 86400})

(defn update-doi 
  "Update the store with a DOI and its status, one of :crossref :other-ra :does-not-exist :invalid"
  [doi status]
  (when (not= status :not-available)
    (let [to-insert (case status
      :crossref {:doi doi :status doi-status-crossref}
      :other-ra {:doi doi :status doi-status-other-ra}
      :does-not-exist {:doi doi :status doi-status-does-not-exist :does-not-exist-at (now)}
      :invalid {:doi doi :status doi-status-invalid}
      )]
    (update ra-collection {:doi doi} to-insert :upsert true))))
  
(defn lookup-doi
  "Get the status of a DOI, one of :crossref :other-ra :does-not-exist :invalid :not-found"
  [doi]
  (let [doi-result (find-one ra-collection {:doi doi})]    
    (if (nil? doi-result)
      :not-found
      (condp = (get doi-result "status")
        doi-status-crossref :crossref
        doi-status-other-ra :other-ra
        doi-status-does-not-exist :does-not-exist
        doi-status-invalid :invalid
        :not-found ; return does-not-exist from cache will force reference to the API.
    ))))