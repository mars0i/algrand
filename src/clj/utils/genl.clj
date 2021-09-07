;; general-purpose utilities
(ns utils.genl)


;; Alternative to (reduce f (repeat init n))
(defn iter 
  "Iteratively apply f to init n times without using a seq."
  [f init n]
  (loop [n n
         acc init]
    (if (<= n 0)
      acc
      (recur (dec n) (f acc)))))
