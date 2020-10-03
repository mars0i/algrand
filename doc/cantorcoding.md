Notes on Cantor coding
===

## background

Siegelmann and Sontag introduced Cantor coding--or see Siegelmann's
book--but Tsuda, and Tsuda and Kuroda introduced the term in 2001.

Siegelmann uses two methods for Cantor coding:

* Multiply each digit by 2.  For example, Siegelmann's
  base-9 coding in chapter 4 uses 0, 2, 4, 6, 8.  Note that there is no
  gap after 8; 0 is the next digit.  (In convertbase.clj, this operation
  is performed by `cantor-code-0`.)

* Multiply each digit by 2 and add 1.  For example, Siegelmann's base-2
  to base-4 coding in chapter 3, uses 1 and 3 in base 4, to code 0 and 1
  in base 2.  Here note that there's a gap above the largest allowed
  digit.  After 3 comes 0, which is not legal in the Cantor code.  (In
  convertbase.clj, this operation is performed by `cantor-code-1`.)


## Cantor-code arithmetic

You can't use normal arithmetic operators to perform arithmetic within a
Cantor code.  For example, in zero-based a base 4 Cantor code for base
2, 0 represents 0, and 2 represents 1.  So 2 + 2 in the Cantor code
should equal 20 (since 1 + 1 in base 2 equals 10).  However, if you use
the normal addition function in base 4, you'll get 2 + 2 = 10 rather
than the desired result 20.

So if you want to e.g. translate an LCG so that it performs all of its
operations literally within a Cantor code, you would have to define
special arithmetic functions.

(Of course you could just take a Cantor-coded seed and decode it, and
then perform the normal LCG operation on it, then translating the
result into the Cantor-coded representation, but that's uninteresting
cheating--although it really is the same, in the sense that the Cantor
arithmetic operators are in effect simply performing the translation
within the arithmetic function.)

## addition

The tricky part is addition, because carrying works differently in
Cantor coding.  e.g. in a zero-based, base-6 representation of base-3,
`2+2=11` in base 3 is represented as `4+4=22`.  I've now implemented
addition for zero-based Cantor coded numbers (currently `cantor+` in
convertbase.clj).

One-based addition is trickier, and not worth doing unless there's a
good reason to use one-based Cantor-coding for other reasons.  Here 
are some rules that it would have to be satisfied e.g.for a base-6
representation of base 3:
```
1 + anything = anything  (since 1 means zero)
3 + 3 = 5                (since 3 means 1, and 5 means 2)
5 + 3 = 30               (i.e. 2 + 1 = 10 in base 3)
5 + 5 = 33               (i.e. 2 + 2 = 11 in base 3)
```
You can do that, but it's tricky.  The easiest way to do this would
simply be to subtract from each digit on input, use zero-based
addition, and then add one to each digit on output.

## multiplication

Multiplication also involves carrying.  If the multiplier is a power
of the Cantor base, then the multiplication is simply a shift, i.e.
you add zero to the end of the numeric representation (like
multiplying by 10 in base 10).

Instead of trying to implement an entire multiplication function with
carrying fromn scratch, you can use the fact that every number is a
linear combination of powers of the base, so $xy = (x_n + \cdots +
x_m)y$, where the $x_i$ are the powers of the base that sum to $x$.
So multiplication in general can be implemented by adding together a
bunch of shifts (i.e. tacking on zeros).


## old exploratory notes:

Suppose you're c-coding base 2 in base 4.  What if you used only
multipliers such that their effect was to perform a base-4 shift that
preserved the c-code.  And your modulus is such that that you're just
shifting digits off the left end of the number.

Now you also need an increment addition; otherwise you're going to end
up with a bunch of zeros on the right. And the increment addition has
to be such that it also preserves the c-code.  

Is this possible?

```clojure
(def ns4 (partial number-to-string 4 0))
(def n 4r2022002020)
(def mul (expt 4 4))
(def m (expt 4 6))
(ns4 (mod n m)) ;=> "2020."
```

The increment is the tough part.  Because we're assuming it iself is
c-coded, but then you will have carries in some cases that escape the
c-code.

So maybe just implement a special addition function?
experiment: `algrand/cantor-+`
