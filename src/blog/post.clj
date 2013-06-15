(ns blog.post
  (:import (org.mozilla.javascript Context ScriptableObject))
  (:import (java.util Date))
  (:import (java.io BufferedReader InputStreamReader))
  (:require [clojure.contrib.str-utils2 :as s])
  (:use somnium.congomongo)
  (:use blog.hash))

(defn load-text-resource [name]
  (apply str
	 (line-seq
	  (new BufferedReader
	       (new InputStreamReader
		    (.getResourceAsStream
		     (.getClassLoader clojure.main ) name))))))

(defn markdown-to-html [txt]
  (let [cx (Context/enter)
        scope (.initStandardObjects cx)
        text (if (string? txt) txt "No text present.")
        input (Context/javaToJS text scope)
        script (str (load-text-resource "showdown.js")
                    "new Showdown.converter().makeHtml(input);")]
    (try
     (ScriptableObject/putProperty scope "input" input)
     (let [result (.evaluateString cx scope script "<cmd>" 1 nil)]
       (Context/toString result))
     (finally (Context/exit)))))

(mongo! :host "127.0.0.1" :db "snamblog")

(defn get-post-info [id]
  (or
   (fetch-one
    :blogposts
    :where { :id id })
   {:id id
    :title "No such article"
    :article
"No such article
===============
This space intentionally left blank (for now ;-) ).
"
    }))

(defn update-post [params]
  (let [post (get-post-info (:id params))
        updates (assoc params
                  :title (first (s/split-lines (:article params)))
                  :ts (Date.))]
    (if (contains? post :_id)
      (update! :blogposts post (merge post updates))
      (insert! :blogposts updates))))

(defn last-posts-summary
  ([n]
     (fetch
        :blogposts
	:where {:id {:$exists 1}}
	:order :ts
	:limit 5
	:skip n
	:only [:title :id :ts]))
  ([]
     (last-posts-summary 0)))

;;============================================
;; Comments on posts
;;============================================

(defn get-comment [id]
  (if id
   (fetch-by-id "comments" id)
   {:comment "" :author "*unknown*"}))

(defn update-comment [params]
  (let [updates (assoc params
                  :ts (Date.))]
    (if (contains? updates :_id)
      (let [comment (get-comment (:_id params))
            new-comment  (merge comment (dissoc updates :_id))]
        (update! :comments comment new-comment))
      (insert! :comments updates))))

(defn last-comments
  ([post_id n]
     (fetch
      :comments
      :where {:post_id post_id}
      :order :ts
      :limit 10
      :skip n))
  ([post]
     (last-comments post 0)))


;;============================================
;; User stuff
;;============================================

(defn get-user [username]
  (fetch-one :users
	     :where {:username username}))

(defn update-user [user]
  (let [saved-user (get-user (:username user))
        new-user   (merge saved-user user)]
    (if (contains? new-user :_id)
      (update! :users saved-user new-user)
      (insert! :users new-user))))

(defn- get-hash [username]
  (:password (get-user username)))

(defn set-hash [username password]
  (update-user (assoc (get-user username) :password (sha256 password))))

(defn user-authenticate? [username password]
  (let [hash (get-hash username)]
    (= (sha256 password) hash)))
