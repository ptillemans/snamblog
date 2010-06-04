(ns blog.core
  (:use layout.layout
        layout.utils
        compojure.core
        ring.adapter.jetty)
  (:require [compojure.route :as route]))

(defn calc [a]
  (str "<h1>a = " a "</h1>"))

(defn calc-add [x y]
  (let [a (Integer. x) b (Integer. y)]
	(str "<h1>" x " + " y " = " (+ a b) "</h1>")))

(defroutes example
  (GET "/" [] "<h1>Hello World Wide Web!</h1>")
  (GET "/"
       (render (index)))
  (GET "/a/"
       (render (viewa params session)))
  (GET "/b/"
       (render (viewb params session)))
  (GET "/c/"
       (render (viewc params session)))
  (GET "/c/:action"
       (render (viewc params session)))

  ;; static files
  (GET "/base.html"
       (serve-file *webdir* "base.html"))
  (GET "/3col.html"
       (serve-file *webdir* "3col.html"))
  (GET "/navs.html"
       (serve-file *webdir* "navs.html"))
  (GET "*/main.css"
       (serve-file *webdir* "main.css"))

  (GET (str "/calc" "/:a" ) [a] 
    (calc a))
  (GET (str "/calc" "/:a" "/add" "/:b") [a b]
    (calc-add a b))

  (route/not-found "Page not found"))

;;(run-jetty example {:port 8080})
	
;; (run-jetty example {:port 8080 :ssl true :ssl-port 8443 :keystore "keystore" :key-password "snowball"})
;; ========================================
;; The App
;; ========================================

(defonce *app* (atom nil))

(defn start-app []
  (if (not (nil? @*app*))
    (stop @*app*))
  (reset! *app* (run-server {:port 8080}
                            "/*" (servlet example-routes))))

(defn stop-app []
  (when @*app* (stop @*app*)))