## design
  
- "index" should be totally from client side (jquery POST), response changes statsUI
- "search" is mainly from server side, jquery only jumps to required uri (with params), no GET


## BUGS
- "xxx/yyy" error search

## TODO

[x] maybe original code is incomplete for a rest request ==> renderJSON
[x] no response should be my fault because postman can get response
[x] get content of searchBox, process it, and pass it with POST
[x] use another stopword dictionary
[x] search should be something like "https://github.com", the url has changed --> mostly scala
- add "HIGHLIGHT"
[-]- boolean retrieval model?
   - LIMIT! /3 STATUTE ACTION /S FEDERAL /2 TORT /3 CLAIM
- effect of lowecasse/stemming/stopwords
- tolerant retrieval
- see whether can using "merge"
[x] authors indexed with same field
- reopen directory reader
[x] app1 for filtered cases!!!
[x] add search limit for A2
- write unit test
- note: won't add "weak phrase option"


## notice:
- removed dblp.xml for "author" attribute (`aux`) since it doesn't correspond to dtd
- ignored tags (i, sup, sub, tt) in xml

## example:
+pubYear:2007 +venue:datenschutz +authors:thilo
+pubYear:2000 +venue:"ACM SIGMOD Anthology" +authors:"Michael Ley"