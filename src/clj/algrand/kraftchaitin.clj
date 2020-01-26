;;;; An implementation of the algorithm in Nie's proof of the 
;;;; Machine Existence (Kraft-Chaitin) theorem in his book.
(ns algrand.kraftchaitin
    (:require [clojure.pprint :as pp]
              [clojure.math.numeric-tower :as m]
              [clojure.string :as s]))

;;(clojure.pprint/cl-format true "0.~b\n" (read-string "2r101"))
;; Note that if you pass a non-integer to ~b, you get back a decimal
;; representation.

(defn interval-of
  "Return a pair specifying bounds [x,y) of the interval generated by
  binary string s.  String can be specifed with our without a decimal
  point, but it's assumed to represent a value in [0,1)."
  [bin-s]
  (let [s (last (s/split bin-s #"\.")) ; strip initial int and decimal point
        s-len (count s)]
    (if (zero? s-len) ; special case for "", i.e. "0."
      [0.0 1.0]
      (let [s-weight (m/expt 2.0 (- s-len))
            s-int (read-string (str "2r" s)) ; only ints--no fractions allowed
            left-bound (* s-int s-weight)
            right-bound (+ left-bound s-weight)]
        [left-bound right-bound]))))

;; Note that when r = length of z, that's the z we want.  This
;; function will skip past it and pull it back on the next iteration.
;; That very slight inefficiency makes the code simpler.
(defn max-shorter
  "Given a sequence of strings Rn-1 sorted by length, and a string size r,
  returns the largest string with size greater than or equal to r."
  [Rn-1 r]
  (loop [zs Rn-1]
    (cond (not (next zs))          (first zs) ; no more; use what we have
          (< r (count (fnext zs))) (first zs)
          :else                    (recur (next zs)))))

(defn make-w
  [old-z pad-len]
  (apply str 
         old-z 
         (repeat pad-len \0))) ; returns z if pad-len is zero

(defn make-new-z
  [old-z pad-len]
  (if (< pad-len 1)
    nil ; this shouldn't happen
    (str
      (apply str 
             old-z
             (repeat (dec pad-len) \0))
      \1)))

(defn make-zs
  [old-z max-pad-len]
  (map (fn [pad-len] (make-new-z old-z pad-len))
       (range 1 (inc max-pad-len))))

(defn next-R-stage
  [Rn-1 r]
  (let [z (max-shorter Rn-1 r)
        pad-len (- r (count z))
        w (make-w z pad-len) 
        new-zs (make-zs z pad-len)
        Rn (concat (remove (partial = z) Rn-1)
                   new-zs)]
    [w Rn]))

(defn requests-weight
  "Calculate the 'weight' of a set of requested lengths for prefix-free 
  input codes (Nies p. 86).  Each r in rs should be a non-negative
  integer.  The weight is the sum_r 2^-r .  The weight condition is
  the requirement that this sum be <= 1."
  [rs]
  (reduce 
    (fn [sum r] (+ sum (m/expt 2.0 (- r))))
    0 rs))

;; TODO: fix docstring
(defn R-stages
  "TODO.  Throws an exception if the weight condition is not satisfied."
  [rs]
  (let [weight (requests-weight rs)]
    (if (> weight 1) ; test that weight condition is satisfieed
      (throw (Exception. (str "Weight condition isn't satisfied: weight " weight " > 1.")))
      (reduce (fn [[ws Rns] r]
                  (let [[w Rn] (next-R-stage (first Rns) r)]
                    [(cons w ws) (cons Rn Rns)]))
              [nil ""]
              rs))))

;; FIXME note bug displayed below:
;; At step 4, Rn is empty.
;; The w="000" at step 5 includes an earlier prefix "0".
;; The problem seems to be in next-R-stage:
; user=> (next-R-stage ["111"] 3)
; ["111" ()]
;
;; OR MAYBE the problem was just that the list of sizes I gave is
;; illegal: It gives a total weight > 1.0.  I think this might be
;; it.  (Maybe I should test this first.)
;; Did new weight test above solve it?
; 
; user=> (clojure.pprint/pprint (R-stages [1 2 3 3 3 5 5 6 6 6 6 7 8]))
; [("01000010"
;   "0100000"
;   "001111"
;   "001110"
;   "001101"
;   "001100"
;   "00101"
;   "00100"
;   "000"
;   "111"
;   "110"
;   "10"
;   "0")
;  (("1" "011" "0101" "01001" "010001" "01000011")
;   ("1" "011" "0101" "01001" "010001" "0100001")
;   ("1" "01")
;   ("1" "01" "001111")
;   ("1" "01" "00111")
;   ("1" "01" "00111" "001101")
;   ("1" "01" "0011")
;   ("1" "01" "0011" "00101")
;   ("1" "01" "001")
;   ()
;   ("111")
;   ("11")
;   ("1"))]
; nil
