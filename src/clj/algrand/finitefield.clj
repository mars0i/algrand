(ns algrand.finitefield
    (:require [clojure.math.numeric-tower :as nt]))

;; For now, polynomials are Clojure sequences of integers that are
;; elements of a prime field.  I should generalize that later to
;; other subfields.


;; Some polynomials for testing:

;; polynomials over F2 or F3:
(def poly1 [1 1 0 1 0 0 1 0])
(def poly2 [0 1 1 1 0 1 1 0])

;; polynomials over F3:
(def poly3 [2 1 2 1 0 1 2 0])
(def poly3 [2 0 0 1 0 2 0 0])


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


;; FIXME shouldn't be dividing by zero.
;; algorithm isn't right yet.
(defn div-poly
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




