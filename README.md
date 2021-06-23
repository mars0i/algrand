# algrand
A motley collection of code written to help me think about algorithmic
randomness. and pseudorandom number generation.

Most of this is in Clojure. The Clojure source is under *src/clj*`, with
the fun stuff in *src/clj/algrand*.  (Traditionally, I've used Sean
Luke's Java Mersenne Twister for pseudorandom number generation with
Clojure, but that may change.)

There are also a few functions written for the Racket dialect of Scheme
in *src/racket*.  Racket is useful here because it makes it easy to
enter and print out fractional numbers in human-readable binary form,
and because one can easily make plots using numbers in such forms.

See files in doc/ directory or docstrings and comments in source files
for brief documentation.
