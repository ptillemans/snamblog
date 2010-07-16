(ns blog.hash
  (:use clojure.contrib.str-utils)
  (:use clojure.contrib.def)
  (:import [java.security MessageDigest]))

(defn digest
  [code input]
  (let [md (MessageDigest/getInstance code )]
    (. md update (.getBytes (str input)))
    (str-join "" (map #(format "%02x" %) (.digest md))))
  )

(defvar sha256 (partial digest "SHA-256")
  "Generates a SHA-256 hash of the given input plaintext.")

(defn auth [password hash]
  (= (sha256 password) hash))
