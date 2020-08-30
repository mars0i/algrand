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

(defn convert-fract-seq
  "Given a number x in [0,1), generates a lazy sequence of digits (or 
  two-digit numbers, for bases greater than 10) that would appear after
  the decimal point in a representation in the given base."  
  [base x]
  (lazy-seq 
    (let [shifted-x (* x base)
          int-part (bigint shifted-x) ; int, bigint :ound toward zero
          fract-part (- shifted-x int-part)]
      (cons int-part
            (convert-fract-seq base fract-part)))))

;; TODO Handle negative numbers
;; Output is float or integer style.  Returning a ratio would have little
;; use, and I have no idea at present of a good way to implment it.
(defn number-to-string
  "Given an integer, ratio, or floating-point number x, returns a float string
  representation of the number in the given base with the specified number of
  digits after the decimal point.  Uses lowercase alphabetic characters for
  digits greater than 9 in bases between 10 and 36."
  [base num-digits x]
  (let [int-part (bigint x) ; int, bigint round toward zero
        fract-part (- x int-part)]
    (apply str 
           (concat
             (Integer/toString int-part base)
             ["."]
             (map (fn [n] (Integer/toString n base))
                  (take num-digits
                        (convert-fract-seq base fract-part)))))))

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

;; TODO very broken: always returns 5/4.
(defn cantor-code
  "Given a number x (preferably a Ratio) extract digits from it in
  base natural-base, and encode them in a Ratio in cantor-base, using
  only alternating cantor-base digits, up to num-digits.  cantor-base 
  must be at least twice natural-base.  x should be non-negative."
  [natural-base cantor-base num-digits x]
  (loop [n-digs num-digits
         y (bigint x)
         acc 0]
        (if (or (zero? n-digs) (zero? y))
          acc
          (let [n (quot y natural-base)
                r (rem y natural-base)
                cantor-digit (/ (inc (* 2 n)) cantor-base)] ; 0->1, 1->3, 2->5, etc.
            (recur (dec n-digs) r (+ cantor-digit acc))))))
