## design

- requirements:
  - stemming
  - ignore case
  - stopwords list
  
- query categories (parsed in server side -- scala)
  - free text: word "word" word ...
  - attribute: <field>:"word word..." and/or <field>:"word" ...

```
Q := | [\epsilon]
     | W Q
     | "W" Q
     | F:W L Q

W := | ^[A-z0-9]+$

L := | AND | OR
```
- "index" should be totally from client side (jquery POST), response changes statsUI
- "search" is mainly from server side, jquery only jumps to required uri (with params), no GET


## BUGS
- "xxx/yyy" error search
- ignore case doesn't work?

## TODO

[x] maybe original code is incomplete for a rest request ==> renderJSON
[x] no response should be my fault because postman can get response
[x] get content of searchBox, process it, and pass it with POST
[x] use another stopword dictionary
[x] search should be something like "https://github.com", the url has changed --> mostly scala
- add "HIGHLIGHT"
- boolean retrieval model?
  - LIMIT! /3 STATUTE ACTION /S FEDERAL /2 TORT /3 CLAIM
- effect of lowecasse/stemming/stopwords
- "phrase query" should use different indexer!!!
- tolerant retrieval
- use a special field "ALL" to for "free text" search, others by combination
- see whether can using "merge"
- authors indexed with same field
- reopen directory reader

- app1 for filtered cases


## notice:
- removed dblp.xml for "author" attribute (`aux`) since it doesn't correspond to dtd
- ignored tags (i, sup, sub, tt) in xml

## example:
+pubYear:2007 +venue:datenschutz +authors:thilo