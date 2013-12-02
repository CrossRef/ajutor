(ns ajutor.doi
  (:use ajutor.util)
)

(def dx-doi-url "http://dx.doi.org/")

; Convert DOIs in URL format into canonical format (not a URL).
(def normalise-doi (partial remove-leading dx-doi-url))

