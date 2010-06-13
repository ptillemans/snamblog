(ns blog.test.hash
  (:use clojure.test)
  (:use blog.hash))

; these constants were created using the md5sum and sha256sum linux commands
(def MD5    "5f4dcc3b5aa765d61d8327deb882cf99")
(def SHA256 "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8") 

(deftest md5-hash-with-string
  (is (= MD5 (md5 "password"))))

(deftest md5-hash-deals-with-non-strings
  (is (= (md5 "1") (md5 1))))

(deftest sha-hash-with-string
  (is (= SHA256 (sha256 "password"))))