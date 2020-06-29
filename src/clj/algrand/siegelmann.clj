;; Functions for experimenting with ideas in Hava Siegelmann's book
;; *Neural Networks and Analog Computation: Beyond the Turing Limit*
(ns algrand.siegelmann
    (:require [clojure.math.numeric-tower :as math]
              [utils.convertbase :as base]))

;; convenience abbreviations
(def n2s base/number-to-string)
(def s2n base/string-to-number)

(defn sigma
  "Linear sigma function: Returns x unless it's outside of [0,1], in
  which case 0 or 1--whichever is closest--is returned."
  [x]
  (cond (< x 0) 0
	(> x 1) 1
	:else x))

(defn lambda-tilde
  "lambda-tilde [equation (4.6), p. 64] is Siegelmann's continuous version of
  Lambda, a left-shift on base-9 fractional numbers that use only even digits."
  [q]
  (let [q9 (* q 9)]
    (reduce 
      (fn [sum j] (+ sum 
                     (* (math/expt -1 j)
                        (sigma (- (* q 9) j)))))
      0 (range 9))))
    


(defn xsi-tilde
  "xsi-tilde [equation (4.7), p. 65] is Siegelmann's continuous version of Xsi,
  a select-leftmost-digit function on base-9 fractional numbers that use only 
  even digits."
  [q]
  (let [q9 (* q 9)]
    (* 2
       (loop [j 3 acc 0]
         (if (neg? j)
           acc
           (recur (dec j)
                  (+ acc
                     (sigma (- q9 (* 2 j) -1)))))))))


;; loop/recur version
;(defn old2-lambda-tilde
;  "lambda-tilde [equation (4.6), p. 64] is Siegelmann's continuous version of
;  Lambda, a left-shift on base-9 fractional numbers that use only even digits."
;  [q]
;  (let [q9 (* q 9)]
;    (loop [j 8 acc 0]
;      (if (neg? j)
;        acc
;        (recur (dec j)
;               (+ acc 
;                  (* (math/expt -1 j)
;                     (sigma (- q9 j)))))))))
