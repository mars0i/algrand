;; Functions for experimenting with ideas in Hava Siegelmann's book
;; *Neural Networks and Analog Computation: Beyond the Turing Limit*
(ns algrand.siegelmann
    (require [utils.convertbase :as base]))

(defn lambda-tilde
  "lambda-tilde [equation (4.6), p. 64] is Siegelmann's continuous version of
  Lambda, a left-shift on base-9 fractional numbers that use only even digits."
  [q]
  (letrec ((q9 (* q 9))
	   (aux (lambda (j acc)
		  (if (>= j 0)
		    (aux (- j 1)
			 (+ acc 
			    (* (expt -1 j)
			       (sigma (- q9 j)))))
		    acc))))
    (aux 8 0)))


(defn xsi-tilde
  "xsi-tilde [equation (4.7), p. 65] is Siegelmann's continuous version of Xsi,
  a select-leftmost-digit function on base-9 fractional numbers that use only 
  even digits."
  [q]
  (letrec ((q9 (* q 9))
           (aux (lambda (j acc)
		  (if (>= j 0)
		    (aux (- j 1)
			 (+ acc
			    (sigma (- q9 (* 2 j) -1)))))
		    acc))))
    (* 2 (aux 3 0))))

