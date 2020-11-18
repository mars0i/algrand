import Data.List  (union, transpose) -- no longer used: isPrefixOf
import Debug.Trace (trace)
import Data.Typeable (typeOf)
-- no longer used: import Data.Char  (digitToInt)


-- THIS IS NOT RIGHT.  IT ADDS PAYOUTS TO ALL POSSIBLE STRINGS.  BUT ONLY THOSE
-- STARTING WITH A PREFIX IN THE GENERATOR SHOULD COUNT.  HOWEVER, WHERE THOSE
-- PREFIXES OVERLAP, THE PAYOUTS SHOULD BE SUMMED.

-- The problem is that I have the payouts organized as a sequence of pairs.
-- What I need is a tree: an infinite tree.
-- A tree starts with "".  I can either make it the full binary tree, with zero
-- payouts on some branches, or just represent the parts with payouts.  
-- In the latter case, while an input string has length less than the longest string
-- in a generator set (assuming it's finite), the payouts are summed from the current
-- location in all of the still-overlapping generator prefixes.
-- If a string branches off of all prefixes, subsequent payouts are zero.
-- if a string matches a prefix but goes beyond its end, then all possible
-- extensions each get payout 1 per location.  
-- Note that since the generator set is itself prefix-free, once a string matching
-- a prefix goes beyond it, the other generators can be ignored, since they can't be
-- prefixes of any of the extended strings.
-- 
-- So I don't actually need an infinite tree per se, since the default behavior after
-- going beyond a generator prefix's length is always the same, and involved no summing.
-- (I didn't realize this previously.)

{-

Code to help verify/understand part of the second half of
Downey and Hirschfeldt's (2010) Theorem 6.3.4, p. 236, which is their
version of Schnorr's theorem that Martin-Löf randomness is equivalent
to randomness defined by computably enumerable martingales.  In
particular, a set of infinite sequences is Martin-Löf iff it contains
no sequences such thagt a c.e. martingale would produce infinite
profit betting on subsequent digits of such a sequence.

In particular, in the M-L random -> martingale random direction of the
proof, D&H give a method for constructing a martingale for each set in
an M-L test.  The construction starts from the fact (Proposition 2.19.2,
p. 74) that every computably enumerable set of infinite sequences can be
generated by a computably enumerable prefix-free set of finite
sequences, and then the construction specifies how to define a
martingale function d_n for each prefix-free set R_n generating one of
the sets U_n that is part of a given M-L test.

The code below constructs the values of d_n for a given R_n.  D&H's
description of d_n is terse, and I had trouble working out why d_n
really is a martingale.  This code allows me to perform the
construction, see the result, and better understand why it creates a
martingale.

(This is my first recent Haskell experiment.  No doubt it could much
more succinct and elegant.)

-}


-- TODO: maybe actually define the d functions so that they can be applied to inputs.


------------------------------------------------------------------
-- new approach

-- doesn't work:
-- data Tree a = Node a Tree Tree | Term deriving (Eq, Ord, Show)
-- instance Functor Tree where fmap f Term = Term fmap f (Node x zero one) = Node (f x) (fmap zero) (fmap one)

-- partly from Learn You a Haskell:

-- a should be Float; it's the payout
data Tree a = EmptyTree | Node a (Tree a) (Tree a) deriving (Show, Eq)  

instance Functor Tree where  
    fmap f EmptyTree = EmptyTree -- probably unused
    fmap f (Node p next_zero next_one) = Node (f p) (fmap f next_zero) (fmap f next_one)

thisPayout (Node p _ _) = p
thisPayout EmptyTree = undefined

nextZero (Node _ zero _) = zero
nextZero EmptyTree = undefined
    
nextOne  (Node _ _ one) = one
nextOne  EmptyTree = undefined

zeroPayoutsTree :: Tree Float
zeroPayoutsTree = Node 0.0 (zeroPayoutsTree) (zeroPayoutsTree)

onePayoutsTree  :: Tree Float
onePayoutsTree  = Node 1.0 (onePayoutsTree)  (onePayoutsTree)


{- |
Example: add_payouts generator (lowerPayouts generator) zeroPayoutsTree
-}
addPayouts (g:gs) (p:ps) (Node x next_zero next_one)
    | g == '0' = Node (x+p) (addPayouts gs ps next_zero) next_one
    | g == '1' = Node (x+p) next_zero (addPayouts gs ps next_one)
addPayouts ""  [] (Node _ _ _) = onePayoutsTree -- once generator exhausted, rest are ones
addPayouts (g:gs) [] (Node _ _ _) = undefined   -- shouldn't happen
addPayouts "" (p:ps) (Node _ _ _) = undefined   -- shouldn't happen
addPayouts _    _    EmptyTree = EmptyTree      -- probably shouldn't happen







------------------------------------------------------------------
-- "raw payouts" create values paired with digits from generator
-- strings according to D&H's rule.


{- |
'lowerPayouts generator' returns a list of payout components for 
the length of of string 'generator'.  The first element corresponds to
the empty string; the last corresponds to the position one less than the
length of the generator.  These are payouts correspondng to the string up
to that point.
-}
lowerPayouts :: String -> [Float]
lowerPayouts generator =
    let len_s = fromIntegral (length generator) in
        map (2**) [-len_s .. -1]
        -- meaning from D&H: map ((2**) . (-len_s +)) [0 .. len_s-1]
        -- Debug.Trace.trace ("len_s = " ++ show len_s) 


{- |
   Returns a list of payout components for the infinite sequence generated 
   by string 'generator'.
-}
raw_generator_payouts :: String -> [(Maybe Char, Float)]
raw_generator_payouts generator =
    zip ((map Just generator) ++ (repeat Nothing))
        ((lowerPayouts generator) ++ (repeat 1))

{- |
   Applies 'generator_payouts' to every generator string in Martin-Löf
   test component set of generators 'test_set'.
-}
raw_payouts_for_test_set test_set = map raw_generator_payouts test_set
-- (kind of silly to separate this out, but useful for testing.)

{- |
   Applies 'raw_payouts_for_test_set' to every component set of 
   generators for a complete Martin-Löf test.
-}
raw_payouts_for_test test = map raw_payouts_for_test_set test

------------------------------------------------------------------
-- "payouts"/Payouts: combine raw payouts into per-digit sums

data Payout = ZeroPayout Float | OnePayout Float  deriving (Show, Eq)

{- |
    Add payout 'p' in '(Maybe Char, p)' to payouts already accumulated in 
    '(ZeroPayout p0, OnePayout p1)' for each digit '0', '1'.
    'Just i' means that payout p is to be added for instances of i
    (i.e. '0', or '1').
    'Nothing' means that payout p applies to both '0' and '1', i.e.
    it will be added both to 'ZeroPayout p0' and 'OnePayout p1'.
-}
acc_payouts ::  (Payout, Payout) -> (Maybe Char, Float) -> (Payout, Payout)
acc_payouts (ZeroPayout p0, OnePayout p1) (Just '0', p) =  (ZeroPayout (p0 + p), OnePayout p1)
acc_payouts (ZeroPayout p0, OnePayout p1) (Just '1', p) =  (ZeroPayout p0,       OnePayout (p1 + p))
acc_payouts (ZeroPayout p0, OnePayout p1) (Nothing,  p) =  (ZeroPayout (p0 + p), OnePayout (p1 + p))

-- Assumes that there is a finite number of generators for each test set; this is not required by M-L.
combine_payouts_at_index payouts_at_index =
    foldl acc_payouts (ZeroPayout 0, OnePayout 0) payouts_at_index

combine_payouts_for_test_set payouts_for_test_set =
    map combine_payouts_at_index (transpose payouts_for_test_set)
    -- amazingly, one can transpose a finite list of infinite lists

payouts_for_test test =
    map combine_payouts_for_test_set (raw_payouts_for_test test)



----------------------------------------------------------
-- Conveience functions for constructing M-L tests

consOtherDigit initstr [] = initstr
consOtherDigit _ (s:ss) =
    if s == '1'
       then '0':s:ss
       else '1':s:ss

combineMLtests (x:xs) (y:ys) = (union x y) : combineMLtests xs ys
combineMLtests xs []         = xs
combineMLtests [] ys         = ys

--------------------------------------------------
-- Example M-L tests

-- prefix-free generators for M-L tests for infinite zeros and infinite ones
zeros          = iterate (\ss -> map ('0':) ss) [""] -- 0, 00, 000, ...
ones           = iterate (\ss -> map ('1':) ss) [""] -- 1, 11, 111, ...
terminal_zeros = [""] : iterate (\ss -> map ('1':) ss) ["0"] -- 0, 10, 110, ...
terminal_ones  = [""] : iterate (\ss -> map ('0':) ss) ["1"] -- 1, 01, 001, ... 

zero_ones = iterate (\ss -> map (consOtherDigit "0") ss) [""]
one_zeros = iterate (\ss -> map (consOtherDigit "1") ss) [""]
alternators = combineMLtests zero_ones one_zeros

zeros_or_ones = combineMLtests zeros ones
terminal_both = combineMLtests terminal_zeros terminal_ones

-- remove the first set since it includes "", a prefix to other elements
multisize :: [[String]]
multisize = drop 1 (foldr combineMLtests [[]] 
                          [terminal_ones, drop 2 terminal_zeros, 
                          drop 4 terminal_ones, drop 6 zeros])

-- Note that even though there are shared prefixes, no string in any
-- generator set is a prefix of any other (since they're all the same
-- length!).
someofem = foldr combineMLtests [[]] 
                 [zeros, ones, terminal_zeros, terminal_ones, zero_ones, 
                 one_zeros]
-- don't include e.g. multisize--you'll get prefix relations
