(ns blog.hash
  (:use clojure.contrib.str-utils)
  (:use clojure.contrib.def)
  (:import [java.security MessageDigest]))

(defn- digest [code input]
  [input]
  (let [md (MessageDigest/getInstance code )]
    (. md update (.getBytes (str input)))
    (let [digest (.digest md)]
      (str-join "" (map #(format "%02x" %) digest)))))
  

(defvar sha256 (partial digest "SHA-256")
  "Generates a SHA-256 hash of the given input plaintext.")


(defvar md5 (partial digest "MD5")
   "Generates a MD5 hash of the given input plaintext.")


