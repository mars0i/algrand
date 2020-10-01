;; experiments with Cantor-coded random number generation
(ns algrand.cantorand
    (:require [clojure.math.numeric-tower :as ma]
              [utils.convertbase :as cb]))

(defn lcg-maker
  "Returns a function which, given a seed, will return an integer LCG
  function using the modulus, multiplier, and increment."
  [modulus multiplier increment]
  (fn [seed]
      (let [newval$ (atom seed)]
        (fn []
            (swap! newval$ 
                   (fn [oldval] (mod (+ increment (* oldval multiplier))
                                     modulus)))))))

(def posix-modulus (ma/expt 2N 32N))
(def posix-multiplier 25214903917N)
(def posix-increment 11N)

;; From Wikipedia Linear Congruential Generator.  Posix parameters, 
;; Java.util.Random, but these in seed sizes and bits from this one.
;; Divide by 2^32.0 to get floats in [0,1).
(def posix-lcg 
  "Given a seed, returns an integer LCG function using Posix parameters."
  (lcg-maker posix-modulus
             posix-multiplier
             posix-increment))



(def cantor-posix-modulus (cb/cantor-code-0 10 20 0 posix-modulus))
(def cantor-posix-multiplier (cb/cantor-code-0 10 20 0 posix-multiplier))
(def cantor-posix-increment (cb/cantor-code-0 10 20 0 posix-increment))

;; This doesn't work.  It's not going to work. cantorcoding.md explains why.
(def bad-cantor-posix-lcg 
  "Given a seed considered to be cantor-coded in base 20, returns an integer 
  LCG function using cantor-coded Posix parameters."
  (lcg-maker cantor-posix-modulus 
             cantor-posix-multiplier 
             cantor-posix-increment))

;;;;;;;;;;;;;;;;;;

(defn padded-pairs
  "Given two finite sequences of elements, returns a sequence of pairs of
  their elements after the shorter sequence is padded with zeros to make their
  lengths equal."
  [xs ys]
  (let [[xs' ys'] (if (> (count xs) (count ys)) ; pad shorter seq with 0's
                    [xs (concat ys (repeat 0))]
                    [(concat xs (repeat 0)) ys])]
    (map vector xs' ys')))

(defn sum-digits-with-carry-fn
  "Given numeric base, returns a function for use with reduce, that accepts a pair
  containing a sequence of sums so far, and the current carry value, and a pair 
  containing two digits to be summed.  The function returns a pair containing 
  the new sum (mod base) conj'ed onto the end of the sequence of sums, and a new
  doubled carry.  (The returned carry is double what it would be naturally because
  the intended use is for zero-based Cantor coding.)"
  [base]
  (fn [[sums carry] [x y]]
      (let [tot (+ x y carry)]
        [(conj sums (mod tot base))
         (* 2 (quot tot base))]))) ; doubled carry for Cantor coding

;; assumes zero-based Cantor coding
;; doesn't handle fractional
(defn cantor-+
  [base x y]
  (let [xs (reverse (cb/convert-int-to-seq base x)) ; reverse to start from less
        ys (reverse (cb/convert-int-to-seq base y)) ;  significant so carry works
        xys (padded-pairs xs ys)
        [sums _] (reduce (sum-digits-with-carry-fn base)  [[] 0]  xys)]
    (cb/sum-digits base (reverse sums) [])))

;i want 
;  2 + 0 = 2, 0
;
;1+1 = 10, i.e. 0, carry 1
;  2 + 2 = 2 0, i.e. 0, carry 2
;
;ie curr digit is mod x y
;carry is quot x y , * 2 because we're mapping a single carry digit to 
;its double, since it's Cantor-coding



(defn OLD-cantor-+
  [base x y]
  (let [x-seq' (cb/convert-int-to-seq base x)
        y-seq' (cb/convert-int-to-seq base y)
        x-len (count x-seq')
        y-len (count y-seq')
        xy-diff (- x-len y-len)
        ; normalize lengths:
        [x-seq y-seq] (cond (zero? xy-diff) [x-seq' y-seq']
                            (pos?  xy-diff) [x-seq' (concat (repeat xy-diff 0) y-seq')]
                            :else           [(concat (repeat (- xy-diff) 0) x-seq') y-seq'])
        _ (println (count x-seq) (count y-seq))
        xy-seq (map vector x-seq y-seq)
        [sum-seq _] (reduce (fn [[sums carry] [x' y']]
                                (let [tot (+ x' y' carry)]
                                  [(conj sums (* 2 (quot tot base)))  ; TODO IS THIS RIGHT?
                                   (mod tot base)]))           ; TODO IS THIS RIGHT?
                            [[] 0] xy-seq)]
  (cb/sum-digits base sum-seq [])))



