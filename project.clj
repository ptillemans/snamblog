(defproject hello-www "1.0.0-SNAPSHOT"
  :description "A Compojure 'Hello World' application."
  :dependencies [[org.clojure/clojure "1.1.0"]
                 [org.clojure/clojure-contrib "1.1.0"]
                 [compojure "0.4.0-SNAPSHOT"]
		 [enlive "1.0.0-SNAPSHOT"]
                 [ring/ring-jetty-adapter "0.2.0"]
                 [org.mortbay.jetty/jetty-util "6.1.14"]
                 [congomongo "0.1.2-SNAPSHOT"]
                 [rhino/js "1.7R2"]]
  :dev-dependencies [[uk.org.alienscience/leiningen-war "0.0.3"]
		     [lein-cuke "0.0.1-SNAPSHOT"] 
		     [swank-clojure "1.2.1"]
		     [com.martiansoftware.nailgun "0.7.1"]])