(ns algrand.lsfr
    (:require [clojure.math.numeric-tower :as ma]
              [clojure.core.matrix :as mx]
              ;[denisovan.core] ; for Neanderthal experiments
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
;; Marsaglia's 32-bit xorshift from Kneusel pp. 50ff:

(def left13 (left-shift-mat 32 13))
(def right17 (right-shift-mat 32 17))
