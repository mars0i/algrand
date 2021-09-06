;; general-purpose utilities
(ns utils.genl)


(defn iter 
  "Iteratively apply f to init n times."
  [f init n]
  (loop [n n
         acc init]
    (if (<= n 0)
      acc
      (recur (dec n) (f acc)))))
