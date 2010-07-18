(ns blog.core
  (:use layout.layout
	blog.post
        layout.utils
        compojure.core
        [ring.adapter.jetty :only (run-jetty)]
        [ring.util.response :only (redirect)]
 	[ring.middleware.file :only (wrap-file)]
        [ring.middleware.session :only (wrap-session)])
  (:require [compojure.route :as route]))

;; ============================================================================
;; Routes
;; ============================================================================
(defroutes app-routes
  ;; app routes
  (GET "/" {session :session} 
      (redirect (str "/post/index")))

  (GET "/post/:id" {params :params session :session}
       (let [id (params "id")
	     username (:username session)
	     article (get-post-info id)]
	 (render (view-post article username))))

  (POST "/login" {headers :headers params :params session :session}
	(let [ username (params "username")
	       password (params "password")
               authenticated (user-authenticate? username password)]
	       (assoc (redirect (headers "referer"))
		 :session (if authenticated 
			    (assoc session :username username) 
			    {})
	       )))

  (GET "/logout" {headers :headers session :session}
	(assoc (redirect (headers "referer"))
	  :session (dissoc session :username)))

  (GET "/edit/:id" {params :params session :session}
       (let [id       (params "id")
	     username (session :username)]
	 (render 
	  (edit-post 
	   (get-post-info id) 
	   username))))

  (POST "/edit/:id" [id title article]
       (do 
	 (update-post {:id id 
		       :article (unescape-html article)})
	   (redirect (str "/post/" id))))

  ;; static files

  (ANY "*" []
       "Page Not Found"))

;; =============================================================================
;; The App
;; =============================================================================

(def app
     (-> app-routes
         (wrap-file "public")
	 (wrap-session)
     ))

(defonce server 
  (run-jetty (var app) {:join? false :port 8080}))

(defn run [] (.start server))

(defn stop [] (.stop server))

