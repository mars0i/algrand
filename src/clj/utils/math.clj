(ns utils.math)
;:require [clojure.math.numeric-tower :as ma]

;; If I want to be able to return integers, and not doubles (which
;; sometimes are not exactly equal to the integer), consider some of the
;; answers here:
;; https://stackoverflow.com/questions/6827516/logarithm-for-biginteger
(defn log-base-n
  "Returns the logarithm of x in base (as a double)."
  [base x]
  (/ (Math/log x) (Math/log base)))
