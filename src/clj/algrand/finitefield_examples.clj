(ns algrand.finitefield-examples)
;; Examples of polynomial vectors for experiments, testing, etc.
;; with functions in algrand.finitefield.

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

;; Primitive polynomials over F2 from Niederreiter & Winterhof p. 37:
(def nw37F2prim2 [1 1 1])
(def nw37F2prim3 [1 1 0 1])  ; also in Aspnes "Notes on Finite Fields" p. 5
(def nw37F2prim4 [1 1 0 0 1])
(def nw37F2prim5 [1 0 1 0 0 1])
(def nw37F2prim6 [1 1 0 0 0 0 1])

;; F3^4 = F81 from Alanenen & Knuth. See doc/finitefieldTestsNotes2.txt.
(def akF3prim4 [2 0 0 1 1]) ; primitive polynomial (pp. 321, 316, cf. p. 310)
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
