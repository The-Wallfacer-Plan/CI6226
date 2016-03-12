## TODO

[x] maybe original code is incomplete for a rest request ==> renderJSON
[x] no response should be my fault because postman can get response
[x] generate dynamic search result with javascript (cannot use scala directly?)
[x] get content of searchBox, process it, and pass it with POST
[x] use another stopword dictionary
- boolean retrieval model?
  - LIMIT! /3 STATUTE ACTION /S FEDERAL /2 TORT /3 CLAIM
- effect of lowecasse/stemming/stopwords
- "phrase query" should use different indexer!!!
- tolerant retrieval

## notice:
- modified dblp.xml for "author" attribute since it doesn't correspond to dtd
- ignored tags (i, sup, sub, tt) in xml