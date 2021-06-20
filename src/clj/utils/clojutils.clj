(ns utils.clojutils)

(defmacro add-to-docstr
  "Macro that appends string addlstr onto end of existing docstring for 
  symbol sym (which need not be quoted).  Can be used to add a docstring to 
  a symbol that has none."
  [sym addlstr] 
  `(alter-meta! #'~sym update-in [:doc] str ~addlstr))

