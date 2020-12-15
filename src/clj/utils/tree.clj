(ns utils.tree)

(defn tree 
  "Attempt to create lazy, potentially infinite trees in Clojure."
  [brancher top]
  (cons top 
        (lazy-seq (brancher top))))

(defn foo-brancher
  "Non-infinite brancher function example."
  [n]
  (if (zero? n)
    nil
    (map (partial tree foo-brancher)
         (reverse (range n)))))

(defn bar-brancher
  "Infinite?"
  [n]
  (map (partial tree bar-brancher)
       [(+ n 1) (+ n 2)]))

;; display such a tree
;;(clojure.pprint (tree br 7))

