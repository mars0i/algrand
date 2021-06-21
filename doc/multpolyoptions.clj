;; versions of mult-poly-generic

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Uses a Clojure vector as if it was a map, summing into the old
;; element to produce a series of new versions in a reduce:
(defn mult-poly-generic-vec
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
    (reduce (fn [pnew [i1 i2]] (update pnew
                                    (+ i1 i2) ; multip sums coeffs
                                    + (* (p1' i1) (p2' i2)))) ; add new product
            starter indexes)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Collects values to sum in a bunch of small maps with numeric keys,
;; then merges them into one map using merge-with to sum them.
;; Needs a separate routine to convert this into a vector at the end--
;; there doesn't seem to be a kool Clojure trick to one-line this.
(defn numeric-map-to-vec
  "Convert a map whose keys are integers into a vector with the same
  values, now indexed by the map keys."
  [numeric-map]
  (reduce (fn [newvec i]    ; or: (vec (map second (sort m))))
              (conj newvec (numeric-map i)))
          []
          (range (count numeric-map))))

(defn mult-poly-generic
  "Polynomial multiplication without modulus."
  [p1 p2]
  (let [p1' (vec p1) ; in case a non-vector is passed in
        p2' (vec p2)
        p1-range (range (count p1'))
        p2-range (range (count p2'))
        sums (for [i1 p1-range
                   i2 p2-range]
                  {(+ i1 i2)
                   (* (p1' i1) (p2' i2))})
        sums-map (apply merge-with + sums)]
    (numeric-map-to-vec sums-map)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; I kind of like this the best, even though it's unclojurely.  It
;; uses a Java array so that the summing into the data structure can
;; be done imperatively.  This is simpler, even though I define a
;; separate function to handle the updating so that the main code is
;; less verbose.  And externally, it's fine for Clojure since the
;; array is created in the function and never escapes it.  However,
;; if I ever want to run the code in Clojurescript, this one won't work.
(defn aupdate 
  "Update the value of Java array a at index i with function f. 
  f is passed the value of a at i and returns a new value to replace it."
  [a i f]
  (aset a i (f (aget a i))))

;; Simple version uses Java array (won't run in Clojurscript)
(defn mult-poly-generic-array
  "Polynomial multiplication without modulus."
  [p1 p2]
  (let [p1' (vec p1) ; in case a non-vector is passed in
        p2' (vec p2)
        p1-len (count p1')
        p2-len (count p2')
        ;; length is count-1 + count-1 + one more for zeroth place:
        coeffs (long-array (repeat (+ p1-len p2-len -1) 0))]
    (doseq [i1 (range p1-len)
            i2 (range p2-len)]
      (aupdate coeffs
               (+ i1 i2)
               (partial + (* (p1' i1) (p2' i2)))
               ))
    (vec coeffs)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn mult-poly
  "Polynomial multiplication mod m."
  [m p1 p2]
  (let [p1-len (count p1)
        p2-len (count p2)
        ;; result length is count-1 + count-1 + one more for zeroth place:
        starter (make-zero-poly (+ p1-len p2-len -1))
        indexes (for [i (range p1-len), j (range p2-len)] [i j])]
    ;; Vectors are associative in Clojure, so we can construct using update:
    (reduce (fn [poly [i1 i2]]
               (update poly
                       (+ i1 i2) ; multiplication sums exponents
                       (partial add-int m) (mult-int m (p1 i1) (p2 i2)))) ; add new product
            starter indexes)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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


