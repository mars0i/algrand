(ns algrand.finitefield
    (:require [clojure.math.numeric-tower :as nt]))

;; For now, polynomials are Clojure sequences of integers (preferably
;; vectors) that are elements of a prime field, with larger exponents on 
;; the left.  That means you can't read off exponents from degrees,
;; but it also means that e.g. with long division, you don't have to
;; count backwards all the time, which prevents some Clojure
;; conveniences unless you keep reversing vectors.

;; FIXME Oh no--but I did multiplication the other way.  
;; Multiplication is easier with low exponents on the left.
;; Division is easier the other way.

;; TODO: Generalize to other subfields than prime fields.


;; Some polynomials for testing:

;; polynomials over F2 or F3:
(def poly1 [1 1 0 1 0 0 1 1])
(def poly2 [0 1 1 1 0 1 1 0])

;; polynomials over F3:
(def poly3 [2 1 2 1 0 1 2 2])
(def poly4 [0 0 0 1 0 2 0 0])


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


;; FIXME I'm now putting high exponents on the right
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

(defn normalize-lengths
  "If one of the polynomial sequences p1 or p2 is shorter than the
  other, pad it with initial zeros."
  [p1 p2]
  (let [p1-len (count p1)
        p2-len (count p2)]
    (cond (< p1-len p2-len) [(pad-high-zeros p2-len p1) p2]
          (> p1-len p2-len) [p1 (pad-high-zeros p1-len p2)]
          :else [p1 p2])))

(defn add-poly
  [m p1 p2]
  "Add polynomials p1 and p2 with mod m arithmetic on coefficients.
  Does not carry."
  (let [[p1' p2'] (normalize-lengths p1 p2)]
  (map (partial add-coeff m) p1' p2')))

(defn sub-poly
  [m p1 p2]
  "Subtract polynomials p1 and p2 with mod m arithmetic on coefficients.
  Does not carry."
  (let [[p1' p2'] (normalize-lengths p1 p2)]
    (map (partial sub-coeff m) p1' p2')))

;; Polynomial multiplication without modulus may be independently useful, 
;; is simpler, and may be more efficient fwiw.  A separate function mods it.
(defn mult-poly-generic
  "Polynomial multiplication without modulus."
  [p1 p2]
  (let [p1' (vec p1) ; in case a non-vector is passed in
        p2' (vec p2)
        p1-len (count p1')
        p2-len (count p2')
        p1-range (range (count p1'))
        p2-range (range (count p2'))
        ;; length is count-1 + count-1 + one more for zeroth place:
        starter (vec (repeat (+ p1-len p2-len -1) 0))
        indexes (for [i (range p1-len)
                      j (range p2-len)]
                  [i j]) ]
    ;; Vectors are associative in Clojure, so we can construct using update:
    (reduce (fn [poly [i1 i2]]
               (update poly
                       (+ i1 i2) ; multiplication sums exponents
                       + (* (p1' i1) (p2' i2)))) ; add new product
            starter indexes)))

(defn mult-poly
  "Polynomial multiplication mod m."
  [m p1 p2]
  (mapv (fn [n] (mod n m))
        (mult-poly-generic p1 p2)))


;; FIXME I'm now putting high exponents on the right
(defn largest-exponent
  "Find the largest exponent in a sequence of integers representing
   coefficients of a polynomial arranged from largest to smallest exponent,
  in order."
  [p]
  (dec (count (strip-high-zeros p))))  ; could be more efficient, but so?

;; pcode
;; let result vec = all zeros
;;
;; if degree dsor > degree dend then ret result vec, and dend as the remainder
;; let a = div max idx of dend by max idx of dsor
;; let b = div max coef of dend by max coef of dsor, mod m
;; place b in loc a in temp result vec (mostly zeros)
;; let c = dsor * temp result vec, mod m
;;   (or last step could be broken out into separate coef and index mults)
;; let new dend = dend - c, mod m
;; recurse with result vec += temp result vec (filled at diff locs: a merge)
;;
(defn div-poly
  "Long division mod m for polynomials p1 and p2."
  [m pdividend pdivisor]
  (let [dividend-degree (largest-exponent pdividend)
        divisor-degree (largest-exponent pdivisor)]
))


;; shouldn't be dividing by zero.
;; algorithm isn't right yet.
;; I'm now putting high exponents on the right
;(defn old-div-poly
;  "Long division mod m for polynomials p1 and p2."
;  [m p1 p2]
;  (when (or p1 p2)
;    (let [dividend (first p1)
;          divisor  (first p2)
;          p1' (rest p1)
;          p2' (rest p2)
;          remainder (mod dividend divisor)
;          quotient  (quot dividend divisor) ; should already be in prime field
;          to-subtract (map (mult-coeff m quotient) p2')
;          newdividend (sub-poly p1' to-subtract)]
;    (cons remainder (div-poly newdividend p2')))))
