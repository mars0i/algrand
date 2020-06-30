;; Functions for experimenting with ideas in Hava Siegelmann's book
;; *Neural Networks and Analog Computation: Beyond the Turing Limit*
(ns algrand.siegelmann
    (:require [clojure.math.numeric-tower :as math]
              [utils.convertbase :as base])
    (:use [uncomplicate.neanderthal.core :only [dot]]
          [uncomplicate.neanderthal.native :only [dv]]))

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

;;;;;;;;;;;;;;;;;;;;;;;;;
;; Chapter 4:

(defn lambda-tilde
  "lambda-tilde [equation (4.6), p. 64] is Siegelmann's continuous version of
  Lambda, a left-shift on base-9 fractional numbers that use only even digits."
  [q]
  (let [q9 (* q 9)]
    (reduce 
      (fn [sum j] (+ sum 
                     (* (math/expt -1 j)
                        (sigma (- q9 j)))))
      0 (range 9))))

(defn xsi-tilde
  "xsi-tilde [equation (4.7), p. 65] is Siegelmann's continuous version of Xsi,
  a select-leftmost-digit function on base-9 fractional numbers that use only 
  even digits."
  [q]
  (let [q9 (* q 9)]
    (* 2
       (reduce (fn [sum j]
                   (+ sum
                      (sigma (- q9 (inc (* 2 j))))))
               0 (range 4)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; defining the network
;; with Neanderthal

;; Encoding of single circuit (s/b many) from example 4.1.1 on p.62:
(def c-hat (base/string-to-number "0.860424440444240222426044444" 9))

(def u (base/string-to-number "0.1" 2))

;; In the statement of lemma 4.1.2 on p.63, HTS says you need a 16-node
;; net, but in the model on p.65, there are nodes 0 through 16, i.e.
;; 17 nodes.  I add the input node u at the end (though that will have
;; to change in the second step).
(def network (dv (conj (vec (repeat 17 0)) u)))

;; In the end I want to replace the vectors below with one big matrix,
;; and the constants s/b args to a generation function,
;; but it's easier to start by coding it all piecemeal.

;(defn x0thru8
;  [u net]
;  (let [v (dv [0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0]
;    ;          0  1  2  3  4  5  6  7  8   9   10 11 12 13 14 15 16
;  (

(defn x9+
  [u net]
  (sigma (* 2 u)))

(defn x10+
  [u net]
  (let [v (dv [1 -1  1 -1  1 -1  1 -1  1 c-hat 0  0  0  0  0  0  0])]
    ;          0  1  2  3  4  5  6  7  8   9   10 11 12 13 14 15 16
    (sigma (dot net v))))




;(defn x10+
;  [u c-hat net]
;  (sigma <inner product of network and
;         [1 -1  1 -1  1 -1  1 -1  1 c-hat 0  0  0  0  0  0  0  0]>)
;          (0  1  2  3  4  5  6  7  8   9   10 11 12 13 14 15 16 17)



