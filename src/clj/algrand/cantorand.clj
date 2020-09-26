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
                   (fn [oldval] (mod (+ (* oldval multiplier) increment)
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

(def cantor-posix-modulus (cb/cantor-code 10 20 0 (ma/expt 2N 32N)))
(def cantor-posix-multiplier (cb/cantor-code 10 20 0 25214903917N))
(def cantor-posix-increment (cb/cantor-code 10 20 0 11N))

;; FIXME
;; user=> (def plcg (posix-lcg 324))
;; #'user/plcg
;; user=> (def lcg (cantor-posix-lcg 324))
;; #'user/lcg
;; user=> (plcg)
;; 601072127N
;; user=> (lcg)
;; 3013462395495N
;; user=> (number-to-string 10 0 601072127N)
;; "601072127."
;; user=> (number-to-string 20 0 3013462395495N)
;; Execution error (IllegalArgumentException) at utils.convertbase/number-to-string (convertbase.clj:63).
;; Value out of range for int: 3013462395495
(def cantor-posix-lcg 
  "Given a seed considered to be cantor-coded in base 20, returns an integer 
  LCG function using cantor-coded Posix parameters."
  (lcg-maker cantor-posix-modulus 
             cantor-posix-multiplier 
             cantor-posix-increment))

