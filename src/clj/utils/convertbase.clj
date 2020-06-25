;;;; Functions to convert bases
(ns utils.convertbase
    (:require [clojure.math.numeric-tower :as m]
              [clojure.string :as s]))

(defn div-with-rem
  [x div]
  [(quot x div) (mod x div)])

(defn num-to-char
  "Converts integers in [10,35] to corresponding characters.  Which
  characters is determined by base-int (usually 55 or 87), which should 
  be 10 less than the lowest character to generate (65 or 97).
  Integers outside the range [10,35] are returned as is."
  [base-int n]
    (if (and (>= n 10) (<= n 35))
      (char (+ base-int n))
      n))

(defn num-to-lowercase
  "Converts integers in [10,35] to corresponding lowercase letter characters,
  mapping, for example, 10 to a, 11, to b, and 35 to z.  Integers outside
  of this range are returned as is."
  [n]
  (num-to-char 87 n))

(defn num-to-uppercase
  "Converts integers in [10,35] to corresponding lowercase letter characters,
  mapping, for example, 10 to A, 11, to B, and 35 to Z.  Integers outside
  of this range are returned as is."
  [n]
  (num-to-char 55 n))

;; Only for numbers in [0,1) and bases <= 36.
(defn convert-fract-seq
  "Given a number x in [0,1), generates a lazy sequence of digits (or 
  two-digit numbers, for bases greater than 10) that would appear after
  the decimal point in a representation in the given base."  
  [x base]
  (lazy-seq 
    (let [shifted-x (* x base)
          int-part (m/floor shifted-x)
          fract-part (- shifted-x int-part)]
      (cons (int int-part)
            (convert-fract-seq fract-part base)))))

;; No reason to make it lazy; integer parts are always finite.
(defn convert-int-seq
  "Given a number x >= 0, generates a sequence of digits (or two-digit 
  numbers, for bases greater than 10) that would appear in a representation 
  of the number in the given base."  
  [x base]
  (if (< base x)
    (let [[quotient remainder] (div-with-rem x base)]
      (conj
        (convert-int-seq quotient base)
        remainder))
    [x]))

;; Only positive numbers and bases <= 36.
(defn convert-number
  "Given a float or rational number x, returns a string representation of the 
  number in the given base with the specified number of digits after the 
  decimal point.  Uses lowercase alphabetic characters for bases greater than 
  10 and less than 37.  If the fourth argument exists and is not nil or false,
  internal operations use Clojure Ratio numbers for the fractional part, which 
  are slower but avoid floating-point errors."
  [x base digits & [ratl]]
  (let [x' (if ratl (rationalize x) x)
        int-part (bigint (m/floor x'))
        fract-part (- x' int-part)]
    (apply str 
           (concat
             (map num-to-lowercase
                  (convert-int-seq int-part base))
             ["."]
             (map num-to-lowercase
                  (take digits
                        (convert-fract-seq fract-part base)))))))
