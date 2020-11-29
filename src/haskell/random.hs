


-- Also:
-- System.Random.SFMT, package sfmt: deps on newer lib versions than stackage
-- AC-Random: from 2011, and only compared with older version of Random
-- Some crypto PRNGs: tf-random, threefish, cryptonite (includes lots)

import System.Random -- built-in interface and PRNG using splitmix
-- import System.Random.SplitMix -- package splitmix
import System.Random.Mersenne.Pure64 -- package mersenne-random-pure64
import System.Random.Mersenne -- package mersenne-random
import Data.PCGen -- package pcgen, an O'Neill PCG PRNG
