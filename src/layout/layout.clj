(ns layout.layout
  (:require [net.cgrand.enlive-html :as html]
     [ring.util.codec :as c])
  (:use ring.adapter.jetty)
  (:use ring.middleware.params)
  (:use ring.middleware.file)
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

(defn view-blog [blog-info]
  (let [navl (nav1)
        navr (nav2)]
   (base {:title (:title blog-info)
          :main (three-col {:left  navl
			    :middle (blog blog-info)
                            :right navr})})))

(defn edit-blog [blog-info]
  (base {:title (:title blog-info)
         :main (editblog blog-info)}))

(defn index
  ([] (base {}))
  ([ctxt] (base ctxt)))

;; =============================================================================
;; controllers
;; =============================================================================

(defn get-blog-article [params session]
  (let [blog { :id (params "id") :title "My First Blog Post" :article "Something *very* interesting"}]
    blog))

;; =============================================================================
;; Routes
;; =============================================================================

(defroutes app-routes
  ;; app routes
  (GET "/" [] 
       (render (index)))
  (GET "/blog/:id" [id]
       (render (view-blog (get-blog-info id))))
  (GET "/edit/:id" [id]
       (render (edit-blog (get-blog-info id))))
  (POST "/edit/:id" [id title article]
       (do 
	 (update-blog {:id id 
		       :article (unescape-html article)})
	   (render (edit-blog (get-blog-info id)))))
  (GET "/c/" {params :params session :session}
       (render (viewc params session)))
  (GET "/c/:action" {params :params session :session}
       (render (viewc params session)))

  ;; static files

  (ANY "*" []
       "Page Not Found"))

;; =============================================================================
;; The App
;; =============================================================================

(def app
     (-> app-routes
         (wrap-file "public")))

(defn run []
  (run-jetty (var app) {:join? false :port 8080}))

