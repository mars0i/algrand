;;;; Matrix utility functions not provided by clojure.core.matrix
(ns utils.matrix
    (:require [clojure.core.matrix :as m]))

;(m/set-current-implementation :persistent-vector)
(m/set-current-implementation :ndarray) ; better at preserving ints
;(m/set-current-implementation :vectorz)

;; Maybe this can be done more simply with views
(defn set-diag
  "Sets a one of the diagonals of matrix m to newval.  If offset = 0,
  this is the main diagonal.  If offset = -1, it's the subdiagonal.
  If offset = 1, it's the superdiagaonl.  Etc."
  [m offset newval]
   (m/emap-indexed (fn [[i j] oldval]
                     (if (= (+ i offset) j)
                       newval
                       oldval))
                 m))

(defn shift-matrix
  "Returns a shift matrix, i.e. a square matrix with zero everywhere except
  on the subdiagonal or superdiagonal, which consists of ones.  If offset is
  1, the superdiagonal is filled with 1's; if offset is -1, the subdiagonal is 
  filled.  The function generalizes the concept of a shift matrix by
  allowing other offsets to shift the diagonal of 1's further, or to return
  an identity matrix when offset = 0."
  [size offset]
  (m/shift (m/identity-matrix size) 0 offset))

(defn reverse-rows
  "Returns a matrix with rows in reverse order."
  [m]
  (m/matrix (reverse (m/slices m))))

(defn reverse-cols
  "Returns a matrix with columns in reverse order."
  [m]
  (m/transpose 
    (m/matrix (reverse (m/slices m 1)))))

(defn zero-mat
  "Returns a matrix consisting of 0's of integer long type.  If only one 
  argument argument is given, a square matrix is returned.  (core.matrix's 
  zero-matrix uses doubles.)"
  ([size] (zero-mat size size))
  ([h w] (m/emap long (m/zero-matrix h w))))

(defn unit-leslie
  "Returns a Leslie matrix with 1's for elements of the first row and
  subdiagonal, and zeros elsewhere."
  [size]
  (m/add
    (m/set-row (zero-mat size) 0 (repeat size 1))
    (shift-matrix size -1)))
