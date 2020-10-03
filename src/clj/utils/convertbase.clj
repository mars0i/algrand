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
  "Given an (big)integer, ratio, or floating-point number x, returns a float 
  string representation of the number in the given base with the specified 
  number of digits after the decimal point.  Uses lowercase alphabetic 
  characters for digits greater than 9 in bases between 10 and 36."
  [base num-digits x]
  (let [[int-part fract-part] (split-int-fract x)]
    (string/join
      (concat
        (map (fn [n] (Integer/toString n base)) ; don't apply to int-part directly, so bigints are handled too
             (convert-int-to-seq base int-part))
        ["."]
        (map (fn [n] (Integer/toString n base))
             (take num-digits
                   (convert-fract-to-seq base fract-part)))))))

;; Convenience abbreviations:
(def base2 (partial number-to-string 2))
(def base4 (partial number-to-string 4))

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

;; Note first-exponent is one less than number of integer digits because
;; We multiply each digit by base to an exponent, one less each time,
;; where the last digit isn't multiplied at all, or rather it's multiplied
;; by (expt base 0).
(defn sum-digits
  "Given a sequence of digits representing an integer in the given base, and 
  a sequence of digits representing a fractional part of a number in the same
  base, return the represented number."
  [base integer-digits fractional-digits]
  (let [first-exponent (dec (count integer-digits)) ; what exponent do they start at?
        digits (concat integer-digits fractional-digits)
        [_ x] (reduce (fn [[m sum] digit] [(/ m base) (+ sum (* m digit))])  ; m records the current exponent, but we don't need it when we're done
                      [(math/expt base first-exponent) 0] ; start at exponent of first integer digit
                      digits)]
    x))

(defn cantor-code-digit-fn
  "Return a function that cantor-codes a single digit, multiplying by 2 and 
  adding offset."
  [offset]
  (fn [digit]
      (+ offset (* 2 digit))))

(defn cantor-decode-digit-fn
  [offset]
  "Return a function that cantor-decodes a single digit, subtracting offset
  and dividing by 2."
  (fn [digit]
      (/ (- digit offset) 2)))

;; contains common code for cantor-code and cantor-decode
(defn cantor-convert 
  "Convert a number x, considered to be in base from-base, to a cantor-coded
  or decoded analog of the original number in base to-base, restricted to 
  num-fract-digits.  Whether this performs cantor-coding or decoding
  depends on the function digit-converter that's passed in.  Note that the 
  number returned is *not*, in general, equal to the original number x.  It
  is a new number that is the analogous representation of the old number in 
  the new base."
  [digit-converter from-base to-base num-fract-digits x]
  (let [[int-part fract-part] (split-int-fract x)]
    (sum-digits to-base 
                (map digit-converter
                     (convert-int-to-seq from-base int-part))
                (take num-fract-digits  
                      (map digit-converter
                           (convert-fract-to-seq from-base fract-part))))))

;; Wrappers for cantor-convert:

(defn cantor-code-0
  "Convert a number x, considered to be in base natural-base, to a 
  antor-coded analog 2x of the original number in base cantor-base, 
  restricted to num-fract-digits.  See cantor-convert for more info."
  [natural-base cantor-base num-fract-digits x]
  (cantor-convert (cantor-code-digit-fn 0) natural-base cantor-base
                  num-fract-digits x))

;; wrapper for cantor-convert
(defn cantor-decode-0
  "Convert a number x, considered to be in base cantor-base, to a 
  decoded analog x/2 of the original number in base natural-base, 
  restricted to num-fract-digits.  See cantor-convert for more info."
  [cantor-base natural-base num-fract-digits x]
  (cantor-convert (cantor-decode-digit-fn 0) cantor-base natural-base
                  num-fract-digits x))


(defn cantor-code-1
  "Convert a number x, considered to be in base natural-base, to a 
  antor-coded analog 2x+1 of the original number in base cantor-base, 
  restricted to num-fract-digits.  See cantor-convert for more info."
  [natural-base cantor-base num-fract-digits x]
  (cantor-convert (cantor-code-digit-fn 1) natural-base cantor-base
                  num-fract-digits x))

;; wrapper for cantor-convert
(defn cantor-decode-1
  "Convert a number x, considered to be in base cantor-base, to a 
  decoded analog (x-1)/2 of the original number in base natural-base, 
  restricted to num-fract-digits.  See cantor-convert for more info."
  [cantor-base natural-base num-fract-digits x]
  (cantor-convert (cantor-decode-digit-fn 1) cantor-base natural-base
                  num-fract-digits x))

;; Doesn't handle fractional
(defn cantor0+
  "Given zero-based Cantor-coded numbers in the specified base, returns a
  number that's the Cantor-coded representation of the sum of the original 
  numbers that had been transformed by Cantor-coding."
  ([base x y]
   (let [xs (reverse (cb/convert-int-to-seq base x)) ; reverse to start from less
         ys (reverse (cb/convert-int-to-seq base y)) ;  significant so carry works
         xys (padded-pairs xs ys)
         [sums final-carry] (reduce (sum-digits-with-carry-fn base) [[] 0N] xys)
         sums (if (pos? final-carry)
                (conj sums final-carry) ; final-carry is first digit of result
                sums)]                  ; unless it's zero
     (cb/sum-digits base (reverse sums) [])))
  ([base x y & zs] (reduce (partial cantor-+ base)
                           (cantor-+ base x y)
                           zs)))
