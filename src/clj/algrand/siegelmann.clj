;; Functions for experimenting with ideas in Hava Siegelmann's book
;; *Neural Networks and Analog Computation: Beyond the Turing Limit*
(ns algrand.siegelmann
    (:require [clojure.math.numeric-tower :as math]
              [utils.convertbase :as base])
    (:use [uncomplicate.neanderthal.core :only [dot mm mv xpy trans
                                                mrows ncols]]
          [uncomplicate.neanderthal.native :only [dv dge]]
          [uncomplicate.fluokitten.core :only [fmap]]))

;; convenience abbreviations
;(def n2s base/number-to-string)
;(def s2n base/string-to-number)

;; my Neanderthal convenience function (should be moved elsewhere)
;; You can also do e.g. this (per Dragan):
;;   (uncomplicate.neanderthal.internal.printing/printer-settings! 
;;     {:matrix-width 17 :matrix-height 17})
(defn prmat
  "Prints all elements of matrix m to stdout with precision prec."
  [m prec]
  (let [fmtstr (str " % ." prec "f")]
    (doseq [i (range (mrows m))
            j (range (ncols m))]
      (when (and (zero? j) (pos? i)) ; (pos? i) test prevents initial newline
        (println))
      (print (format fmtstr (m i j)))))
  (println))


;; for all chapters
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
(def c-hat (base/string-to-number 9 "0.860424440444240222426044444"))

;; p. 63: since I have only one circuit in c-hat, u has to contain a single 1.
(def u (dv [(base/string-to-number 2 "0.1")]))

;; u should be on the input line for a single tick.  Add a few zeros on 
;; the front to show that nothing starts before u shows up.  
;; [concat should be as good as lazy-cat here, except maybe if you start
;; with a very long initial repeat sequence.]
(def us (map (fn [y] (dv [y])) (lazy-cat (repeat 5 0) u (repeat 0))))

;; Initial state of network is all zeros. (Where did HTS say this? ch 3 ?)
(def initial-state (dv 17))

;; From eq 2.2 p19, and p65 - constant vector to be added on each iteration:
(def c (dv (concat (range 0 -9 -1) [0 0  0  0  0  -2 0  -1])))
;                      0-8          9 10 11 12 13 14 15 16

;; sigma(2u) is the entire value of x9+, and u is added in x13+:
;; Note this is a 1-column matrix.
(def b (dge 17 1 [0  0  0  0  0  0  0  0  0  2  0  0  0  1  0  0  0]))
;                0  1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 

;; Note that Neanderthal vectors look like row vectors but behave as 
;; column vectors, e.g. in the mv multiplication operator, the vector
;; is the second arg.  That means the node indexes are column indexes
;; in the weight matrix.  However, I'd rather see it the other way
;; when defining the matrix, so I'll just transpose what I input:
(def a (trans
         (dge [;0  1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 
               [0  0  0  0  0  0  0  0  0  0  9  0  0  0  0  0  0] ; x0
               [0  0  0  0  0  0  0  0  0  0  9  0  0  0  0  0  0] ; x1
               [0  0  0  0  0  0  0  0  0  0  9  0  0  0  0  0  0] ; x2
               [0  0  0  0  0  0  0  0  0  0  9  0  0  0  0  0  0] ; x3
               [0  0  0  0  0  0  0  0  0  0  9  0  0  0  0  0  0] ; x4
               [0  0  0  0  0  0  0  0  0  0  9  0  0  0  0  0  0] ; x5
               [0  0  0  0  0  0  0  0  0  0  9  0  0  0  0  0  0] ; x6
               [0  0  0  0  0  0  0  0  0  0  9  0  0  0  0  0  0] ; x7
               [0  0  0  0  0  0  0  0  0  0  9  0  0  0  0  0  0] ; x8
               [0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0] ; x9
               [1 -1  1 -1  1 -1  1 -1 1 c-hat 0 0  0  0  0  0  0] ; x10
               [0 2/9 0 2/9 0 2/9 0 2/9 0  0  0  0 1/9 -2 0  0  0] ; x11
               [0  0  0  0  0  0  0  0  0  0  0  1  0  0  0  0  0] ; x12
               [0  0  0  0  0  0  0  0  0  0  0  0  0  0  1  1  0] ; x13
               [0  0  0  0  0  0  0  1  0  0  0  0  0  2  0  0  0] ; x14
               [0  0  0  0  0  0  0 -1  0  0  0  0  0  1  0  0  0] ; x15
               [0  0  0  0  0  0  0  1  0  0  0  0  1  0  0  0  0] ; x16
              ];0  1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 
         )))

(defn next-state-sum
  "See equation (2.2) p. 20.  Where a is the weight matrix for current 
  state vector x, b is the weight matrix for input vector u, and c is 
  a vector of constants, computes xa + ub + c and returns a new vector.
  (Note vectors function as column vectors though displayed as rows.)"
  [a x b u c]
  (xpy (mv a x) (mv b u) c))

(defn next-state
  "See equation (2.2) p. 20.  Computes next state by mapping sigma
  over the result of next-state sum.  Where a is the weight matrix for 
  current state vector x, b is the weight matrix for input vector u, and 
  c is a vector of constants, computes sigma(xa + ub + c) and returns a 
  new state vector x+.  (The previous state vector x is the last argument
  so that you can use partial to easily wrap the constant structures 
  into a function.  Note vectors function as column vectors though 
  displayed as rows.)"
  [a b c u x]
  (fmap sigma
        (next-state-sum a x b u c)))

(defn make-states
  "Return a lazy sequence of states from us, a Clojure sequence of input
  vectors, x, and initial state vector, a and b, weight matrices for x
  and u, respectively, and constant vector c."
  [a b c us x]
  (lazy-seq
    (let [x+ (next-state a b c (first us) x)]
      (cons x+ (make-states a b c (rest us) x+)))))

(defn nine-strings
  "Given e.g. a Neanderthal vector x, return a Clojure sequence of
  strings that convert the entries in x into base 9 strings with 
  length len after the decimal point."
  [len x]
  (map (partial base/number-to-string 9 len)
       (into [] x))) ; convert Neanderthal vector to Clojure vector
