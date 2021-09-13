(ns algrand.sylvester
    (:require [clojure.math.numeric-tower :as ma]
              [clojure.core.matrix :as mx]
              [utils.math :as um]
              [utils.convertbase :as cb]))

;; Note that clojure.core.matrix's inner-product and mmul can be used
;; to do all of the same operations, except that mmul converts integer
;; inputs to float outputs, while inner-product preserves numeric 
;; character.  (This is a bug in mmul.)  So I always use inner-product
;; to multiply matrices, vectors, etc..

;; Sometimes this is all I need in the repl from core.matrix.
(def prm 
  "Local convenience abbreviation for clojure.core.matrix/pm."
  mx/pm)

;; IN THE FOLLOWING, vector and matrix elements are represented in the
;; natural Clojure order: vector element 1 is Clojure vector element 0, etc.

;; docstring attempts to emulate what defn does.  Adding backspaces
;; to the param list would do more, but could be annoying in some context.
(def make-sylvester
  "([n])
  Generates a Hadamard-Sylvester matrix of size 2^n X 2^n.  Memoizes
  the result."
  (memoize
    (fn [n]
        (if (== n 0)
          (mx/matrix [[1]])
          (let [m (make-sylvester (dec n))
                m- (mx/mul -1 m)]
            (mx/join-along 1 (mx/join m m) (mx/join m m-)))))))

;; Maybe this belongs elsewhere or this ns should be renamed.
;; Seems to recreates the necessary Sylvester matrix each time, but
;; that's OK because make-sylvester memoizes.
(defn fourier
  "Boolean function Fourier transformation using Sylvester matrix.
  Vector s, which will be treated as a column vector, should have a 
  length that is a power of 2."
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
