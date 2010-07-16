(ns blog.test.blog
  (:use clojure.contrib.test-is)
  (:use blog.blog ))

(deftest mark-down-to-html-simple []
  (is (= "<p>hello</p>" (markdown-to-html "hello"))))
  
(deftest mark-down-to-html-markup []
  (is (= "<p><em>hello</em></p>" (markdown-to-html "*hello*"))))
  
(deftest mark-down-to-html-non-string []
  (is (= "<p>No text present.</p>" (markdown-to-html '()))))

