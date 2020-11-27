# CandyJar V1.0-alpha
Candidate Viewer for TRAPUM and MPIFR pulsar surveys with the MeerKAT telescope

For now, alpha and beta releases will not be given as `jar` files. Please build from source using `gradle`. 

## Dependencies that need to be natively installed for building

1. Java JDK 14 or above
2. JavaFX 15
3. gradle 6.3 or above 


## Installing JAVA

### Ubuntu / Debian
```shell
sudo add-apt-repository ppa:linuxuprising/java
sudo apt update
sudo apt install oracle-java15-installer
```

You will need to accept the oracle license when installing. 

if `add-apt-repository` is not available by default, you can add it by adding

```shell
sudo apt-get install software-properties-common
```

If you have set up `apt` to not install recommended packages, please also install this:

```shell
sudo apt install oracle-java15-set-default
```

or by choose java 15 using `update alternatives`:

```shell
update-alternatives --config java
```

Once done, please check your default java version is 15.X. Running the following commands should show something similar. 

```shell
 java -version
 java version "15" 2020-09-15
 Java(TM) SE Runtime Environment (build 15+36-1562)
 Java HotSpot(TM) 64-Bit Server VM (build 15+36-1562, mixed mode, sharing)
 
 javac -version
 javac 15
```
### MAC OSX

#### From `.dmg` file

Java JDK 15 is available as `.dmg` files for MAC OSX. Download the `.dmg` file from here: https://www.oracle.com/java/technologies/javase-jdk15-downloads.html 
and install it in the usual way. 

#### From source

Please download the `.tar.gz` file from https://www.oracle.com/java/technologies/javase-jdk15-downloads.html appropriate for Mac OSX

Run the following:
```shell
sudo mv openjdk-15_osx-x64_bin.tar.gz /Library/Java/JavaVirtualMachines/
cd /Library/Java/JavaVirtualMachines/
sudo tar -xzf openjdk-15_osx-x64_bin.tar.gz
sudo rm openjdk-15_osx-x64_bin.tar.gz
```
Make sure `java_home` can see it:

`/usr/libexec/java_home -v15`

Add `JAVA_HOME` environment variable to the `.rc` file corresponding to your shell. For example, 

`echo -n "\nexport JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-15.jdk/Contents/Home" >> ~/.bash_profile`


Once, done, please either source your rc file again, or open a new terminal and check your default java version is 15.X. Running the following commands should show something similar. 

```shell
 java -version
 java version "15" 2020-09-15
 Java(TM) SE Runtime Environment (build 15+36-1562)
 Java HotSpot(TM) 64-Bit Server VM (build 15+36-1562, mixed mode, sharing)
 
 javac -version
 javac 15
```

 
## Using the viewer 

Run the following to build:

```shell
git clone https://github.com/vivekvenkris/CandyJar.git
cd CandyJar
./gradlew build
```

and to run the application, please do `./gradlew run`

This should open the application on your primary screen. You can provide commandline arguments like the following `./gradlew run --args="your arguments"`
You can check `./gradlew run --args="-h"` for a list of available arguments. 



