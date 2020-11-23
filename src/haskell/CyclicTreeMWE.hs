import qualified Data.Tree

data Tree a = Leaf | Node {value :: a,
                           left :: (Tree a),
                           right :: (Tree a)}
                           deriving (Show, Eq)  

instance Functor Tree where  
    fmap f Leaf = Leaf
    fmap f (Node v l r) = Node (f v) (fmap f l) (fmap f r)


-- Ack!  It's not breaking on the cyclic tree.
foo (d:ds) (Node v l r)
  | d == 'L' = Node "Go left" (foo ds l) r
  | d == 'R' = Node "Go right" l (foo ds r)
  | otherwise = undefined -- illegal direction
foo _ (Node v l r) = Node "Done" l r -- no more processing

truncateTree _ Leaf = Leaf
truncateTree n (Node v l r) =
    if n <= 0
       then Leaf
       else Node v (truncateTree (n-1) l) (truncateTree (n-1) r)

-- These are supposed to break foo, and they don't!
infiniteCyclicNodes = Node "i" infiniteCyclicNodes infiniteCyclicNodes
notbadCyclicNodes = truncateTree 4 $ infiniteCyclicNodes
goodCyclicNodes = truncateTree 4 $ Node "g" goodCyclicNodes goodCyclicNodes

-- This is supposed to and does work.
novelNodes = truncateTree 4 $ newzeros ()
    where newzeros _ = Node "n" (newzeros ()) (newzeros ())

----------------------------------------
-- Handy utility code

toDataTree Leaf = Data.Tree.Node "End" []
toDataTree (Node v l r) = Data.Tree.Node v [toDataTree l, toDataTree r]

drawTree tree = putStr $ Data.Tree.drawTree $ fmap show (toDataTree tree)

boundedTreeEqual _ Leaf Leaf = True
boundedTreeEqual _ Leaf _    = False
boundedTreeEqual _ _ Leaf    = False
boundedTreeEqual n (Node p1 z1 o1) (Node p2 z2 o2) =
    if n <= 0
       then p1 == p2
       else (boundedTreeEqual (n-1) z1 z2) && (boundedTreeEqual (n-1) o1 o2)
