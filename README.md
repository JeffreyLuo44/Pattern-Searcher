-------------------------------------------------
| Pattern Search - Regex Compiler, Searcher     |
| Jedd Lupoy (1536884) & Jeffrey Luo (1535901)  |
-------------------------------------------------
Grammar:
E -> T
E -> TE
T -> F
T -> F*
T -> F+
T -> F?
T -> F|E
F -> .
F -> v
F -> \v
F -> (E)

USAGE: java REcompile <REGEX> | java REsearch <file to be searched>

Other Notes:
Square brackets are preprocessed. For example, [ab?c] becomes (a|b|\?|c).
Two or more special characters in a row is not allowed (except for parenthesis). For example, "a**", "bc???" or "bc*?" are not allowed.
