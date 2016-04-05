# Setup
- Requirement:
  1. Oracle Java 8 is required and should be pre-installed; if you have multiple JREs, please ensure `JAVA_HOME` is correctly pointed to the Java 8 directory
  1. `LSearcher` is based on [Scala](http://www.scala-lang.org/) and [Play!](https://www.playframework.com/), you can use [activator](https://www.lightbend.com/activator/download) (embedded in our project) to get them if `bash` is available
- Deployment

```shell
git clone https://github.com/HongxuChen/ci6226.git
cd ci6226/
# download dblp.xml file to "public/resources"
# change "rootDir" value in app/models/common/Config.scala to point to that absolute path of "public/resources"
./activator # in Linux/Mac OS X
# On Windows, you download activator and run executable `activator` in project directory
run -Dhttp.port=9001 # (inside activator shell)
open http://localhost:9001 in your favorite web browser
```

- Try at http://155.69.145.146:9001
