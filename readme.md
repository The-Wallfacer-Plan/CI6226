## design

- "index" should be totally from client side (jquery POST), response changes statsUI
- "search" is mainly from server side, jquery only jumps to required uri (with params), no GET

## TODO

- add "HIGHLIGHT"
   - LIMIT! /3 STATUTE ACTION /S FEDERAL /2 TORT /3 CLAIM
- effect of lowecasse/stemming/stopwords
- tolerant retrieval
- see whether can using "merge"
- reopen directory reader
- write unit test
- note: won't add "weak phrase option"

## notice:
- removed dblp.xml for "author" attribute (`aux`) since it doesn't correspond to dtd
- ignored tags (i, sup, sub, tt) in xml

## example:
- +pubYear:2007 +venue:datenschutz +authors:thilo
- +pubYear:2000 +venue:"ACM SIGMOD Anthology" +authors:"Michael Ley"

## setup
- Requirement:
  1. Oracle Java 8 is required and should be pre-installed; if you have multiple JREs, please ensure `JAVA_HOME` is correctly pointed to the Java 8 directory
  1. `LSearcher` is based on [Scala](http://www.scala-lang.org/) and [Play!](https://www.playframework.com/), you can use [activator](https://www.lightbend.com/activator/download) (embedded in our project) to get them if `bash` is available
- Deployment

```shell
git clone https://github.com/HongxuChen/ci6226.git
cd ci6226/
./activator # or download activator yourself and set the executable inside your PATH environment and run `activator` in project directory
run -Dhttp.port=9001 # (inside activator shell)
open http://localhost:9001 in your favorite web browser
```

- Try it at http://155.69.145.146:9001
