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

;; There doesn't seem to be a straightforward way to do this e.g. with into.
(defn numeric-map-to-vec
  "Convert a map whose keys are integers into a vector with the same
  values, now indexed by the map keys."
  [numeric-map]
  (reduce (fn [newvec i]
              (conj newvec (numeric-map i)))
          []
          (range (count numeric-map))))
;; simpler, hackier: (vec (map second (sort m))))

(defn mult-poly
  "Polynomial multiplication mod m."
  [m p1 p2]
  (let [p1' (vec p1) ; in case a non-vector is passed in
        p2' (vec p2)
        p1-range (range (count p1'))
        p2-range (range (count p2'))
        sums-map (apply merge-with (partial add-coeff m)
                        (for [i1 p1-range
                              i2 p2-range]
                             {(+ i1 i2)
                              (mult-coeff m (p1' i1) (p2' i2))}))]
    (numeric-map-to-vec sums-map)))
;(do (println [i1 i2 (+ i1 i2)] [(p1' i1) (p2' i2) (* (p1' i1) (p2' i2))])


(defn largest-exponent
  "Find the largest exponent in a sequence of integers representing
   coefficients of a polynomial arranged from largest to smallest exponent,
  in order."
  [p]
  (dec (count (strip-high-zeros p))))  ; could be more efficient, but so?

(defn div-poly
  "Long division mod m for polynomials p1 and p2."
  [m pdividend pdivisor]
  (let [dividend-degree (largest-exponent pdividend)
        divisor-degree (largest-exponent pdivisor)]
))

;; FIXME shouldn't be dividing by zero.
;; algorithm isn't right yet.
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
