;; Some material to use for unit tests

;; In F5, division example, Lidl & Niederreiter _Finite Fields_, pp. 20f:
(def ff20-dividend  [3 4 0 0 1 2])
(def ff20-divisor   [1 0 3])
(def ff20-quotient  [1 2 2 4])
(def ff20-remainder [2 2])

;; F2^2 addition table
user=> (map (partial add-poly 2 [0]) [[0] [1] [0 1] [1 1]])
([0] [1] [0 1] [1 1])
user=> (map (partial add-poly 2 [1]) [[0] [1] [0 1] [1 1]])
([1] [0] [1 1] [0 1])
user=> (map (partial add-poly 2 [0 1]) [[0] [1] [0 1] [1 1]])
([0 1] [1 1] [0 0] [1 0])
user=> (map (partial add-poly 2 [1 1]) [[0] [1] [0 1] [1 1]])
([1 1] [0 1] [1 0] [0 0])

;; F2^2 multiplication table
user=> (map (partial mult-poly nw37-F2prim2 2 [0]) [[0] [1] [0 1] [1 1]])
([0] [0] [0] [0])
user=> (map (partial mult-poly nw37-F2prim2 2 [1]) [[0] [1] [0 1] [1 1]])
([0] [1] [0 1] [1 1])
user=> (map (partial mult-poly nw37-F2prim2 2 [0 1]) [[0] [1] [0 1] [1 1]])
([0] [0 1] [1 1] [1])
user=> (map (partial mult-poly nw37-F2prim2 2 [1 1]) [[0] [1] [0 1] [1 1]])
([0] [1 1] [1] [0 1])

;; F2^3 multiplication table
user=> (map (partial mult-poly nw37-F2prim3 2 [0]) [[0] [1] [0 1] [1 1] [0 0 1] [1 0 1] [0 1 1] [1 1 1]])
([0] [0] [0] [0] [0] [0] [0] [0])
user=> (map (partial mult-poly nw37-F2prim3 2 [1]) [[0] [1] [0 1] [1 1] [0 0 1] [1 0 1] [0 1 1] [1 1 1]])
([0] [1] [0 1] [1 1] [0 0 1] [1 0 1] [0 1 1] [1 1 1])
user=> (map (partial mult-poly nw37-F2prim3 2 [0 1]) [[0] [1] [0 1] [1 1] [0 0 1] [1 0 1] [0 1 1] [1 1 1]])
([0] [0 1] [0 0 1] [0 1 1] [1 1] [1] [1 1 1] [1 0 1])
user=> (map (partial mult-poly nw37-F2prim3 2 [1 1]) [[0] [1] [0 1] [1 1] [0 0 1] [1 0 1] [0 1 1] [1 1 1]])
([0] [1 1] [0 1 1] [1 0 1] [1 1 1] [0 0 1] [1] [0 1])
user=> (map (partial mult-poly nw37-F2prim3 2 [0 0 1]) [[0] [1] [0 1] [1 1] [0 0 1] [1 0 1] [0 1 1] [1 1 1]])
([0] [0 0 1] [1 1] [1 1 1] [0 1 1] [0 1] [1 0 1] [1])
user=> (map (partial mult-poly nw37-F2prim3 2 [1 0 1]) [[0] [1] [0 1] [1 1] [0 0 1] [1 0 1] [0 1 1] [1 1 1]])
([0] [1 0 1] [1] [0 0 1] [0 1] [1 1 1] [1 1] [0 1 1])
user=> (map (partial mult-poly nw37-F2prim3 2 [0 1 1]) [[0] [1] [0 1] [1 1] [0 0 1] [1 0 1] [0 1 1] [1 1 1]])
([0] [0 1 1] [1 1 1] [1] [1 0 1] [1 1] [0 1] [0 0 1])
user=> (map (partial mult-poly nw37-F2prim3 2 [1 1 1]) [[0] [1] [0 1] [1 1] [0 0 1] [1 0 1] [0 1 1] [1 1 1]])
([0] [1 1 1] [1 0 1] [0 1] [1] [0 1 1] [0 0 1] [1 1])

