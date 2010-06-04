(ns layout.layout
  (:require [net.cgrand.enlive-html :as html])
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

(html/defsnippet nav1 "layout/navs.html" [:div#nav1] [])
(html/defsnippet nav2 "layout/navs.html" [:div#nav2] [])
(html/defsnippet nav3 "layout/navs.html" [:div#nav3] [])
(html/defsnippet blog "layout/blog.html" [:div#blog] 
  [{:keys [id title article]}]
  [:h2#title html/text-node] 
    (maybe-substitute title)
  [:div#article html/any-node] 
    (maybe-substitute 
     (html/html-snippet (markdown-to-html article))))

;; =============================================================================
;; Pages
;; =============================================================================

(defn view-blog [blog-info]
  (let [navl (nav1)
        navr (nav2)]
   (base {:title "View B"
          :main (three-col {:left  navl
			    :middle (blog blog-info)
                            :right navr})})))

(defn viewc [params session]
  (let [navs [(nav1) (nav2)]
        [navl navr] (if (= (:action params) "reverse") (reverse navs) navs)]
    (base {:title "View C"
           :main (three-col {:left  navl
                             :right navr})})))

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
  (GET "/blog/:id" {params :params}
       (render (view-blog (get-blog-info (params "id")))))
  (GET "/edit/:id" {params :params}
       (render (edit-blog (get-blog-info (params "id")))))
  (POST "/edit/:id" {params :params}
       (do (update-blog params)
	   (render (edit-blog (get-blog-info (params "id"))))))
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

