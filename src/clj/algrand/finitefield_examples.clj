(ns algrand.finitefield-examples
    (:require [algrand.finitefield :as ff]))
;; Examples of polynomial vectors for experiments, testing, etc.
;; with functions in algrand.finitefield.

(defn alanenknuth-string-to-poly
  "Transform a string in \"Alanen-Knuth\" format--i.e. a string of
  digits representing polynomial coefficients, with hight exponents
  on the left--into a representation of a polynomial as a vector of
  integer coefficients with higher exponents on the right.  The
  asterisk character is treated as the number 10.  [See Alanen &
  Knuth (1964), Sankhya Series A, v. 26 no. 4, p. 309.]"
  [ak-str]
  (mapv 
    (fn [c] (if (= c \*) 10 (Character/digit c 10)))
    (reverse ak-str))) ; makes string into seq of chars

(defn alanenknuth-strings-to-polys
  "Transform a sequence of strings representing polyonomials in \"Alanen-Knuth\"
  format into a sequence of integer vector representations of the same 
  polynomials using alanenknuth-string-to-poly."
  [ak-strs]
  (mapv alanenknuth-strings-to-polys ak-strs))

;; polynomials over F2 (or higher):
(def poly2a [1 1 0 1 0 0 1 1])
(def poly2b [0 1 1 1 0 1 1 0])

;; polynomials over F3 (or higher):
(def poly3a  [2 1 2 1 0 1 2 2])
(def poly3b  [0 0 0 1 0 2])
(def poly3b+ [0 0 0 1 0 2 0 0])

;; Other F5 (or higher):
(def poly5a [0 1 1 1 0 1 1 0 2 4 3 0 3])
(def poly5b [1 0 2 4])
(def poly5c [3 2 4 3])

;; In F5, division example, Lidl & Niederreiter _Finite Fields_, pp. 20f:
(def ff20dividend  [3 4 0 0 1 2])
(def ff20divisor   [1 0 3])
(def ff20quotient  [1 2 2 4])
(def ff20remainder [2 2])

;; Underscore means exponent below, so e.g. "F2_5" means F_{2^5}, i.e. GF(32)

;; Primitive polynomials over F2 from Niederreiter & Winterhof p. 37:
(def nw37F2_2prim [1 1 1])  ; for F2^2
;; implementation of linear recurrence based on preceding:
(defn f [v] [(v 1) (v 2) (mod (+ (v 0) (v 1)) 2)])

(def nw37F2_3prim [1 1 0 1])  ; also in Aspnes "Notes on Finite Fields" p. 5
(def nw37F2_4prim [1 1 0 0 1]) ; for F2^4
(def nw37F2_5prim [1 0 1 0 0 1])
(def nw37F2_6prim [1 1 0 0 0 0 1])

;; A few primitive polynomials from section 7 of from Alanenen & Knuth
(def alF3_4prim [2 0 0 1 1]) ; F81 (pp. 321, 316, cf. p. 310)
(def alF5_2prim [2 1 1])     ; F25
(def alF5_3prim [2 0 1 1])   ; F125
(def alF5_4prim [3 0 1 1 1]) ; F625
(def alF7_2prim [3 1 1])     ; F49
(def alF7_3prim [2 1 1 1])   ; F343
(def alF11_2prim [7 1 1])    ; F121
(def alF11_3prim [5 0 1 1])  ; F1332


(def akF3_4 [[1 0 0 0] ; elements of F3^4 in order of powers of x, i.e. [0 1]
             [0 1 0 0] ; x^1
             [0 0 1 0] ; x^2
             [0 0 0 1] ; x^3
             [1 0 0 2] ; x^4
             [2 1 0 1] ; x^5
             [1 2 1 2] ; ...
             [2 1 2 2]
             [2 2 1 0]
             [0 2 2 1]
             [1 0 2 1]
             [1 1 0 1]
             [1 1 1 2]
             [2 1 1 2]
             [2 2 1 2]
             [2 2 2 2]
             [2 2 2 0]
             [0 2 2 2]
             [2 0 2 0]
             [0 2 0 2]
             [2 0 2 1]
             [1 2 0 1]
             [1 1 2 2]
             [2 1 1 0]
             [0 2 1 1]
             [1 0 2 0]
             [0 1 0 2]
             [2 0 1 1]
             [1 2 0 0]
             [0 1 2 0]
             [0 0 1 2]
             [2 0 0 2]
             [2 2 0 1]
             [1 2 2 2]
             [2 1 2 0]
             [0 2 1 2]
             [2 0 2 2]
             [2 2 0 0]
             [0 2 2 0]
             [0 0 2 2]
             [2 0 0 0]
             [0 2 0 0]
             [0 0 2 0]
             [0 0 0 2]
             [2 0 0 1]
             [1 2 0 2]
             [2 1 2 1]
             [1 2 1 1]
             [1 1 2 0]
             [0 1 1 2]
             [2 0 1 2]
             [2 2 0 2]
             [2 2 2 1]
             [1 2 2 1]
             [1 1 2 1]
             [1 1 1 1]
             [1 1 1 0]
             [0 1 1 1]
             [1 0 1 0]
             [0 1 0 1]
             [1 0 1 2]
             [2 1 0 2]
             [2 2 1 1]
             [1 2 2 0]
             [0 1 2 2]
             [2 0 1 0]
             [0 2 0 1]
             [1 0 2 2]
             [2 1 0 0]
             [0 2 1 0]
             [0 0 2 1]
             [1 0 0 1]
             [1 1 0 2]
             [2 1 1 1]
             [1 2 1 0]
             [0 1 2 1]
             [1 0 1 1]
             [1 1 0 0]
             [0 1 1 0]
             [0 0 1 1]]) ; x^79  (note x^{81-1} = [1])
