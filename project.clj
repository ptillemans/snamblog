(defproject snamblog "0.1.0-SNAPSHOT"
  :description "A Compojure based weblog for personal use."
  :dependencies [[org.clojure/clojure "1.2.0-beta1"]
                 [org.clojure/clojure-contrib "1.2.0-beta1"]
                 [compojure "0.4.1"]
		 [enlive "1.0.0-SNAPSHOT"]
                 [ring/ring-jetty-adapter "0.2.5"]
                 [congomongo "0.1.2-SNAPSHOT"]
                 [rhino/js "1.7R2"]]
  :dev-dependencies [[uk.org.alienscience/leiningen-war "0.0.3"]
		     [org.clojars.springify/lein-cuke "0.0.2"]
		     [swank-clojure "1.2.1"]])