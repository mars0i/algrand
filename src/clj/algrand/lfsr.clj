(ns algrand.lfsr
    (:require [clojure.math.numeric-tower :as ma]
              [clojure.core.matrix :as mx]
              ;[denisovan.core] ; for Neanderthal experiments
              [utils.convertbase :as cb]
    ))


;; Simple LFSR based on vectors of 0's and 1's.
;; Example usage: (iterate (partial lfsr [1 4 5]) [1 0 0 1 0 1 0 1 0 1 1])
(defn lfsr 
  "Applies an LSFR specified by list of (zero-based) taps indexes to bit-vec,
  which should be a vector of 0's and 1's.  The result drops the first element
  in bit-vec, and tacks onto the end the bitwise xor of elements of bit-vec at
  locations specified by taps."
  [taps bit-vec]
  (conj (vec (rest bit-vec)) ; alternatives: drop, pop, rest, next.  see below.
        (reduce bit-xor 
               (map (partial nth bit-vec) taps))))

;; pop strips the last element from a vector, or the first element
;; from a list.  conj adds to end of vector, or to front of list.
;; Other operations remove vector-ness, to one extent or another.
;; I want LIFO semantics, not stack semantics.



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Experiments with matrix-based LFSRs

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
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

