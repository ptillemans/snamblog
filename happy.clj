(defn sqr [x] (* x x))
(defn digits [x coll] 
	(if (<= x 0) 
		coll 
		(recur (int (/ x 10)) (cons (rem x 10) coll))))
(defn ssd [x] (reduce + (map #(sqr %) (digits x ()))))
(defn happy? [x a-map] 
	(cond 
		(= x 1) "happy"
		(a-map x) "sad"
		:else  (recur (ssd x) (assoc a-map x 1))))
			
(sumSquareDigits 139)