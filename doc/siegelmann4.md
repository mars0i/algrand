siegelmann4.md
===

Notes on code based on chapter 4 of Sieglemann's *Neural Netowrks and
Analog Computation*.  

-----------

#### Lemma 4.1.2, stated on page 63

The statement about the value of $x_r$ is misleading.  It suggests that
there is a node value that will consist of a bunch of zeros, followed by
the encoding string, followed by more zeros, but that's not what the
algorithm in the proof provides.  Rather, what you get is that node
$x_{16}$ contains zero during each step until the specified step (but
see below about the count), and then in the specified step it contains
the encoding string, and then after that it goes back to zero
henceforth.

Note two off-by-one-ish errors in Lemma 4.1.2:

- The network needed in the proof has 17 nodes (numbered 0 through
  16), not 16.

- The number of zero steps before the encoding appears is two less
  than what's stated.  You can either change $n$ to $n-1$, or change 5
  to 3.  (This becomes clear when you run the algorithm.  Yes, I'm
  taking into account that the step numbering is 1-based.)

#### Circuit retrieval algorithm on page 65, implemented in file siegelmann4.clj

This is the core of the proof of Lemma 4.1.2.

I'll refer to the step in which u appears as tick 1 or step 1.  So steps
are 1-based, while matrix and vector indexes will be 0-based.

All elements of the state vector (of length 17) start as zero.

I formulate the state update algorithm using matrices and vectors, in
terms of Equation (2.2), p. 19.

c-hat, i.e. $\hat{C}$, is the col 9 to row 10 weight.

$u$ is added only on the first tick.  Not before, not after.  It's
replaced with zero after the first tick.


1. So $x_9$ is nonzero only on the first tick.  That causes c-hat to be
placed in $x_{10}$ on the second tick (since $x_0$ through $x_8$ are still zero).
This is the *only* time that c-hat as a weight is used directly.
After that, information from it shuttles around in the $x_0$ through $x_8$
nodes and $x_{10}$.  (Pieces of c-hat get into $x_{10}$ not because of the c-hat
weight on $x_9$, but because $x_{10}$ is a linear combination of $x_0$ through $x_8$.)

2. $x_{10}$ and $x_0$ through $x_8$ implement the lambda-tilde shift operator,
but they do a bit more than that in the network.  At each subsequent
step, $x_0$ through $x_8$ will contain either:

	a. A number that contains the left-shift of the decimal part of
	what's left of c-hat in x_j for j in {0,2,4,6,8}.  This will
	be the next value of $x_{10}$.

	b. 1 in x_i for i < j.  There is alwasy an even number of
	these 1's, so on the next tick, they will sum to zero in the
	the linear combination that produces $x_{10}$.

	c. 0 in x_i for i > j.

3.  $x_{11}$ reconstructs digits from the 1's in $x_0$--$x_7$, and places them on
the front of $x_{11}$:

	- 2 times the 4-part sum in $x_{11}$ is the digit that was just
	stripped off.  This is not obvious, but the count of 1's below
	the newly shifted float (derived from c-hat) is equal to the
	digit that was shifted off.  You might think you could instead
	just sum all of the 1's.  But you have to choose input nodes
	that will work every time, and then you'd accidentally add in
	the register with a float in it.  The trick that the algorithm
	uses is based on the fact that the new piece of c-hat is always
	in an even-numbered node, so if you only use odd-numbered
	registers, you're guaranteed to miss it.  Since there is always
	an even number of 1's below the piece of c-hat, doubling the odd
	nodes gives you the same count as if you counted all of the 1's.
	(And the registers in $x_0$--$x_8$ above the c-hat float are
	zero.) So 2($x_1+x_3+x_5+x_7$) gives you the digit that was just
	shifted off.

	- 2($x_1+x_3+x_5+x_7$) is an integer, so we need to right-shift
	it; 1/9 times 2($x_1+x_3+x_5+x_7$) right-shifts the integer into
	the first decimal place.  $x_{12}$ had the old version of the
	partially constructed circuit encoding, which we right-shift by
	(1/9)$x_{12}$ to make space for the newly reconstructed digit.

	(This is why the encoding in c-hat has to be backwards: you're
	pulling digits off the stack that came from c-hat, and then
	pushing them onto the stack in $x_{12}$ and $x_{11}$, so the
	order gets reversed.)

4.  Counting through the circuits:

	- $u$ is also placed in $x_{13}$ on tick 1.  (At that point
	$x_{14}$ and $x_{15}$ are zero, so contribute nothing.)  Again,
	$u$ as a value coming from the input stream will never appear
	again after this tick.

	- This value is then copied to $x_{14}$ or $x_{15}$, but not
	  both.  

	- If $x_7=0$ (i.e. the last digit shifted off the c-hat string
	  is not 8), $x_{13}$ is simply copied to $x_{15}$, which is
	  then copied back to $x_{13}$, and so on.  Vamping to
	  maintain storage of this value.

	- Meanwhile, $x_{14}$ stays zero, since 2 is greater than a
	  left-shifted $u$-string.

	- But when $x_7$ is 1 (i.e. when the last digit shifted off of
	  the c-hat string is 8), $x_15$ become zero, and now $x_{14}$
	  is in effect $\sigma(2x_{13}-1)$.  There the 1 simply strips
	  off the 1 newly shifted into the integer position.
	  (Remember, $u$ consists of a series of 1's indicating how
	  many circuits to count until.)

	- The resulting value will then be copied back to $x_{13}$.
	  So this is how we increment the counter by stripping a digit
	  from the $u$ string, when an 8 is encountered in c-hat.

	- The preceding assumed that 1's remained from $u$.  When all
	  that's left are zeros, $x_{14}$ and $x_{15}4, and hence
	  $x_{13}$, will be forced to zero.

4. blah blah $x_{13}$ in $x_{11}$.

5. putting the output on $x_{16}$
