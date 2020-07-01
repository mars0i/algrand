;; Functions for experimenting with ideas in Hava Siegelmann's book
;; *Neural Networks and Analog Computation: Beyond the Turing Limit*
(ns algrand.siegelmann
    (:require [clojure.math.numeric-tower :as math]
              [utils.convertbase :as base])
    (:use [uncomplicate.neanderthal.core :only [dot mm mv xpy]]
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
;; using Neanderthal
;;
;; See Siegelman:
;;    Equation (2.2), p. 19
;;       Here there is a single, unweighted input line on which u appears 
;;       briefly, so M=1, and the b matrix is a vector.
;;       N=17 apparently: In the statement of lemma 4.1.2 on p.63, HTS says
;;       you need a 16-node net, but in the model on p.65, there are nodes 
;;       0 through 16, i.e. 17 nodes.
;;    Equation (2.5), p. 21
;;    Text following lemma 4.1.2, p. 63 (the following is part of the proof)
;;    Retrieval network equations on p. 65

;; Encoding of single circuit (s/b many) from example 4.1.1 on p.62.
;; (This will become an element in the weight matrix.)
(def c-hat (base/string-to-number "0.860424440444240222426044444" 9))

;; p. 63: since I have only one circuit in c-hat, u has to contain a single 1.
(def u (base/string-to-number "0.1" 2))

;; Initial state of network: all zeros. (Where did HTS say this?  I forget.)
(def network (dv 17))

;; From eq 2.2 p19, and p65 - constant vector to be added on each iteration:
(def c (dv (concat (range 0 -9 -1) [0 0  0  0  0  -2 0  -1])))
;                      0-8          9 10 11 12 13 14 15 16

;; sigma(2u) is the entire value of x9+, and u is added in x13+:
(def b (dv [0  0  0  0  0  0  0  0  0  2  0  0  0  1  0  0  0]))
;           0  1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 

;; could use a macro I suppose
(defn x0thru8-maker
  "Given index i, returns a function to implment x0+ through x8+."
  [i]
  (fn [net]
      (let [v  (dv [0 0 0 0 0 0 0 0 0 0  1      0  0  0  0  0  0  0])
            iv (dv [0 0 0 0 0 0 0 0 0 0  (- i)  0  0  0  0  0  0  0])]
        ;          0 1 2 3 4 5 6 7 8  9  10     11 12 13 14 15 16 u
        (sigma
          (xpy (dot net v) iv)))))

(def x0+ (x0thru8-maker 0))
(def x1+ (x0thru8-maker 1))
(def x2+ (x0thru8-maker 2))
(def x3+ (x0thru8-maker 3))
(def x4+ (x0thru8-maker 4))
(def x5+ (x0thru8-maker 5))
(def x6+ (x0thru8-maker 6))
(def x7+ (x0thru8-maker 7))
(def x8+ (x0thru8-maker 8))

(defn x9+
  [net]
  (let [v (dv [0 0 0 0 0 0 0 0 0 0  0  0  0  0  0  0  0  2])]
    ;          0 1 2 3 4 5 6 7 8  9 10 11 12 13 14 15 16 u
    (sigma (dot net v))))

(defn x10+
  [net]
  (let [v (dv [1 -1  1 -1  1 -1  1 -1  1 c-hat 0  0  0  0  0  0  0  0])]
    ;          0  1  2  3  4  5  6  7  8   9   10 11 12 13 14 15 16 u
    (sigma (dot net v))))

;; toadd: x11+

(defn x12+
  [net]
  (let [v (dv [0 0 0 0 0 0 0 0 0 0 0  1  0  0  0  0  0  0])]
    ;          0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 u
    (sigma (dot net v))))

(defn x13+
  [net]
  (let [v (dv [0 0 0 0 0 0 0 0 0 0 0  0  0  0  1  1  0  1])]
    ;          0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 u
    (sigma (dot net v))))
