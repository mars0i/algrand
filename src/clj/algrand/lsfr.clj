(ns algrand.lsfr
    (:require [clojure.math.numeric-tower :as ma]
              [clojure.core.matrix :as mx]
              ;[denisovan.core] ; for Neanderthal experiments
              [utils.convertbase :as cb]
    ))

;; Note I use inner-product rather than mmul
;; Logically we need mmul, not inner-product, but the result is the same,
;; and mmul but not inner-product has a bug that turns everything
;; into doubles, rather than preserving Ratio types.

;(mx/set-current-implementation :persistent-vector)
(mx/set-current-implementation :ndarray)
;(mx/set-current-implementation :vectorz)

(defn right-shift-mat
  [size shift]
  (mx/shift (mx/identity-matrix size) 0 shift))

(defn left-shift-mat
  [size shift]
   (mx/shift (mx/identity-matrix size) 1 shift))



;;;;;;;;;;
;; Marsaglia's 32-bit xorshift PRNG from Kneusel pp. 50ff:

(def left13 (left-shift-mat 32 13))
(def right17 (right-shift-mat 32 17))
(def left5 (left-shift-mat 32 5))
(def i32 (mx/identity-matrix 32))

(def xorshift32-mat
  (mx/inner-product (mx/add i32 left13)
                    (mx/add i32 right17)
                    (mx/add i32 left5)))

(def kneusel-seed (mx/matrix (cb/convert-int-to-seq 2 2463534242)))

;; (inner-product kneusel-seed xorshift32) doesn't produce the 
;; correct result because it uses regular multiplication rather than
;; F2 operations.  I need an actual xor across the elements.

