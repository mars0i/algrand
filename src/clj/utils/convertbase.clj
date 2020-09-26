;;;; Functions to convert bases
;;;; 
;;;; Note that Clojure allows integer literals in many bases with e.g.
;;;; 2r101, 9r1234578, 26rabcdefghijklmnop, as well as
;;;; 0x1a for hex and 0123457 for octal.
;;;; However, none of these allow digits after a decimal point.
;;;; The same thing goes for Integer/toString and integer/parseInt.

(ns utils.convertbase
    (:require [clojure.math.numeric-tower :as math]
              [clojure.string :as string]))

(defn log
  "Returns the logarithm of x in base (as a double)."
  [base x]
  (/ (Math/log x) (Math/log base)))

;; Note:
;; It's useful to distinguish between the integer part and the
;; fractional part of a number, because converting the fractional side
;; can go on forever, but the integer side has a finite number of digits.

(defn split-int-fract
  "Return a pair consisting of the integer part (as a bigint) and fractional 
  part of x."
  [x]
  (let [int-part (bigint x)
        fract-part (- x int-part)]
    [int-part fract-part]))
  
(defn convert-int-to-seq
  "Given an integer, returns a sequence of digits (or two-digit numbers, 
  for bases greater than 10) representing the number in the given base."
  [base x]
  (loop [y (bigint x), digits nil]
     (if (zero? y)
       digits
       (recur (quot y base)
              (cons (mod y base) digits)))))

(defn convert-fract-to-seq
  "Given a number x in [0,1), generates a lazy sequence of digits (or 
  two-digit numbers, for bases greater than 10) that would appear after
  the decimal point in a representation in the given base."  
  [base x]
  (lazy-seq 
    (let [shifted-x (* x base)
          [int-part fract-part] (split-int-fract shifted-x)]
      (cons int-part
            (convert-fract-to-seq base fract-part)))))

;; TODO Handle negative numbers
;; Output is float or integer style.  Returning a ratio would have little
;; use, and I have no idea at present of a good way to implment it.
(defn number-to-string
  "Given an integer, ratio, or floating-point number x, returns a float string
  representation of the number in the given base with the specified number of
  digits after the decimal point.  Uses lowercase alphabetic characters for
  digits greater than 9 in bases between 10 and 36."
  [base num-digits x]
  (let [[int-part fract-part] (split-int-fract x)]
    (apply str 
           (concat
             (Integer/toString int-part base)
             ["."]
             (map (fn [n] (Integer/toString n base))
                  (take num-digits
                        (convert-fract-to-seq base fract-part)))))))

;; TODO Handle negative numbers
;; Code has a lot of setup but the actual calculation doesn't need
;; to special-case for int part vs fract part.
(defn float-string-to-number
  "Given a string representation s of an integer or float in the given base, 
  returns a Clojure Ratio or BigInt for the number represented.  Handles bases
  from 2 through 36, with either lowercase or uppercase letters for bases > 10."
  [base s]
  (let [nodot (string/replace s "." "") ; parse float or integer
        nodot-len (count nodot)
        [int-part-len fract-part-len] (split-int-fract
                                        (or (string/index-of s ".") ; if nil dot loc, it's an 
                                            nodot-len))             ; integer string, use length
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
  [base s]
  (let [[numator-s denomator-s] (string/split s #"/")] ; parse ratio string?
    (if denomator-s                            ; nil if no slash
      (/ (float-string-to-number base numator-s) 
         (float-string-to-number base denomator-s))
      (float-string-to-number base s))))

(defn string-to-double
  "Like string-to-number, but returns a double rather than a Ratio."
  [base s]
  (double (string-to-number base s)))

;; TODO revise docstring
;;
;; Note first-exponent is one less than number of integer digits because
;; We multiply each digit by base to an exponent, one less each time,
;; where the last digit isn't multiplied at all, or rather it's multiplied
;; by (expt base 0).
(defn sum-digits
  "ADD DOCSTRING"
  [base integer-digits fractional-digits]
  (let [first-exponent (dec (count integer-digits)) ; what exponent do they start at?
        digits (concat integer-digits fractional-digits)
        [_ x] (reduce (fn [[m sum] digit] [(/ m base) (+ sum (* m digit))])  ; m records the current exponent, but we don't need it when we're done
                      [(math/expt base first-exponent) 0] ; start at exponent of first integer digit
                      digits)]
    x))

(defn cantor-code-digits
  "Given a sequence of natural-digits that can be represented as digits in 
  some base (e.g. 16 is the 17th digit in base 27), return a sequence of
  \"cantorized\" digits that are one more than twice the original digit
  (0->1, 1->3, 2->5, etc.)."
  [xs]
  (map (fn [digit] (inc (* 2 digit)))
       xs))

(defn cantor-decode-digits
  "Given a sequence of natural-digits that can be represented as digits in 
  some base (e.g. 16 is the 17th digit in base 27), return a sequence of
  \"cantorized\" digits that are one more than twice the original digit
  (0->1, 1->3, 2->5, etc.)."
  [xs]
  (map (fn [digit] (/ (dec digit) 2))
       xs))

(defn cantor-code-digit
  "Cantor-code a single digit, i.e. multiply by 2 and add 1."
  [digit]
  (inc (* 2 digit)))

(defn cantor-decode-digit
  [digit]
  "Cantor-decode a single digit, i.e. subtract 1 and divide by 2."
  (/ (dec digit) 2))

(defn cantor-code
  "Convert a number x, considered to be in base natural-base, to a 
  antor-coded analog of the original number in base cantor-base, restricted
  to num-fract-digits .  Note that the number returned is *not*, in general,
  equal to the original number x.  It is a new number that is such that, if
  you convert it to a base cantor-base representation of the original base
  natural-base number (e.g. using number-to-string), the result will
  be the cantor-coded representation of the original base natural-base
  representation."
  [natural-base cantor-base num-fract-digits x]
  (let [[int-part fract-part] (split-int-fract x)]
    (sum-digits cantor-base 
                (cantor-code-digits
                  (convert-int-to-seq natural-base int-part))
                (take num-fract-digits  
                      (cantor-code-digits
                        (convert-fract-to-seq natural-base fract-part))))))


(defn cantor-decode
  [natural-base cantor-base num-fract-digits x]
  (let [[int-part fract-part] (split-int-fract x)]
    (sum-digits cantor-base 
                (cantor-decode-digits
                  (convert-int-to-seq natural-base int-part))
                (take num-fract-digits  
                      (cantor-decode-digits
                        (convert-fract-to-seq natural-base fract-part))))))
