;;;; Functions to convert bases
(ns utils.convertbase
    (:require [clojure.math.numeric-tower :as math]
              [clojure.string :as string]))

(defn convert-fract-seq
  "Given a number x in [0,1), generates a lazy sequence of digits (or 
  two-digit numbers, for bases greater than 10) that would appear after
  the decimal point in a representation in the given base."  
  [x base]
  (lazy-seq 
    (let [shifted-x (* x base)
          int-part (bigint shifted-x) ; int, bigint :ound toward zero
          fract-part (- shifted-x int-part)]
      (cons int-part
            (convert-fract-seq fract-part base)))))

;; Output is float or integer style.  Returning a ratio would have little
;; use, and I have no idea at present of a good way to implment it.
(defn number-to-string
  "Given an integer, ratio, or floating-point number x, returns a float string
  representation of the number in the given base with the specified number of
  digits after the decimal point.  Uses lowercase alphabetic characters for
  digits greater than 9 in bases between 10 and 36."
  [x base digits]
  (let [int-part (bigint x) ; int, bigint round toward zero
        fract-part (- x int-part)]
    (apply str 
           (concat
             (Integer/toString int-part base)
             ["."]
             (map (fn [n] (Integer/toString n base))
                  (take digits
                        (convert-fract-seq fract-part base)))))))

;; Code has a lot of setup but the actual calculation doesn't need
;; to special-case for int part vs fract part.
(defn float-string-to-number
  "Given a string representation s of an integer or float in the given base, 
  returns a Clojure Ratio or BigInt for the number represented.  Handles bases
  from 2 through 36, with either lowercase or uppercase letters for bases > 10."
  [s base]
  (let [nodot (string/replace s "." "") ; parse float or integer
        nodot-len (count nodot)
        int-part-len (or (string/index-of s ".") ; if nil dot loc, it's an 
                         nodot-len)              ; integer string, use length
        fract-part-len (- nodot-len int-part-len) 
        nums (map (fn [n] (bigint (Integer/parseInt n base))) ; w/base: letters
                  (string/split nodot #""))
        exponents (range (dec int-part-len)       ; dec: 1's place has expt 0
                         (dec (- fract-part-len)) ; dec: range to before bound
                         -1)
        components (map (fn [x e] (* x (math/expt base e)))
                        nums exponents)]
    (reduce + components)))

(defn string-to-number
  "Given a string representation s of an integer, float, or ratio in the 
  given base, returns a Clojure Ratio or BigInt for the number represented.
  Handles bases from 2 through 36, with either lowercase or uppercase letters
  for bases > 10."
  [s base]
  (let [[numator-s denomator-s] (string/split s #"/")] ; parse ratio string?
    (if denomator-s                            ; nil if no slash
      (/ (float-string-to-number numator-s base) 
         (float-string-to-number denomator-s base))
      (float-string-to-number s base))))

(defn string-to-double
  "Like string-to-number, but returns a double rather than a Ratio."
  [x base]
  (double (string-to-number x base)))

;;;;;;;;;;;;;;;;;;;;;;;;;
;; OTHER VERSIONS:

; (defn div-with-rem
;   "Returns a pair consisting of the integer quotient of dividend/divisor
;   followed by divident mod divisor."
;   [dividend divisor]
;   [(quot dividend divisor) (mod dividend divisor)])

; ;; No reason to make it lazy; integer parts are always finite.
; (defn convert-int-seq
;   "Given a number x >= 0, generates a sequence of digits (or two-digit 
;   numbers, for bases greater than 10) that would appear in a representation 
;   of the number in the given base."  
;   [x base]
;   (if (< base x)
;     (let [[quotient remainder] (div-with-rem x base)]
;       (conj
;         (convert-int-seq quotient base)
;         remainder))
;     [x]))

; (defn num-to-char
;   "Converts integers in [10,35] to corresponding characters.  Which
;   characters is determined by base-int (usually 55 or 87), which should 
;   be 10 less than the lowest character to generate (65 or 97).
;   Integers outside the range [10,35] are returned as is."
;   [base-int n]
;     (if (and (>= n 10) (<= n 35))
;       (char (+ base-int n))
;       n))
; 
; (defn num-to-lowercase
;   "Converts integers in [10,35] to corresponding lowercase letter characters,
;   mapping, for example, 10 to a, 11, to b, and 35 to z.  Integers outside
;   of this range are returned as is."
;   [n]
;   (num-to-char 87 n))
; 
; (defn num-to-uppercase
;   "Converts integers in [10,35] to corresponding lowercase letter characters,
;   mapping, for example, 10 to A, 11, to B, and 35 to Z.  Integers outside
;   of this range are returned as is."
;   [n]
;   (num-to-char 55 n))


; (defn parse-int
;   "Function wrapper for Java method Integer/parseInt."
;   [s]
;   (Integer/parseInt s))

; ;; Maybe reorg first few lines of let, mapping parse-int once.
; ;; FIXME NPE if no fract part
; ;; TODO parse hexadecimal, etc.
; (defn convert-string-1
;   "Given a string representation s of a number in the given base (possibly 
;   with a fractional part after the decimal point), returns a Clojure
;   number represented.  This is a Ratio by default.  If the third argument, 
;   use-double, is provided and is truthy, a double is returned instead."
;   [s base & [use-double]]
;   (let [[int-string fract-string] (string/split s #"\.")
;         int-xs   (map parse-int (string/split int-string #""))
;         fract-xs (map parse-int (string/split fract-string #""))
;         int-part   (reduce (fn [acc n] (+ n (* acc base)))
;                            0 int-xs)
;         fract-part (reduce (fn [acc n] (/ (+ n acc) base))
;                            0 (reverse fract-xs))
;         result (+ int-part fract-part)]
;     (if use-double
;       (double result)
;       result)))
; 
; ;; FIXME NPE if no fract part
; ;; TODO parse hexadecimal, etc.
; (defn convert-string-2
;   "Given a string representation s of a number in the given base (possibly 
;   with a fractional part after the decimal point), returns a Clojure
;   Ratio for the number represented."
;   [s base]
;   (let [[int-string fract-string] (string/split s #"\.")
;         int-part (Integer/parseInt int-string base)
;         fract-xs (map parse-int (string/split fract-string #""))
;         fract-part (reduce (fn [acc n] (/ (+ n acc) base))
;                            0 (reverse fract-xs))]
;     (+ int-part fract-part)))
