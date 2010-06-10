(ns blog.core
  (:use layout.layout
        layout.utils
        compojure.core
        ring.adapter.jetty)
  (:require [compojure.route :as route]))

;;(run-jetty example {:port 8080})
	
;; (run-jetty example {:port 8080 :ssl true :ssl-port 8443 :keystore "keystore" :key-password "secret"})
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
