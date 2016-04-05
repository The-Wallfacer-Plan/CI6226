LSearcher: A DBLP Search Engine Based on Lucene
===============================================

Overview
--------

## LSearcher consists of two projects:

- Project 1 is a basic search engine that can be used to search for the publication records in DBLP.
- Project 2 contains two applications.
  - Application 1 finds the top-10 most popular research topics in each year from Year 2000 to 2015, and in each year for a specific publication venue or an author;
  - Application 2 discovers the top-10 most similar publication venues for a given conference or journal.

### Resources and toolkits used in LSearcher:

  (1) Dataset in compressed XML in DBLP: http://dblp.uni-trier.de/xml/
  (2) Oracle Java 8: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
  (3) Lucene 5.5.0: https://lucene.apache.org/ 
  (4) Play! framework 2.5.0 : https://www.playframework.com/
  (5) bootstrap-v4-alpha4: http://v4-alpha.getbootstrap.com/getting-started/introduction/
  (6) jQuery-2.2: https://jquery.com/download/
  (7) Scala-2.11.7: http://scala-lang.org/
  (8) Mallet library (MAchine Learning for LanguagE Toolkit): http://mallet.cs.umass.edu/
  (9) Lightbend Activator£ºhttps://www.lightbend.com/activator/download

### Setup  

The source code of the system is available on GitHub from https://github.com/HongxuChen/ci6226,  a demo is currently available on http://155.69.145.146:9001 (NTU access only). 

  - Requirement:
    1. Oracle Java 8 is required and should be pre-installed; if you have multiple JREs, please ensure `JAVA_HOME` is correctly pointed to the Java 8 directory
    1. `LSearcher` is based on [Scala](http://www.scala-lang.org/) and [Play!](https://www.playframework.com/), you can use [activator](https://www.lightbend.com/activator/download) (embedded in our project) to get them if `bash` is available
  - Deployment  

      git clone https://github.com/HongxuChen/ci6226.git
      cd ci6226/
      # put downloaded dblp.xml file to "public/resources"
      # change "rootDir" value in app/models/common/Config.scala and point to "public/resources"
      ./activator # for Linux/Mac OS X
      # On Windows, you download activator and run executable `activator` in project directory
      run -Dhttp.port=9001 # (inside activator shell)
      open http://localhost:9001 in your favorite web browser


Functionalities and Results
-------------------------

There are three tags at the top of the interface. Tag "Home" is the basic search engine, and Tags "App1" and "App2" are the first and second applications in Project 2.

## Basic Search

It is tagged by "Home" at the top of the UI.
(1) The first step is to determine the settings of the system. There are three kinds of options for the indexing process: stemming or not, case sensitivity or not, and stopwords removal or not. You can also select how many records to be display and which kind of similarity is used (classic similarity or BM25 similarity). 

(2) Once the system is configured, the second step is to index the collection by clicking the "Index Docs" button. When the indexing process is finished, the textbox at the bottom returns the options settings and indexing time.

(3) At the third step, you can enter your query in the input textbox. It accepts many kinds of queries. 
     (a) The simple queries are single terms or phrases. A single term is a single word, e.g., system; while a phrase is a group of words surrounded by double quotes, e.g., "system model". 
       For a single query "system", the first resulted record is described as follows.
                 docID: 1134718   Score: 1.522377
                 pubYear	1992
                 paperId	journals/csys/Neuman92
                 authors	B. Clifford Neuman
                    kind	article
                   title	The Prospero File System: A Global File System Based on the Virtual System Model.
                   venue	Computing Systems
        For the phrase "system model" (must be surrounded by double quotes), the first record is the following one.
                 docID: 356624    Score: 2.047171
                 pubYear	2011
                 paperId	journals/procedia/Strand11
                 authors	Gary Strand
                    kind	inproceedings
                   title	Community Earth System Model Data Management: Policies and Challenges.
                   venue	ICCS
      (b) The queries can also specify the field. Without a detailed field, the search will consider all terms in the records. However, once the field is specified, the search will only consider the terms in the matched field. The format of a query with a field can be described as: field: <term> or field:"<phrase>". For example, to search the records whose titles contain "system model", the query can be described as: title: "system model". The record listed at the first place is the following one:
                 docID: 570186    Score: 4.107412
                 pubYear	2008
                 paperId	journals/istr/ProbstH08
                 authors	Christian W. Probst;Ren¨¦ Rydhof Hansen
                    kind	article
                   title	An extensible analysable system model.
                   venue	Inf. Sec. Techn. Report

       (c) For a single term query with or without a field, it also support single and multiple character wildcard searches. For a single character wildcard, use the "?" symbol; for a multiple character wildcard, use the "*" symbol. The single character wildcard search looks for terms that match that with the single character replaced, and multiple character wildcard searches looks for 0 or more characters. 
For example,  to search for model, modeled or modeling, the query can be described as: model*. With this query, some returned records are:
       Record 1: docID: 19        Score: 1.000000
                 pubYear	2015
                 paperId	journals/twc/KhademiCIJV15
                 authors	Seyran Khademi;Sundeep Prabhakar Chepuri;Zoubir Irahhauten;Gerard J. M. Janssen;Alle-Jan van der Veen
                    kind	article
                   title	Channel Measurements and Modeling for a 60 GHz Wireless Link Within a Metal Cabinet.
                   venue	IEEE Transactions on Wireless Communications
       Record 2: docID: 35        Score: 1.000000
                 pubYear	2004
                 paperId	journals/twc/XuCHV04
                 authors	Hao Xu;Dmitry Chizhik;Howard C. Huang;Reinaldo A. Valenzuela
                    kind	article
                   title	A generalized space-time multiple-input multiple-output (MIMO) channel model.
                   venue	IEEE Transactions on Wireless Communications
       Record 3: docID: 133      Score: 1.000000
                 pubYear	2007
                 paperId	journals/twc/HanT07
                 authors	Y. Han;Kah Chan Teh
                    kind	article
                   title	Performance Study of Asynchronous FFH/MFSK Communications using Various Diversity Combining Techniques with MAI Modeled as Alpha-Stable Process.
                   venue	IEEE Transactions on Wireless Communications

To search for test or text, the query can be written as: te?t. Some sample returned records are:
       Record 1: docID: 1939      Score: 1.000000
                 pubYear	2013
                 paperId	journals/twc/HuangC13a
                 authors	Qi Huang;Pei-Jung Chung
                    kind	article
                   title	An F-Test Based Approach for Spectrum Sensing in Cognitive Radio.
                   venue	IEEE Transactions on Wireless Communications
       Record 2: docID: 3066      Score: 1.000000
                 pubYear	2010
                 paperId	journals/siu/BellissensJDM10
                 authors	C¨¦drick Bellissens;Patrick Jeuniaux;Nicholas D. Duran;Danielle S. McNamara
                    kind	article
                   title	A Text Relatedness and Dependency Computational Model.
                   venue	Stud. Inform. Univ.
       (d) A query can also described as regular expression matching a pattern between forward slashes "/". For example, to find records containing "text" or "test", the query can be written as /te[xs]t/. The results of this example are the same as the query te?t.
       (e) The query can also be a fuzzy expression by using the tilde "~". An additional (optional) parameter can specify the maximum number of edits allowed. The value is between 0 and 2, and the default is 2 edit distances. For example, the following query can used to search for a term similar in spelling to text: text~, i.e., text~2 since 2 is default. This query can return records containing text, texts, test, and so on. For example,
        Record 1: docID: 691645    Score: 0.592871
                 pubYear	2013
                 paperId	journals/corr/abs-1305-2831
                 authors	Khushboo Thakkar;Urmila Shrawankar
                    kind	article
                   title	Test Model for Text Categorization and Text Summarization
                   venue	CoRR  
        Record 2: docID: 2240559     Score: 0.564503
                 pubYear	2014
                 paperId	conf/vts/Gattiker14
                 authors	Anne Gattiker
                    kind	inproceedings
                   title	Unstructured text: Test analysis techniques applied to non-test problems.
                   venue	VTS
         Record 3: docID: 87327     Score: 0.547886
                 pubYear	2010
                 paperId	journals/jcse/Jo10
                 authors	Taeho Jo
                    kind	article
                   title	Representation of Texts into String Vectors for Text Categorization.
                   venue	JCSE
        (f) The system can also support proximity searches using the tilde "~" at the end of a phrase, i.e., finding words that are within a specific distance away. 
        (g) Range searches. Range queries allow one to match records whose field(s) values are between the lower and upper bound specified by the Range Query. For example, to search for records whose authors are between A and B, but not including A and B, use the query authors: {A TO B}. 
        (h) Boosting a term. To boost a term use the caret "^" with a boost factor (a number) at the end of the term that are searched. The higher the boost factor, the more relevant the term will be. Boosting allows controling the relevance of a document by boosting its term. If you want a term to be more relevant, it is boosted using the ^ symbol along with the boost factor next to the term. 
Here, give two examples.
    The first one is the query: system model (note this query contain two single term, different with "system model"). The the two terms are equal relevant. The most relevant record is: 
                   docID: 1134718    Score: 1.713261
                 pubYear	1992
                 paperId	journals/csys/Neuman92
                 authors	B. Clifford Neuman
                    kind	article
                   title	The Prospero File System: A Global File System Based on the Virtual System Model.
                   venue	Computing Systems
    The second one is the query: system model^4. Here the records with model are more relevant. Thus, the most relevant record is: 
                   docID: 2520501     Score: 1.493212
                 pubYear	2009
                 paperId	conf/icsoft/SchlechterSF09
                 authors	Antoine Schlechter;Guy Simon;Fernand Feltz
                    kind	inproceedings
                   title	From an Abstract Object-oriented Domain Model to a Meta-model for the Domain - Model Driven Development of a Manufacturing Execution System.
                   venue	ICSOFT (1)
        (i) The system also support Boolean operators, such as AND, OR, NOT, +, and - .
For example, search for records that contain system or model: system OR model;
             search for records that contain system or the phrase "model cheking" is in the titles: system OR "model checking";
             search for records that contain system and model: system AND model;
             search for records that contain system and have the phrase "model cheking" in the title: system AND "model checking";
             search for records whose titles contain sytem and the authors contain Steve Walker: system AND authors: ``Steve Walker'';
             search for records that contain system but no model: system NOT model
             search for records that must contain system but may contain model: +system model;
             search for records that must contain system model but not model checking: "system model" -"model checking"; 
The operators can also be combined. For example, to search for either "system" or "checking" and "model" use the query: (system OR checking) AND model
       (j) Users can use parentheses to group multiple clauses to a single field, for example, to search for a title that contains both the phrase  "system model" and the word "data" use the query: title: (+"system model" +data).
        (k) Some special characters are also supposed by using the "\" before the characters. These characters are +, -, &&, ||, !, (, ), {, }, [, ], ^, ", ~, *, ?, :, \, and /. For example, (1+1)||(2-1) can be described as \(1\+1\)\||\(2\-1\) 
         
For more details of the supporting queries, users can click help to look for the detailed descriptions of the acceptable formats of queries. 
   
(4) The last step is to search for and display the records based on the specified queries.


## Application 1

It is tagged by "App1" at the top of the user interface. This application is to search for the popular topics. 
In this application, users can define the year, venue, and author they want to query. The year duration is from 2000 to 2015. The venue and author are options and thus can be empty. The results or any queries contain two parts: the first one is the topics based on term frequency, and the other is the topics based on topical N-Gram considering each term as a unit. 
Here some examples are given.
    (1) Search for the most popular research topics in Year 2007. For this query, users only need to set the pubYear to be 2007. 
The returned top 10 topics are:
sensor networks (2145)
wireless sensor (1576)
wireless sensor networks (1324)
neural networks (996)
wireless networks (839)
hoc networks (830)
web services (663)
genetic algorithm (555)
performance analysis (553)
support vector (513)

   
(2) Search for the most popular research topics in SIGIR 2012. For this query, the pubYear is set to 2012, and the venue is sigir. The results are shown as follows.
web search (10)
federated search (4)
aggregated search (4)
search engines (4)
social media (4)
search result (3)
text classification (3)
learning rank (3)
search engine (3)
retrieval evaluation (3)


## Application 2

By using App2, users can search for the most similar publication venue and year for a given conference or journal.
In this application, as for the basic search engine, users can also configure the system by setting stemming, character case, and stopwords.
Before input the queries, users should go through the representations of all venues in DBLP and find out the venue that they want to query in DBLP. The help contains the DBLP representations of all publications at different years. 
For example, the item "2014  IEEE Trans. Knowl. Data Eng." from the help means that in DBLP, the journal "IEEE Transactions on Knowledge and Data Engineering" of Year 2014 is written as "IEEE Trans. Knowl. Data Eng.". Thus, if you want to search for the most similar publication venue and year of TKDE 2014, you input "IEEE Trans. Knowl. Data Eng." in the venue textbox, rather than TKDE. The returned results are top N publication venues and their scores. For example, the top 10 similar publication venues of TKDE 2014 are:

IEEE Trans. Knowl. Data Eng., 2015	0.7648006
ICDE, 2011	                        0.7269442
ICDE, 2012	                        0.71243507
IEEE Trans. Knowl. Data Eng., 2013	0.67970675
IEEE Trans. Knowl. Data Eng., 2011	0.66545534
IEEE Trans. Knowl. Data Eng., 2016	0.657765
IEEE Trans. Knowl. Data Eng., 2012	0.6533236
EDBT, 2009	                        0.6407726
DASFAA, 2009	                        0.63892615
DASFAA (1), 2012	                0.61591434
