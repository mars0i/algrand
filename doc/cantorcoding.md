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

## exploratory notes:

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
