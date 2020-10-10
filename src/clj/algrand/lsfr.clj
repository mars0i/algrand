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
  ([size]
   (mx/shift (mx/identity-matrix size) 0 1))
  ([size shifts]
   (let [m1 (right-shift-mat size)]
         (loop [m m1 i shifts]
               (if (<= i 1)
                 m
                 (recur (mx/inner-product m1 m) (dec i)))))))

(defn left-shift-mat
  ([size]
   (mx/shift (mx/identity-matrix size) 1 1))
  ([size shifts]
   (let [m1 (left-shift-mat size)]
         (loop [m m1 i shifts]
               (if (<= i 1)
                 m
                 (recur (mx/inner-product m1 m) (dec i)))))))

;;;;;;;;;;
;; Marsaglia's 32-bit xorshift from Kneusel pp. 50ff:

(def left13 (left-shift-mat 32 13))
(def right17 (right-shift-mat 32 17))
