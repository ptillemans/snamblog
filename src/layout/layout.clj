(ns layout.layout
  (:require [net.cgrand.enlive-html :as html]
     [ring.util.codec :as c])
  (:use ring.adapter.jetty)
  (:use ring.middleware.params)
  (:use ring.middleware.file)
  (:use ring.middleware.session)
  (:use ring.util.response)
  (:use layout.utils)
  (:use blog.blog)
  (:use compojure.core))

;; =============================================================================
;; The Templates Ma!
;; =============================================================================

(html/deftemplate base "layout/base.html"
  [{:keys [title header main footer]}]
  [:#title]  (maybe-content title)
  [:#header] (maybe-substitute header)
  [:#main]   (maybe-substitute main)
  [:#footer] (maybe-substitute footer))

(html/defsnippet three-col "layout/3col.html" [:div#main]
  [{:keys [left middle right]}]
  [:div#left]   (maybe-substitute left)
  [:div#middle] (maybe-substitute middle)
  [:div#right]  (maybe-substitute right))

(def *link-sel* [[:.content (html/nth-of-type 1)] :> html/first-child])

(html/defsnippet link-model "layout/navs.html" *link-sel*
  [{title :title id :id}] 
  [:a] (html/do-> 
        (html/content title) 
        (html/set-attr :href (str "/blog/" id))))

(html/defsnippet nav1 "layout/navs.html" [:div#nav1 ] 
  []
  [:.content] (html/content (map link-model (last-blogs-summary))))

(html/defsnippet nav2 "layout/navs.html" [:div#nav2] [])
(html/defsnippet nav3 "layout/navs.html" [:div#nav3] [])

(html/defsnippet actions "layout/navs.html" [:div#loggedin]
  [{id :id username :username}]
  [:span#username] (html/content username)
  [:a#edit] (html/set-attr :href (str "/edit/" id))
)

(html/defsnippet header "layout/base.html" [:div#header]
  [{id :id username :username}]
  [:div#action] (maybe-substitute (if username (actions {:id id :username username}))))

(html/defsnippet blog "layout/blog.html" [:div#blog] 
  [{:keys [id title article]}]
  [:h2#title html/text-node] 
    (maybe-substitute title)
  [:div#article html/any-node] 
    (maybe-substitute 
     (html/html-snippet (markdown-to-html article))))

(html/defsnippet editblog "layout/editblog.html" [:div#editblog] 
  [{:keys [id title article]}] 
  [:input#id] (html/set-attr :id id)
  [:input#title] (html/set-attr :value (escape-html title))
  [:textarea#article html/any-node] 
    (maybe-substitute 
     (html/html-snippet (escape-html article))))

;; =============================================================================
;; Pages
;; =============================================================================

(defn view-blog [blog-info username]
  (let [navl (nav1)
        navr (nav2)]
   (base {:title (:title blog-info)
	  :header (header {:id (:id blog-info) :username username})
          :main (three-col {:left  navl
			    :middle (blog blog-info)
                            :right navr})})))

(defn edit-blog [blog-info username]
  (base {:title (:title blog-info)
         :header (header {:id (:id blog-info), :username username})
         :main (editblog blog-info)}))

(defn index [username] 
  (base {
         :header (header {:id nil, :username username})
	 :main   (str "Hello " username "!")
	     }))

;; =============================================================================
;; Routes
;; =============================================================================
(defroutes app-routes
  ;; app routes
  (GET "/" {session :session} 
       (render (index (:username session))))
;;       (str session))
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

