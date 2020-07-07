;; Functions for experimenting with ideas in chapter 4 of 
;; Hava Siegelmann's book *Neural Networks and Analog Computation: Beyond 
;; the Turing Limit*
(ns algrand.siegelmann4
    (:require [clojure.math.numeric-tower :as ma]
              [clojure.core.matrix :as mx]
              [utils.convertbase :as cb]))

(mx/set-current-implementation :persistent-vector)
;(mx/set-current-implementation :ndarray)


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
                     (* (ma/expt -1 j)
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
;; using core.matrix

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

;; Encoding of one or more circuits from example 4.1.1 on p.62.
;; This will become an element in the weight matrix.
;; NOTE (4.1), (4.2) p. 62: the circuit s/b encoded backwards here.
(def c-hat 
  (cb/string-to-number
    9 
    (str "0.8" 
         "44444062422204244404442406" ; Ex. 4.1.1: 60424440444240222426044444
         "8"
         "42206"                      ; a single NOT-gate
         "8"
         "44424062422204242404444406" ; like 4.1.1 but swapping ORs, ANDs
    ))) 

;; p. 63: since I have only one circuit in c-hat, u has to contain a single 1.
(def u (mx/array [(cb/string-to-number 2 "0.1")]))

;; u should be on the input line for a single tick.  Add a few zeros on 
;; the front to show that nothing starts before u shows up.  
;; [concat should be as good as lazy-cat here, except maybe if you start
;; with a very long initial repeat sequence.]
(def us (map (fn [y] (mx/array [y]))
             (lazy-cat [0] u (repeat 0))))

;; Initial state of network is all zeros. (Where did HTS say this? ch 3 ?)
(def initial-state (mx/array (repeat 17 0)))

;; From eq 2.2 p19, and p65 - constant vector to be added on each iteration:
(def c (mx/array (concat (range 0 -9 -1) [0 0  0  0  0  -2 0  -1])))
;                            0-8          9 10 11 12 13 14 15 16

;; b is a column matrix to be multiplied by u
;; sigma(2u) is the entire value of x9+, and u is added in x13+:
(def b (mx/transpose
         (mx/matrix [[0  0  0  0  0  0  0  0  0  2  0  0  0  1  0  0  0]])))
;                     0  1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 

(def a (mx/matrix [;0  1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 
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
         ))

(defn next-state-sum
  "See equation (2.2) p. 20.  Where a is the weight matrix for current 
  state vector x, b is the weight matrix for input vector u, and c is 
  a vector of constants, computes xa + ub + c and returns a new vector."
  [a x b u c]
  (mx/add (mx/mmul a x) (mx/mmul b u) c))

(defn next-state
  "See equation (2.2) p. 20.  Computes next state by mapping sigma
  over the result of next-state sum.  Where a is the weight matrix for 
  current state vector x, b is the weight matrix for input vector u, and 
  c is a vector of constants, computes sigma(xa + ub + c) and returns a 
  new state vector x+.  (The previous state vector x is the last argument
  so that you can use partial to easily wrap the constant structures 
  into a function.)"
  [a b c u x]
  (mx/emap sigma
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
  "Given e.g. a core.matrix array x, return a Clojure sequence of
  strings that convert the entries in x into base 9 strings with 
  length len after the decimal point."
  [len x]
  (mx/emap (partial cb/number-to-string 9 len) x))

(defn print9
  "Given e.g. a sequence xs of core.matrix arrays, print to stdout a Clojure 
  sequence of strings that convert the entries in x into base 9 strings with 
  length len after the decimal point."
  [len xs]
  (let [fmtone (str "%" (+ 3 len) "d")
        fmtstr (apply str "   %d" (repeat 16 fmtone))]
    (println (apply format fmtstr (range 17)))
    (map (fn [x] (println (nine-strings len x))) xs)))

;; Convenience function (should be moved elsewhere?)
(defn prmat
  "Prints all elements of matrix m to stdout with precision prec."
  [prec m]
  (print (format "%2d " 0))
  (let [fmtstr (str " %d:% ." prec "f")
        [nrows ncols] (mx/shape m)]
    (doseq [i (range nrows)
            j (range ncols)]
      (when (and (zero? j) (pos? i)) ; (pos? i) test prevents initial newline
        (print (format "\n%2d " i)))
      (print (format fmtstr j (mx/mget m i j)))))
  (println))

;; VERY kludgey:
; (do (println "" (apply str (interpose "      " (range 17)))) (map (partial print9 4) (take 6 sts)))

