import Data.List  (union, transpose) -- no longer used: isPrefixOf
import qualified Data.Tree  -- so I can convert to these trees and use some of their functions
import Debug.Trace (trace)    -- DEBUG
-- import Data.Typeable (typeOf) -- DEBUG
-- import Data.Foldable (foldr', foldl')
-- import Data.Char  (digitToInt)
import System.Random

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
-}


-- TODO: maybe actually define the d functions so that they can be applied to inputs.


------------------------------------------------------------------
-- Basic definitions and functions

-- | Trees represent functions from possible finite strings of 0's and 1's to
-- real numbers.  The payout field contains the value for the string of 0's 
-- and 1's up to this point.  The nextZero and nextOne contents represent 
-- the two possible next digits with their payouts, and subsequent possible
-- digits and payouts.  Note that the very first node in the entire tree 
-- typically represents neither 0 nor 1; it represents the empty sequence.
data Tree a = Leaf | Node {payout :: a,
                           nextZero :: (Tree a),
                           nextOne :: (Tree a)}
   deriving (Show, Eq)  

-- a should be Float; it's the payout

instance Functor Tree where  
    fmap f Leaf = Leaf
    fmap f (Node p next0 next1) = Node (f p) (fmap f next0) (fmap f next1)

children (Node _ z o) = [z, o]
children Leaf = [Leaf] -- or undefined?



{- |
'copyTree tree' generates a new copy of tree.
-}
copyTree Leaf = Leaf
copyTree (Node p z o) = Node p (copyTree z) (copyTree o)

{- |
'takeTree n tree' returns a tree that is identical tree up to
depth n, where it is truncated by replacing Nodes with Leafs.
-}
takeTree _ Leaf = Leaf
takeTree n (Node p z o) =
    if n <= 0
       then Leaf
       else Node p (takeTree (n-1) z) (takeTree (n-1) o)



{- |
'boundedTreeEqual n tree1 tree2' tests whether the two trees are
identical up to depth n.
-}
-- This is a lot faster than using takeTree.
boundedTreeEqual :: (Integral a) => a -> Tree Double -> Tree Double -> Bool
boundedTreeEqual _ Leaf Leaf = True
boundedTreeEqual _ Leaf _    = False
boundedTreeEqual _ _ Leaf    = False
boundedTreeEqual n (Node p1 z1 o1) (Node p2 z2 o2) =
    if n <= 0
       then p1 == p2
       else (boundedTreeEqual (n-1) z1 z2) && (boundedTreeEqual (n-1) o1 o2)

----------------------------------------------
-- Slipper functions

-- | A Slipper is like a zipper, but it is only for examining, not modifying.
-- A Slipper slips along the paths of a tree, but doesn't reconstruct it, 
-- i.e. it doesn't zip up a new tree when backing out.
-- Note that we conceive of Trees with root at left, branching to the right.
-- The current implementation of Slipper is simply a list of Tree nodes: 
-- the current node followed by its parent and other ancestors, in reverse 
-- order.  We move left by popping nodes off the list.  Moving right 
-- means extracting the zero or one node from the current node and then 
-- pushing it onto the list.  (The first node is neither 0 nor 1; it 
-- represents the empty sequence.)

type Slipper a = [Tree a]

-- I want to display the values of the two children as well as of the current:
showCurr :: (Show a) => Slipper a -> String
showCurr ( (Node p (Node pz _ _) (Node po _ _)) : ns ) =
        "<"++(show p)++" [z: "++(show pz)++" o: "++(show po)++"]>"
showCurr ( (Node p Leaf (Node po _ _)) : ns ) =
        "<"++(show p)++" [z: "++"leaf"++" o: "++(show po)++"]>"
showCurr ( (Node p (Node pz _ _) Leaf) : ns ) =
        "<"++(show p)++" [z: "++(show pz)++" o: "++"leaf"++"]>"
showCurr (Leaf:ns) = "<leaf>"

-- | Initialize a slipper at the beginning of a tree.
slipOnto :: Tree a -> Slipper a
slipOnto node = [node]

-- | Move right to the zero node
goZero :: Slipper a -> Slipper a
goZero nodes@((Node _ next0 _):ns) = next0:nodes

-- | Move right to the one node
goOne :: Slipper a -> Slipper a
goOne nodes@((Node _ _ next1):ns) = next1:nodes

-- | Move left to the parent node.
goPrev :: Slipper a -> Slipper a
goPrev (_:ns) = ns

----------------------------------------------

-- not test data; these are essential
zeroPayoutsTree :: Tree Double
zeroPayoutsTree = Node 0.0 zeroPayoutsTree zeroPayoutsTree
-- without cycles: zeroNodes _ = Node 0.0 (zeroNodes 42) (zeroNodes 42)

onePayoutsTree  :: Tree Double
onePayoutsTree  = Node 1.0 onePayoutsTree onePayoutsTree


{- |
'lowerPayouts len' returns a list of payout components for a generator of 
length len.  The first element corresponds to the empty string; the last 
corresponds to the position one less than the length of the generator.  
These are payouts correspondng to the string up to that point.
-}
lowerPayouts :: Int -> [Double]
lowerPayouts len = map (2^^) [-len .. -1] -- from D&H: map ((2**) . (-len_s +)) [0 .. len_s-1]


{- |
Add payouts for generator string with lower payouts to tree.  The first payout
corresponds to the empty string.
Examples: addPayouts generator (lowerPayouts (length generator)) zeroPayoutsTree
          addPayouts "01010" (lowerPayouts 5) zeroPayoutsTree
-}
addPayouts (g:gs) (p:ps) (Node x next0 next1)
  | g == '0' = Node {payout=(x+p), nextZero=(addPayouts gs ps next0), nextOne=next1}
  | g == '1' = Node {payout=(x+p), nextZero=next0, nextOne=(addPayouts gs ps next1)}
  | otherwise = undefined -- no other characters allowed
addPayouts "" [] _ = onePayoutsTree -- default case when lists exhausted
addPayouts _  _  _ = undefined      -- lists should be same length, Leafs not allowed


{- | Convenience function to addPayouts to a tree directly from a generator -}
addGeneratorPayouts tree generator =
    addPayouts generator (lowerPayouts (length generator)) tree
        
{- | Sum payouts from all generators in list generators. See addPayouts. -}
sumGeneratorSet generators =
    foldl addGeneratorPayouts zeroPayoutsTree generators
-- to use foldl vs foldr, swap order of args for addGeneratorPayouts

{- | Transform each generator set in a list into a test tree. See addPayouts. -}
makeMLtest :: [[String]] -> [Tree Double]
makeMLtest generator_set_list = map sumGeneratorSet generator_set_list

{- | 
Tests whether node satisfies the martingale property, assigning equal 
probability to each branch.  In other words, is the simple average of the 
two child payouts equal to the parent payout for each (non-Leaf) node?
-}
isMartingaleNode (Node p (Node zp _ _) (Node op _ _)) = (zp + op)/2 == p
isMartingaleNode (Node _ Leaf Leaf) = True -- OK for a truncated tree
isMartingaleNode _ = False -- unbalanced (?)

{- | 
Tests whether a tree satisfies the martingale property, assigning equal 
probability to each branch. In other words, is the simple average of the 
two child payouts always equal to the parent payout for each non-Leaf node?
This obviously won't work with infinite trees.
Example to show that the first 50 test sets for martingale repreentation mm
of an M-L test are martingale up to 15 deep, each:
   take 50 $ map (\t -> isMartingaleTree (takeTree 15 t)) mm
Remember that increasing depth needs exponentially more space and time.
-}
isMartingaleTree Leaf = True
isMartingaleTree top@(Node _ z o) =
    isMartingaleNode top && isMartingaleTree z && isMartingaleTree o
-- Note can't use fmap in any obvious way here because it maps over *payouts*,
-- and this function has to map over *nodes*, since it has to get the
-- next nodes as well.  What I need is more like a fold.



-- Folds inspired by Data.Tree's rosetree fold, but the function argument here
-- operates on nodes, not node values (payouts).

-- v3:
-- foldTree f = go where
--    go n@(Node p z o) = f n z o

-- v2:
-- foldTree f = go where

-- attempt to define isMartingaleTree in terms of above foldTree
-- ismt Leaf = True
-- ismt node = foldTree f node where
--    f n ns = isMartingaleNode n && foldl ismt ns


----------------------------------------------------------
-- Borrow tools from Data.Tree
-- I don't want that representation, but by converting to it, we get access
-- to one more more useful functions.

{- | Convert our Tree to Data.Tree -}
toDataTree Leaf = Data.Tree.Node (-42.0) []  -- Have to have a label, even for a leaf node
toDataTree (Node p next0 next1) =
    Data.Tree.Node p [toDataTree next0, toDataTree next1]

{- | Draw an ASCII diagram of a tree using Data.Tree.drawTree, other functions. -}
drawTree tree = putStr $ Data.Tree.drawTree $ fmap show (toDataTree tree)

----------------------------------------------------------
-- Convenience functions for constructing M-L tests

consOtherDigit initstr [] = initstr
consOtherDigit _ (s:ss) =
    if s == '1'
       then '0':s:ss
       else '1':s:ss

combineMLtests (x:xs) (y:ys) = (union x y) : combineMLtests xs ys
combineMLtests xs []         = xs
combineMLtests [] ys         = ys

--------------------------------------------------
-- Example generator sets for M-L tests

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

-- Remove the first set since it includes "", a prefix to other elements,
-- and remove the second since it includes "1", which is a prefix for "110".
multisize :: [[String]]
multisize = drop 2 (foldr combineMLtests [[]] 
                          [terminal_ones, drop 2 terminal_zeros, 
                          drop 4 terminal_ones, drop 6 zeros])

-- Note that even though there are shared prefixes, no string in any
-- generator set is a prefix of any other (since they're all the same
-- length!).
someofem = foldr combineMLtests [[]] 
                 [zeros, ones, terminal_zeros, terminal_ones, zero_ones, 
                 one_zeros]
-- don't include e.g. multisize--you'll get prefix relations


-----------------------------------------------
-- Random generation of generator sets

-- System.Random uses a Steele et al. SplitMix PRNG:
-- https://hackage.haskell.org/package/random-1.2.0/docs/System-Random.html

-- this experiment doesn't work, though innards works in ghci:
-- r = do {return getStdRandom (randomR (1,6))}

topRng = getStdGen

generateGenerators rng maxLen =
    let randSeq = randomRs (0,1) rng
        rng' = newStdGen
        selectStrings g seq =
            let (len, g') = randomR (1, maxLen) g
            in (take len seq) : (selectStrings g (drop len seq))
     in selectStrings rng' randSeq



{- cruft:
    where generateOne seq =
       let (len, g) = randomR (1,max_len) rng in
                newString = take n_generators $ randomRs (0,1) g
                restOfSeq = drop n_generators $ randomRs (0,1) g
             in (newString, restOfSeq)
-}




{-
-- some test data
gen1 = "101110110"
gen2 = "1001001010101"
gp1 = addPayouts gen1 (lowerPayouts (length gen1)) zeroPayoutsTree
gp2 = addPayouts gen2 (lowerPayouts (length gen2)) gp1
g2s = sumGeneratorSet [gen1,gen2]
-}
