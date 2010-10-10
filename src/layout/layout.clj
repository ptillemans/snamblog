(ns layout.layout
  (:require [net.cgrand.enlive-html :as html]
     [ring.util.codec :as c])
  (:use
   [layout.utils :only (maybe-content maybe-substitute escape-html)]
   blog.post
   ))

;; ============================================================================
;; The Templates
;; ============================================================================

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
        (html/set-attr :href (str "/post/" id))))

(html/defsnippet nav1 "layout/navs.html" [:div#nav1 ]
  []
  [:.content] (html/content (map link-model (last-posts-summary))))

(html/defsnippet nav2 "layout/navs.html" [:div#nav2] [])
(html/defsnippet nav3 "layout/navs.html" [:div#nav3] [])

(html/defsnippet actions "layout/navs.html" [:div#loggedin]
  [{id :id username :username}]
  [:span#username] (html/content username)
  [:a#edit] (html/set-attr :href (str "/post/" id "/edit"))
)

(html/defsnippet page-header "layout/base.html" [:div#header]
  [{id :id username :username}]
  [:div#action] (maybe-substitute (if username (actions {:id id :username username}))))

(html/defsnippet show-comment "layout/post.html" [:div#show_comment]
  [{:keys [comment author ts]}]
  [:p#show_comment_text] (html/content comment)
  [:span#ts] (html/content (str ts))
  [:span#author] (html/content author))

(html/defsnippet edit-comment "layout/post.html" [:div#edit_comment]
  [{:keys [post_id _id comment author ts]}]
  [:span#author] (html/content author)
  [:span#ts] (html/content (str ts))
  [:form] (html/set-attr :action (str "/post/" post_id "/comment/" _id))
  [:input#comment_form_id] (html/set-attr :value _id)
  [:textarea#comment_form_text] (html/content comment)
  )

(defn comment-box [comment username]
  (if (= username (:author comment))
    (edit-comment comment)
    (show-comment comment)))

(html/defsnippet post "layout/post.html" [:div#post]
  [{:keys [id title article]} username]
  [:h2#title html/text-node]
    (maybe-substitute title)
  [:div#article html/any-node]
    (maybe-substitute
     (html/html-snippet (markdown-to-html article)))
  [:form#new_comment_form]
    (html/set-attr :action (str "/post/" id "/comment"))
  [:div#comments]
    (html/content (map #(comment-box % username) (last-comments id))))

(html/defsnippet editpost "layout/editpost.html" [:div#editpost]
  [{:keys [id title article]}]
  [:input#id] (html/set-attr :id id)
  [:input#title] (html/set-attr :value (escape-html title))
  [:textarea#article html/any-node]
    (maybe-substitute
     (html/html-snippet (escape-html article))))

;; ============================================================================
;; Pages
;; ============================================================================

(defn view-post [post-info username]
  (let [navl (nav1)
        navr (nav2)
        body (post post-info username)]
   (base {:title (:title post-info)
          :header (page-header
                   {:id (:id post-info) :username username})
          :main (three-col
                 {:left  navl
                  :middle body
                  :right navr})
          })))

(defn edit-post [post-info username]
  (base {:title (:title post-info)
         :header (page-header {:id (:id post-info), :username username})
         :main (editpost post-info)}))

(defn index [username]
  (base {
         :header (page-header {:id nil, :username username})
	 :main   (str "Hello " username "!")
	     }))

