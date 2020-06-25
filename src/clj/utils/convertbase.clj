;;;; Functions to convert bases
(ns utils.convertbase
    (:require [clojure.math.numeric-tower :as math]
              [clojure.string :as string]))

(defn div-with-rem
  "Returns a pair consisting of the integer quotient of dividend/divisor
  followed by divident mod divisor."
  [dividend divisor]
  [(quot dividend divisor) (mod dividend divisor)])

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
          int-part (math/floor shifted-x)
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
  "Given a float or rational number x, returns a string representation of
  the number in the given base with the specified number of digits after
  the decimal point.  Uses lowercase alphabetic characters for bases
  greater than 10 and less than 37.  By default uses Clojure Ratio numbers
  internally.  If the fourth argument use-double exists and is truthy,
  the function uses doubles internally instead."
  [x base digits & [use-double]]
  (let [x' (if use-double (double x) (rationalize x))
        int-part (bigint (math/floor x')) ; coerce away ".0", might as well use BigInt rather than Integer
        fract-part (- x' int-part)]
    (apply str 
           (concat
             (map num-to-lowercase
                  (convert-int-seq int-part base))
             ["."]
             (map num-to-lowercase
                  (take digits
                        (convert-fract-seq fract-part base)))))))


(defn parse-int
  "Function wrapper for Java method Integer/parseInt."
  [s]
  (Integer/parseInt s))

;; Note the reduce fns for int-part and fract-part have different forms 
;; because ;; left of decimal point, the first digit is unmultiplied, but
;; right of the point, even the first digit is in units of 1/base.
(defn convert-string
  "Given a string representation s of a number in the given base (possibly 
  with a fractional part after the decimal point), returns a Clojure
  number represented.  This is a Ratio by default.  If the third argument, 
  use-double, is provided and is truthy, a double is returned instead."
  [s base & [use-double]]
  (let [[int-string fract-string] (string/split s #"\.")
        int-xs   (map parse-int (string/split int-string #""))
        fract-xs (map parse-int (string/split fract-string #""))
        int-part   (reduce (fn [acc n] (+ n (* acc base)))
                           0 int-xs)
        fract-part (reduce (fn [acc n] (/ (+ n acc) base))
                           0 (reverse fract-xs))
        result (+ int-part fract-part)]
    (if use-double
      (double result)
      result)))
