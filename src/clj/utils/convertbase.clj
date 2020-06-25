;;;; Functions to convert bases
(ns utils.convertbase)

;; Only for numbers in [0,1) and bases <= 36.
(defn convert-fract-seq
  "Given a number x in [0,1), generates a lazy sequence of digits that would 
  appear after the decimal point in a representation in the given base."  
  [x base]
  (lazy-seq 
    (let [shifted-x (* x base)
          int-part (floor shifted-x)
          fract-part (- shifted-x int-part)]
      (cons (int int-part)
            (convert-fract-seq fract-part base)))))

;; Only for numbers in [0,1) and bases <= 36.
(defn convert-fract
  "Given a number x in [0,1), returns a string representation of the number in 
  the given base with the specified number of digits after the decimal point.
  If the fourth argument exists and is not nil or false, internal operations
  use Clojure Ratio numbers, which are slower but avoid floating-point error."
  [x base digits & [ratl]]
  (apply str "0." 
         (map num-to-lowercase
              (take digits (convert-fract-seq 
                             (if ratl (rationalize x) x)
                             base)))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn strip-dot
  [s]
  (apply str (split s #"\.")))

(defn div-with-rem
  [x div]
  [(quot x div) (mod x div)])
