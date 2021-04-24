;;;; Matrix utility functions not provided by clojure.core.matrix
(ns utils.matrix
    (:require [clojure.core.matrix :as mx]))

;(mx/set-current-implementation :persistent-vector)
(mx/set-current-implementation :ndarray) ; better at preserving ints
;(mx/set-current-implementation :vectorz)

;; Maybe this can be done more simply with views
(defn set-diag
  "Sets a one of the diagonals of matrix m to newval.  If offset = 0,
  this is the main diagonal.  If offset = -1, it's the subdiagonal.
  If offset = 1, it's the superdiagaonl.  Etc."
  [m offset newval]
   (mx/emap-indexed (fn [[i j] oldval]
                     (if (= (+ i offset) j)
                       newval
                       oldval))
                 m))

(defn shift-matrix
  "Returns a shift matrix, i.e. a square matrix with zero everywhere except
  on one of the diagonals, which will be all ones.  If offset is 1, the 
  superdiagonal is filled with 1's; if offset is -1, the subdiagonal is 
  filled.  Other offsets shift the diagonal 1's as you'd expect."
  [size offset]
  (mx/shift (mx/identity-matrix size) 0 offset))
