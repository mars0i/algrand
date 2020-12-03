


-- Also:
-- System.Random.SFMT, package sfmt: deps on newer lib versions than stackage
-- AC-Random: from 2011, and only compared with older version of Random
-- Some crypto PRNGs: tf-random, threefish, cryptonite (includes lots)

import System.Random -- built-in interface and PRNG using splitmix
-- import System.Random.SplitMix -- package splitmix
import System.Random.Mersenne.Pure64 -- package mersenne-random-pure64
import System.Random.Mersenne -- package mersenne-random
import Data.PCGen -- package pcgen, an O'Neill PCG PRNG

-- I think that System.Random 1.2, based on SplitMix, is fine for
-- many uses.  It has a period of 2^64, though, which might not be 
-- enough for some simulations.


{- Example:

Prelude> import System.Random.Mersenne.Pure64

Prelude System.Random.Mersenne.Pure64> :browse System.Random.Mersenne.Pure64
newPureMT :: IO PureMT
pureMT :: GHC.Word.Word64 -> PureMT
randomDouble :: PureMT -> (Double, PureMT)
randomInt :: PureMT -> (Int, PureMT)
randomInt64 :: PureMT -> (GHC.Int.Int64, PureMT)
randomWord :: PureMT -> (Word, PureMT)
randomWord64 :: PureMT -> (GHC.Word.Word64, PureMT)
data PureMT
  = System.Random.Mersenne.Pure64.Internal.PureMT {-# UNPACK #-}System.Random.Mersenne.Pure64.Internal.MTBlock
                                                  {-# UNPACK #-}Int
                                                  System.Random.Mersenne.Pure64.Internal.MTBlock

Prelude System.Random.Mersenne.Pure64> dubsteps (d,g) = (d,g):dubsteps (randomDouble g)

Prelude System.Random.Mersenne.Pure64> ds = let (d,g) = randomDouble (pureMT 123456) in dubsteps (d,g)

Prelude System.Random.Mersenne.Pure64> map fst $ take 6 ds
[0.17995875420864893,0.7752018151231932,0.8766754260595218,0.6570175765883899,0.17291701417426397,0.5726678439697035]

Prelude System.Random.Mersenne.Pure64> cs = dubsteps (0, pureMT 18)

Prelude System.Random.Mersenne.Pure64> map fst $ take 6 cs
[0.0,0.3796381243235504,0.7723819137789838,0.20093089299404499,0.3533673579280684,0.7696927096234812]

-}
