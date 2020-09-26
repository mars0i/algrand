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
  (lcg-maker posix-modulus posix-multiplier posix-increment))
