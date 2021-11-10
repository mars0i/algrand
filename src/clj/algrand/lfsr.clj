
;; Miscellaneous experiments.  Not systematic.
(ns algrand.lfsr
    (:require ;[clojure.math.numeric-tower :as nt]
              [clojure.core.matrix :as mx]
              [algrand.finitefield :as ff]
              [utils.convertbase :as cb]
    ))


;; Lidl & Niederreiter p. 402, Example 8.14:
(def m814 (mx/matrix [[0 0 0 0 1][1 0 0 0 1][0 1 0 0 0][0 0 1 0 0][0 0 0 1 0]]))

(def xs814 (iterate (fn [prev-state] (ff/finite-mmul 2 prev-state m814))
                    [0 0 0 0 1]))

;; or:

;; Think of the LFSR algorithm this way:
;; Calculate the new highest-index element.  
;; (You can think of it as now sitting just past the end of the register.)
;; Then shift every element to the lower element, losing the old lowest one.
;; And as part of this, the old highest element is replaced by the newly
;; calculated value, which is "shifted" in from its "location" off the
;; high end of the register.
(defn linrec814
  "Lidl and Niederreiter example 8.14 using explicit sums rather than 
  matrix multiplication."
  [v]
  ;; shift to lower locs   calc new elem for high index
  [(v 1) (v 2) (v 3) (v 4) (mod (+ (v 0) (v 1)) 2)])

(def ys814 (iterate linrec814 [0 0 0 0 1]))

;; Here's a recurrence (not in L&N).  It generates all 0's at first.
(defn linrecA
  [v]
  [(v 1) (v 2) (v 3) (v 4) (mod (+ (v 0) (v 4)) 2)])

(def ysA (iterate linrecA [0 0 0 0 1]))

(defn linrec822
  "Lidl and Niederreiter example 8.22."
  [[a0 a1]]
  [a1 (mod (+ a0 a1) 2)])

(def ys822 (iterate linrec822 [1 1])) ; Lidl and Niederreiter example 8.22

(defn linrec830
  "Lidl and Niederreiter example 8.30."
  [[a0 a1 a2 a3 a4 a5]]
  [a1 a2 a3 a4 a5 (mod (+ a4 a2 a1 a0) 2)])

(def ys830impulse (iterate linrec830 [0 0 0 0 0 1]))
(def ys830_11 (iterate linrec830 [0 0 0 0 1 1]))
(def ys830_100 (iterate linrec830 [0 0 0 1 0 0]))

(defn linrec4
  "Full period n=4 using taps based on polynomial from N&W p. 37."
  [[a0 a1 a2 a3]]
  [a1 a2 a3 (mod (+ a0 a1) 2)])

(defn linrec5
  "Full period n=5 using taps based on polynomial from N&W p. 37."
  [v]
  [(v 1) (v 2) (v 3) (v 4) (mod (+ (v 0) (v 2)) 2)])

(defn linrec6
  "Full period n=6 using taps based on polynomial from N&W p. 37."
  [[a0 a1 a2 a3 a4 a5]]
  [a1 a2 a3 a4 a5 (mod (+ a0 a1) 2)])

(defn linrec8
  "Full period n=8 using taps based on polynomial from N&W p. 37."
  [[a0 a1 a2 a3 a4 a5 a6 a7]]
  [a1 a2 a3 a4 a5 a6 a7 (mod (+ a0 a2 a3 a4) 2)])


(defn linrec12
  "Full period n=12 using four taps based on polynomial from N&W p. 37."
  [[a0 a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11]]
  [a1 a2 a3 a4 a5 a6 a7 (mod (+ a0 a1 a4 a6) 2)])

(defn linrec12seq
  "Returns a lazy sequence from linrec12 starting from seed."
  [seed]
  (iterate linrec12 seed))

;; FIXME doesn't work because linrec12 returns a sequence of states,
;; not a sequence of digits.
(defn tausworthe12
  "Returns a paid containing the new state and a new output number as
  a Clojure fraction.  (Pass to a function such as double to convert to a 
  floating point number.)"
  [state wordsize decimation]
  (let [after-decimation (drop decimation (linrec12seq state));; IS THIS RIGHT?
        word (take wordsize after-decimation)
        _ (prn word)
        newstate (take (count state) (drop wordsize after-decimation))]
    [newstate (cb/sum-digits 2 [0] word)]))

;;;;;;;;;;;;;;;;;;;;;;;;;

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

