(ns blog.test.post
  (:use clojure.contrib.test-is)
  (:use blog.post ))

(deftest load-text-resource-showdown
  "verify if we can read the showdown javascript from the classpath."
  []
  (is (> (count (load-text-resource "showdown.js")) 1000)))

(deftest mark-down-to-html-simple
  []
  (is (= "<p>hello</p>" (markdown-to-html "hello"))))
  
(deftest mark-down-to-html-markup
  []
  (is (= "<p><em>hello</em></p>" (markdown-to-html "*hello*"))))
  
(deftest mark-down-to-html-non-string
  []
  (is (= "<p>No text present.</p>" (markdown-to-html '()))))

