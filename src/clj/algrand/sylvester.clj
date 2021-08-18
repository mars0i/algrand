(ns algrand.sylvester
    (:require [clojure.math.numeric-tower :as ma]
              [clojure.core.matrix :as mx]))

(defn make-sylvester
  "Generates a Hadamard-Sylvester matrix of size 2^n X 2^n."
  [n]
  (if (== n 0)
    (mx/matrix [[1]])
    (let [m (make-sylvester (dec n))
          m- (mx/mul -1 m)]
      (mx/join-along 1 (mx/join m m) (mx/join m m-)))))
