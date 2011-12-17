(defproject snamblog "0.1.0-SNAPSHOT"
  :description "A Compojure based weblog for personal use."
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [compojure "0.4.1"]
                 [enlive "1.0.0-SNAPSHOT"]
                 [ring/ring-jetty-adapter "0.2.5"]
                 [org.clojars.bmabey/congomongo "0.1.2-SNAPSHOT"]
                 [rhino/js "1.7R2"]]
  :main blog.core)
