
;; Miscellaneous experiments.  Not systematic.
(ns algrand.lfsr
    (:require [clojure.math.numeric-tower :as ma]
              [clojure.core.matrix :as mx]
              ;[denisovan.core] ; for Neanderthal experiments
              [utils.convertbase :as cb]
    ))


(defn matrec
  "Linear recurrence (LFSR) in Fq using matrix m and initial value vector v.
   Returns a new state vector.  q must be prime; doesn't currently handle 
  non-prime fields."
  [q m v]
  (map #(mod % q) (mx/inner-product v m)))

;; Lidl & Niederreiter p. 402, Ex. 8.14:
(def m814 (mx/matrix [[0 0 0 0 1][1 0 0 0 1][0 1 0 0 0][0 0 1 0 0][0 0 0 1 0]]))
(def xs814 (iterate (partial matrec 2 m814) [0 0 0 0 1]))
;; or:
(defn linrec814
  "Lidl and Niederreiter example 8.14 using explicit sums rather than 
  matrix multiplication."
  [v]
  ;;   the shift           calc new elem for end
  [(v 1) (v 2) (v 3) (v 4) (mod (+ (v 0) (v 1)) 2)])

(def ys814 (iterate linrec814 [0 0 0 0 1]))


;; OR SHOULD THE OLD ONE NOT BE DROPPED?  Let the sequence extend??
;; Simple LFSR based on vectors of 0's and 1's.
;; Example usage: (def states (iterate 
;;                              (partial lfsr [1 4 5])
;;                              [1 0 0 1 0 1 0 1 0 1 1]))
;; (def bs (map first states)) ; <-- a sequence of supposedly random bits
;; 
(defn lfsr2
  "Applies an F2 LSFR specified by taps, a sequence of (zero-based) indexes,
  and bits, which should be a seed vector of 0's and 1's.  The result drops 
  the first element in bits, and tacks onto the end the bitwise xor of
  elements of bits at locations specified by taps.  That is, a new value
  is constructed from earlier values, and appended to the end (rhs) of the
  random bits, while the first bit in the random bits is removed, perhaps
  after having been examined."
  [taps bits]
  (conj (vec (rest bits)) ; alternatives: drop, pop, rest, next.  see below.
        (reduce 
          (fn [sum tap] (bit-xor sum (nth bits tap)))
          0 taps)))

;; pop strips the last element from a vector, or the first element
;; from a list.  conj adds to end of vector, or to front of list.
;; Other operations remove vector-ness, to one extent or another.
;; I want LIFO semantics, not stack semantics.
;;
;; Or, to put the new value on the front, so that the head of the
;; random sequence is on the right, I could use butlast and cons or conj.

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Experiments with matrix-based LFSRs

;; Note I use inner-product rather than mmul
;; Logically we need mmul, not inner-product, but the result is the same,
;; and mmul but not inner-product has a bug that turns everything
;; into doubles, rather than preserving Ratio types.

;(mx/set-current-implementation :persistent-vector)
;(mx/set-current-implementation :ndarray)
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

