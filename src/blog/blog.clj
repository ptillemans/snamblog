(ns blog.blog
  (:import (org.mozilla.javascript Context ScriptableObject))
  (:import (java.util Date))
  (:require [clojure.contrib.str-utils2 :as s])
  (:use somnium.congomongo))

(defn load-text-resource [name]
	(apply str 
		(line-seq 
			(new java.io.BufferedReader 
				(new java.io.InputStreamReader 
					(.getResourceAsStream 
						(.getClassLoader clojure.main ) name))))))
						
(defn markdown-to-html [txt]
  (let [cx (Context/enter)
        scope (.initStandardObjects cx)
        text (or txt "No text present.")
        input (Context/javaToJS text scope)
        script (str (load-text-resource "showdown.js")
                    "new Showdown.converter().makeHtml(input);")]
    (try
     (ScriptableObject/putProperty scope "input" input)
     (let [result (.evaluateString cx scope script "<cmd>" 1 nil)]
       (Context/toString result))
     (finally (Context/exit)))))

(mongo! :db "snamblog")

(defn get-blog-info [id] 
  (or 
   (fetch-one 
    :blog-posts
    :where { :id id })
   {:title "No such article" :article "No such article\n==================\nThis space intentionally left blank\n" }))

(def debug (atom {}))

(defn update-blog [params] 
  (let [post (get-blog-info (:id params))
        previous (:_id post)
        updates (assoc params
		  :title (first (s/split-lines (:article params)))
		  :ts (Date.) 
		  :prev previous)]
    (do
      (reset! debug (atom updates))
      (if post
	(update! :blog-posts post (dissoc (merge post updates) :id)))
      (insert! :blog-posts updates))))

(defn last-blogs-summary 
  ([n]
     (fetch 
        :blog-posts 
	:where {:id {:$exists 1}} 
	:order :ts 
	:limit 5 
	:skip n
	:only [:title :id :ts]))
  ([]
     (last-blogs-summary 0)))


;;============================================
;; User stuff
;;============================================

(defn hash-password [password]
  (str password))

(defn user-authenticate? [username password]
  (let [db-user 
	   (fetch-one :users :where {:username username} :only [:username :password])]
    (= password (hash-password (:password db-user)))))
 