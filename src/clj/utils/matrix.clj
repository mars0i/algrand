;;;; Matrix utility functions not provided by clojure.core.matrix
(ns algrand.utils.matrix
    (:require [clojure.core.matrix :as mx]))

;(mx/set-current-implementation :persistent-vector)
;(mx/set-current-implementation :ndarray)
;(mx/set-current-implementation :vectorz)

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

(defn right-shift-mat
  "Returns a size X size matrix with 1's in the subdiagonal and
  zeros elsehwere.  Multiplying on the left of a vector returns
  the vector shifted one step down/right."
  [size]
  (set-diag (mx/zero-matrix size) -1 1))

(defn left-shift-mat
  "Returns a size X size matrix with 1's in the superdiagonal and
  zeros elsehwere.  Multiplying on the left of a vector returns
  the vector shifted one step up/left."
  [size]
  (set-diag (mx/zero-matrix size) 1 1))

;; Commented out because other ops will convert to floats anyway.
;; It's a lost cause.
;(defn zero-mat
;  "Version of clojure.core.matrix/zero-matrix that returns a zero matrix
;  with integers rather than floats."
;  [size]
;  (mx/emap int                     
;        (mx/zero-matrix size size))) ; returns doubles
