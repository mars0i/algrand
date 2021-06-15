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
  [m x y]
  (mod (* x y) m))

;; use quot for raw division


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

(defn div-poly
  [m p1 p2]
  (loop [p1' p1
         p2' p2
         result []]
      (let [dividend (first p1')
            divisor  (first p2')
            quotient (quot dividend divisor)
            remainder (mod dividend divisor)]
        (recur
               (rest p1')
               (rest p2') 


