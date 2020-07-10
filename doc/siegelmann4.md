siegelmann4.md
===

Notes on code based on chapter 4 of Sieglemann's *Neural Netowrks and
Analog Computation*.  

Circuit retrieval algorithm on page 65, implemented in file siegelmann4.clj:

I'll refer to the step in which u appears as tick 1 or step 1.
So steps are 1-based, but matrix and vector indexes are 0-based.

All elements of the state vector start as zero.

c-hat is the col 9 to row 10 weight.

u is added only on the first tick.  Not before, not after.  It's
replaced with zero after tick 1.


1. So x9 is nonzero only on the first tick.  That causes c-hat to be
placed in x10 on the second tick (since x0 through x8 are still zero).
This is the *only* time that c-hat as a weight is used directly.
After that, information from it shuttles around in the x0 through x8
nodes and x10.  (Pieces of c-hat get into x10 not because of the c-hat
weight on x9, but because x10 is a linear combination of x0 through x8.)

2. x10 and x0 through x8 implement the lambda-tilde shift operator,
but they do a bit more than that in the network.  At each subsequent
step, x0 through x8 will contain either:

	a. A number that contains the left-shift of the decimal part of
	what's left of c-hat in x_j for j in {0,2,4,6,8}.  This will
	be the next value of x10.

	b. 1 in x_i for i < j.  There is alwasy an even number of
	these 1's, so on the next tick, they will sum to zero in the
	the linear combination that produces x10.

	c. 0 in x_i for i > j.

3.  x11 reconstructs digits from the 1's in x0-x7, and places them on
the front of x11:

	2 times the 4-part sum in x11 is the digit that was just
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
	(And the registers in x0-x8 above the c-hat float are zero.)
	So 2(x1+x3+x5+x7) gives you the digit that was just shifted off.

	2(x1+x3+x5+x7) is an integer, so we need to right-shift it; 1/9
	times 2(x1+x3+x5+x7) right-shifts the integer into the first
	decimal place.  x12 had the old version of the partially
	constructed circuit encoding, which we right-shift by (1/9)x12
	to make space for the newly reconstructed digit.

	(This is why the encoding in c-hat has to be backwards: you're
	pulling digits off the stack that came from c-hat, and then
	pushing them onto the stack in x12 and x11, so the order gets
	reversed.)

3. 8's

4. counting by shifting u

5. putting the output on x16
