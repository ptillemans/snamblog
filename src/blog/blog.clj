(ns blog.blog
  (:import (org.mozilla.javascript Context ScriptableObject))
  (:use somnium.congomongo))

(defn load-text-resource [name]
	(apply str 
		(line-seq 
			(new java.io.BufferedReader 
				(new java.io.InputStreamReader 
					(.getResourceAsStream 
						(.getClassLoader clojure.main ) name))))))
						
(defn markdown-to-html [txt]
  (let [cx (Context/enter)
        scope (.initStandardObjects cx)
        input (Context/javaToJS txt scope)
        script (str (load-text-resource "showdown.js")
                    "new Showdown.converter().makeHtml(input);")]
    (try
     (ScriptableObject/putProperty scope "input" input)
     (let [result (.evaluateString cx scope script "<cmd>" 1 nil)]
       (Context/toString result))
     (finally (Context/exit)))))

(mongo! :db "snamblog")

(defn get-blog-info [id] 
  (fetch-one 
    :blog-posts
    :where { :id id }))

