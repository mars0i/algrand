(ns algrand.sylvester
    (:require [clojure.math.numeric-tower :as ma]
              [clojure.core.matrix :as mx]
              [utils.math :as um]))

(defn make-sylvester
  "Generates a Hadamard-Sylvester matrix of size 2^n X 2^n."
  [n]
  (if (== n 0)
    (mx/matrix [[1]])
    (let [m (make-sylvester (dec n))
          m- (mx/mul -1 m)]
      (mx/join-along 1 (mx/join m m) (mx/join m m-)))))

;; Maybe this belongs elsewhere or this ns should be renamed
;; Inefficiently reconstructs the needed Sylvester-Hadamard matrix every time
(defn fourier
  "Boolean function Fourier transformation using Sylvester matrix.
  Vector s, which will be treated as a column vector, should have a 
  length that's a power of 2."
  [s]
  (let [len (count s)
        len-log-double (um/log-base-n 2 len)
        len-log-long (long len-log-double)
        eps 0.0000000001 ; is this the best value here?
        sylvester-sized (< (- len-log-double len-log-long) eps)]
    (when sylvester-sized
      (mx/emap #(/ % len)
               (mx/inner-product (make-sylvester len-log-long)
                                 s)))))

