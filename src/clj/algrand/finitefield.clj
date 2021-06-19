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


(defn add-coef
  "Add x and y mod m."
  [m x y]
  (mod (+ x y) m))

(defn sub-coef
  "Subtract x and y mod m."
  [m x y]
  (mod (- x y) m))

(defn mult-coef
  "Multiply x and y mod m."
  [m x y]
  (mod (* x y) m))

;; NOT RIGHT
;; I think what it should do is:
;; (a) determine the inverse of the divisor
;; (b) multiply that by the dividend
(defn quot-coef
  "Divide x and y mod m using integer division."
  [m x y]
  (mod (quot x y) m))

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
  (mapv (partial add-coef m) p1' p2')))

(defn sub-poly
  [m p1 p2]
  "Subtract polynomials p1 and p2 with mod m arithmetic on coefficients.
  Does not carry."
  (let [[p1' p2'] (normalize-lengths p1 p2)]
    (mapv (partial sub-coef m) p1' p2')))

;; Polynomial multiplication without modulus may be independently useful, 
;; is simpler, and may be more efficient fwiw.  A separate function mods it.
(defn mult-poly-generic
  "Polynomial multiplication without modulus."
  [p1 p2]
  (let [p1-len (count p1)
        p2-len (count p2)
        ;; length is count-1 + count-1 + one more for zeroth place:
        starter (vec (repeat (+ p1-len p2-len -1) 0))
        indexes (for [i (range p1-len), j (range p2-len)] [i j])]
    ;; Vectors are associative in Clojure, so we can construct using update:
    (reduce (fn [poly [i1 i2]]
               (update poly
                       (+ i1 i2) ; multiplication sums exponents
                       +  (* (p1 i1) (p2 i2)))) ; add new product
            starter indexes)))

(defn mult-poly
  "Polynomial multiplication mod m."
  [m p1 p2]
  (mapv (fn [n] (mod n m))
        (mult-poly-generic p1 p2)))

(defn make-monomial
  [exponent coef]
  (conj (vec (repeat exponent 0)) coef))

(defn make-zero-poly
  [len]
  (vec (repeat len 0)))

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
  "Long division mod m of polyomial dividend by polynomial divisor.  
  Returns pair containing quotient and remainder polynomials."
  [m dividend divisor]
  (let [deg-dividend (degree dividend)
        deg-divisor (degree divisor)]
    (when (neg? deg-divisor) (throw (Exception. "Division by the zero polynomial.")))
    (loop [quotient (make-zero-poly (inc (- deg-dividend deg-divisor))) 
           dend dividend]
          (let [deg-dend (degree dend)]
            (if (>= deg-divisor deg-dend) ; TODO: If they are =, do we always divide?  Yes?? because even if divisor coeff is larger, we can divide mod m (?)
              [quotient (strip-high-zeros dend)] ; undivided dividend is remainder; TODO s/b a map?
              ;; Divide largest term in dividend by largest term in divisor:
              (let [qexpt (- deg-dend deg-divisor) ; divide exponent = subtract
                    ;; I DON'T THINK MY quot-coef IS RIGHT:
                    qcoef (quot-coef m (dend deg-dend) (divisor deg-divisor))
                    newquotient (assoc quotient qexpt qcoef)
                    multiplier (make-monomial qexpt qcoef)
                    newdend (sub-poly m dend (mult-poly m divisor multiplier))] 
                (println "deg-dend:" deg-dend " deg-divisor:" deg-divisor) ; DEBUG
                (println "qexpt:" qexpt " qcoef:" qcoef) ; DEBUG
                (println "new quotient:" newquotient) ; DEBUG
                (println "monomial multiplier:" multiplier) ; DEBUG
                (println "new dividend:" newdend) ; DEBUG
                (println)
                (recur newquotient newdend)))))))
