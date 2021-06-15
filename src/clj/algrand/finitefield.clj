(ns algrand.finitefield
    (:require [clojure.math.numeric-tower :as nt]))

;; For now, polynomials are Clojure sequences of integers (preferably
;; vectors) that are elements of a prime field, with larger exponents on 
;; the left.  That means you can't read off exponents from degrees,
;; but it also means that e.g. with long division, you don't have to
;; count backwards all the time, which prevents some Clojure
;; conveniences unless you keep reversing vectors.
;; TODO: Generalize to other subfields than prime fields.


;; Some polynomials for testing:

;; polynomials over F2 or F3:
(def poly1 [1 1 0 1 0 0 1 1])
(def poly2 [0 1 1 1 0 1 1 0])

;; polynomials over F3:
(def poly3 [2 1 2 1 0 1 2 2])
(def poly4 [1 0 0 1 0 2 0 0])


(defn strip-high-zeros
  "Strip initial zeros from a sequence of coefficients.  Length of the
  remaining sequence is one more than the degree of the polynomial."
  [p]
  (drop-while zero? p))

(defn pad-high-zeros
  "If sequence p is shorter than minimum-length, concatenate initial
  zeros onto it so that it has minimum-length."
  [minimum-length p]
  (let [n-zeros (- minimum-length (count p))]
    (if (pos? n-zeros)
      (concat (repeat n-zeros 0) p)
      p)))

(defn add-coeff
  "Add x and y mod m."
  [m x y]
  (mod (+ x y) m))

(defn sub-coeff
  "Subtract x and y mod m."
  [m x y]
  (mod (- x y) m))

(defn mult-coeff
  "Multiply x and y mod m."
  [m x y]
  (mod (* x y) m))

(defn quot-coeff
  "Divide x and y mod m using integer division."
  [m x y]
  (mod (quot x y) m))



(defn add-poly
  [m p1 p2]
  "Add polynomials p1 and p2 with mod m arithmetic on coefficients."
  (map (partial add-coeff m)
       p1 p2))

(defn sub-poly
  [m p1 p2]
  "Subtract polynomials p1 and p2 with mod m arithmetic on coefficients."
  (map (partial sub-coeff m)
       p1 p2))

;; TODO: mult-poly


(defn largest-exponent
  "Find the largest exponent in a sequence of integers representing coefficients
  of a polynomial arranged from smallest to largest exponent in order."
  [p]
  (let [p' (vec p)]
    (loop [i (dec (count p'))]
      (cond (neg? i) nil
            (zero? (p' i)) (recur (dec i))
            :else i))))

(defn div-poly
  "Long division mod m for polynomials p1 and p2."
  [m pdividend pdivisor]
  (let [dividend-degree (largest-exponent pdividend)
        divisor-degree (largest-exponent pdivisor)]
))

;; FIXME shouldn't be dividing by zero.
;; algorithm isn't right yet.
;; assumed lerger exponents on left
(defn old-div-poly
  "Long division mod m for polynomials p1 and p2."
  [m p1 p2]
  (when (or p1 p2)
    (let [dividend (first p1)
          divisor  (first p2)
          p1' (rest p1)
          p2' (rest p2)
          remainder (mod dividend divisor)
          quotient  (quot dividend divisor) ; should already be in prime field
          to-subtract (map (mult-coeff m quotient) p2')
          newdividend (sub-poly p1' to-subtract)]
    (cons remainder (div-poly newdividend p2')))))
