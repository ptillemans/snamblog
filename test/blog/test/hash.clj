(ns blog.test.hash
  (:use clojure.test)
  (:use blog.hash))

; this constant was created using the sha256sum linux commands
(def SHA256 "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8")

(deftest sha256-hash-deals-with-non-strings
  (is (= (sha256 "1") (sha256 1))))

(deftest sha-hash-with-string
  (is (= SHA256 (sha256 "password"))))

(deftest authenticate-user []
	 (let [password "password"
	       hash     SHA256]
	   (is (auth password hash)))) 