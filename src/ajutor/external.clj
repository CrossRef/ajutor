(ns ajutor.external
  (:use compojure.core)
  (:require [clj-http.client :as client]))

(def doi-registration-authority-service-url "http://doi.crossref.org/ra/")

(defn lookup-ra
  "Talk to the RA webservice, return status as one of :crossref :other-ra :does-not-exist :invalid :not-available"
  [doi]
  (try
      (let
        [response (client/get (str doi-registration-authority-service-url doi) {:accept :json :as :json})]
        (if (= (-> response :body first :status) "Invalid DOI")
          :invalid
          (case (-> response :body first :RA)
            "CrossRef" :crossref
            "DOI does not exist" :does-not-exist
            :other-ra
          )))
        (catch java.net.SocketException e :not-available)))
  
