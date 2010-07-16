(ns blog.core
  (:use layout.layout
	blog.blog
        layout.utils
        compojure.core
        ring.adapter.jetty
	ring.middleware.session
        ring.util.response
        ring.middleware.file
        ring.middleware.session)
  (:require [compojure.route :as route]))

;; ============================================================================
;; Routes
;; ============================================================================
(defroutes app-routes
  ;; app routes
  (GET "/" {session :session} 
       (let [article (get-blog-info "index")]
	 (render (view-blog article (:username session)))))
  (GET "/blog/:id" {params :params session :session}
       (let [id (params "id")
	     username (:username session)
	     article (get-blog-info id)]
	 (render (view-blog article username))))

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
	  (edit-blog 
	   (get-blog-info id) 
	   username))))

  (POST "/edit/:id" [id title article]
       (do 
	 (update-blog {:id id 
		       :article (unescape-html article)})
	   (redirect (str "/blog/" id))))

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

(defn run []
  (run-jetty (var app) {:join? false :port 8080}))

;; (run-jetty example {:port 8080 :ssl true :ssl-port 8443 :keystore "keystore" :key-password "secret"})

