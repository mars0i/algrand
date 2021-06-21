(ns algrand.finitefield
    (:require [clojure.math.numeric-tower :as nt]))

;; For now, polynomials are Clojure vectors of integers that are elements 
;; of a prime field, with smaller exponents on the left.  
;; Later: Possibly generalize to subfields other than prime fields.


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; POLYNOMIALS FOR TESTING

;; polynomials over F2 (or higher):
(def poly2a [1 1 0 1 0 0 1 1])
(def poly2b [0 1 1 1 0 1 1 0])

;; polynomials over F3 (or higher):
(def poly3a  [2 1 2 1 0 1 2 2])
(def poly3b  [0 0 0 1 0 2])
(def poly3b+ [0 0 0 1 0 2 0 0])

;; F5
(def poly5a [0 1 1 1 0 1 1 0 2 4 3 0 3])
(def poly5b [1 0 2 4])
(def poly5c [3 2 4 3])


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MISC UTILITY FUNCTIONS

(defn degree
  "Returns the degree of (vector) polynomial p, or nil if all zeros, i.e.
  it represents the zero polynomial."
  [p]
  (let [len (count p)]
    (loop [i (dec len)]
      (cond (neg? i) -1   ; i.e. all zeros, negative degree
            (pos? (p i)) i ; note zero degree means pnomial is nonzero constant
            :else (recur (dec i))))))

(defn pad-high-zeros
  "If sequence p is shorter than minimum-length, concatenate zeros
  onto it so that it has minimum-length."
  [minimum-length p]
  (let [n-zeros (- minimum-length (count p))]
    (if (pos? n-zeros)
      (vec (concat p (repeat n-zeros 0)))
      p)))

(defn strip-high-zeros
  "If polynomial vector p has extra zeros after the largest nonzero
  term, strip them off."
  [p]
  (vec (take (inc (degree p)) p)))

(defn normalize-lengths
  "If one of the polynomial sequences poly1 or poly2 is shorter than the
  other, pad it with initial zeros."
  [poly1 poly2]
  (let [poly1-len (count poly1)
        poly2-len (count poly2)]
    (cond (< poly1-len poly2-len) [(pad-high-zeros poly2-len poly1) poly2]
          (> poly1-len poly2-len) [poly1 (pad-high-zeros poly1-len poly2)]
          :else [poly1 poly2])))

(defn make-monomial
  [exponent coef]
  (conj (vec (repeat exponent 0)) coef))

(defn make-zero-poly
  [len]
  (vec (repeat len 0)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; INTEGER ARITHMETIC MOD m

(defn add-int
  "Add x and y mod m."
  [m x y]
  (mod (+ x y) m))

(defn sub-int
  "Subtract x and y mod m."
  [m x y]
  (mod (- x y) m))

(defn mult-int
  "Multiply x and y mod m."
  [m x y]
  (mod (* x y) m))

;; See https://en.wikipedia.org/wiki/Finite_field_arithmetic#Multiplicative_inverse
;; Current version uses theorem that for nonzero elements of a field of prime 
;; order m, zero, x^{m-1} = 1 mod m, so x^{m-2} mod m is x's multiplicative 
;; inverse.  This can produce very large integers internally, making use
;; of Clojure's BigInt facility..
;; Consider revising to make use of the extended Euclidean algorithm,  which
;; won't use such large numbers, as specified here:
;; and https://en.wikipedia.org/wiki/Extended_Euclidean_algorithm#Modular_integers
;; Also consider wrapping this in memoize.
(defn invert-int-nomemo
  "Computes the inverse of a nonzero x, mod m, assuming that m is prime.
  Does not memoize: Recomputes every time the same arguments are provided."
  [m x]
  (long
    (mod (nt/expt x (- m 2))
       m)))

(def invert-int 
  "([m x])
  Computes the inverse of a nonzero x, mod m, assuming that m is prime.
  Memoizes: The inverse of x mod m is only computed the first time the
  function is called with m and x.  Then the result is stored for future
  use in the same Clojure session."
  (memoize invert-int-nomemo))

;; Note that it's not enough to simply divide usng quot and then mod the result.
(defn div-int
  "Divides x by y mod m, or rather, multiplies x by the inverse of y mod m."
  [m x y]
  (mult-int m x (invert-int m y)))

(defn mult-int-poly
  "Multiply coefficients of polynomial poly by scalar value x mod m."
  [m x poly]
  (mapv (partial mult-int m x)
        poly))

(defn div-int-poly
  "Divide coefficients of polynomial poly by scalar value y mod m."
  [m y poly]
  (mapv (partial mult-int m (invert-int m y))
        poly))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; POLYNOMIAL ARITHMETIC MOD POLY

(defn add-poly
  [m poly1 poly2]
  "Add polynomials poly1 and poly2 with mod m arithmetic on coefficients.
  Does not carry."
  (let [[poly1' poly2'] (normalize-lengths poly1 poly2)]
  (mapv (partial add-int m) poly1' poly2')))

(defn sub-poly
  [m poly1 poly2]
  "Subtract polynomials poly1 and poly2 with mod m arithmetic on coefficients.
  Does not borrow."
  (let [[poly1' poly2'] (normalize-lengths poly1 poly2)]
    (mapv (partial sub-int m) poly1' poly2')))

;; FIXME: Don't I need mod by the defining primitive polynomial?
;; And there should be an additional argument?
;; Yes: If degree of result is >= degree of primitive poly, then
;; divide by the latter and return the remainder.
(defn mult-poly
  "Polynomial multiplication mod m."
  [m poly1 poly2]
  (let [poly1-len (count poly1)
        poly2-len (count poly2)
        ;; result length is count-1 + count-1 + one more for zeroth place:
        starter (make-zero-poly (+ poly1-len poly2-len -1))
        indexes (for [i (range poly1-len), j (range poly2-len)] [i j])]
    ;; Vectors are associative in Clojure, so we can construct using update:
    (reduce (fn [poly [i1 i2]]
               (update poly
                       (+ i1 i2) ; multiplication sums exponents
                       (partial add-int m) (mult-int m (poly1 i1) (poly2 i2)))) ; add new product (old value is passed as first arg to updating fn)
            starter indexes)))

;; FIXME: Don't I need mod by the defining primitive polynomial?
;; (Or only in mult-poly?) (Will that the mutual recursion terminate?)
;;
;; FIXME: What should the test for termination be?  If we allow dividing the
;; leading polynomials when they have the same degree, one will keep dividing
;; forever.  If I don't, that won't loop forever, but why shouldn't the 
;; coefficients divide each other?  Or should it be something more complicated,
;; like "divide once" or "divide if the dividend's coefficient is larger?
;; (What is "larger" in modular arithmetic?)
;;
;; Would it help to divide both terms by the leading coefficient of the 
;; divsor, so that it's monic?  Then the leading term of the dividend
;; will go away after the first subtraction.
;;
;; Or maybe the problem is that sub-int is wrong?  Or there's something else 
;; that's wrong in my polynomial division implementation?
;;
;; (Incorrect) pseudocode for the following function:
;; let result vec = all zeros
;; if degree dsor > degree dend then ret result vec, and dend as the remainder
;; let a = div max idx of dend by max idx of dsor
;; let b = div max coef of dend by max coef of dsor, mod m
;; place b in loc a in temp result vec (mostly zeros)
;; let c = dsor * temp result vec, mod m
;; let new dend = dend - c, mod m
;; recurse with result vec += temp result vec (filled at diff locs: a merge)
(defn div-poly
  "Long division mod primitive polynomial p, with coefficients mod m of,
  polyomial dividend by polynomial divisor.  
  Returns pair containing quotient and remainder polynomials."
  [p m dividend divisor]
  (let [deg-dividend (degree dividend)
        deg-divisor (degree divisor)
        dsor-lead-coef (divisor deg-divsor) ;; make divisor monic:
        dend (div-int-poly m dend-lead-coef dividend)
        dsor (div-int-poly m dsor-lead-coef divisor)]
    (when (neg? deg-divisor) (throw (Exception. "Division by the zero polynomial.")))
    (loop [quotient (make-zero-poly (inc (- deg-dividend deg-divisor))) 
           dend dividend]
          (let [deg-dend (degree dend)]
            (if (>= deg-divisor deg-dend)
              {:quotient quotient, :remainder (strip-high-zeros dend)}
              ;; Divide largest term in dividend by largest term in divisor:
              (let [qexpt (- deg-dend deg-divisor) ; divide exponent = subtract
                    qcoef (div-int m (dend deg-dend) (divisor deg-divisor))
                    newquotient (assoc quotient qexpt qcoef)
                    multiplier (make-monomial qexpt qcoef)
                    newdend (sub-poly m dend (mult-poly m divisor multiplier))] 
                ;(println "deg-dend:" deg-dend " deg-divisor:" deg-divisor) ; DEBUG
                ;(println "qexpt:" qexpt " qcoef:" qcoef) ; DEBUG
                ;(println "new quotient:" newquotient) ; DEBUG
                ;(println "monomial multiplier:" multiplier) ; DEBUG
                ;(println "new dividend:" newdend) ; DEBUG
                ;(println)
                (recur newquotient newdend)))))))
