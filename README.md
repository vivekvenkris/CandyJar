# CandyJar V3.1
Candidate Viewer for TRAPUM and MPIFR pulsar surveys with the MeerKAT telescope

For now, releases will not be given as `jar` files. Please build from source using `gradle`. The application needs `Java 14` or above to run. Please start with installing JAVA and then you can build and run the application. 

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
You can check `./gradlew run --args="-h"` for a list of available arguments. It should display something like this:

```
*************************Candy Jar V2.1-alpha*******************************
usage: CandyJar
 -d,--add_psrcat_db <arg>       Add a psrcat database to get known pulsars
                                from. Currently only takes pulsars with
                                positions in RA/DEC and in correct hms/dms
                                format
 -e,--extend_png                Scale png beyond actual size. This is only
                                ever useful for large resolution monitors
                                where you want to resize the PNG to a
                                higher resolution than original.
 -h,--help                      show this help message
 -l,--list_screens              List available screens
 -n,--num_charts <arg>          Number of charts needed on the secondary
                                screen (Min:0, max:3)
 -s1,--primary_screen <arg>     Choose primary screen to open the
                                application in. The application opens full
                                screen by default. You can provide an
                                optional custom resolution if you like, of
                                the format: <screen—num>:widthxheight. Eg:
                                1:1920x1080 will open the application
                                onyour first screen, with the resolution
                                of 1920x1080
 -s2,--secondary_screen <arg>   Choose secondary screen to open the
                                application in. You can provide custom
                                resolution like for screen 1
```
 

Once you run the program, you will find a text field where you can add a candidate directory. You can also use the directory selector button (with the three dots) to navigate and select the directory. This is the directory that contains the `candidates.csv` file. Once done, click on `Get Pointings` to load all the UTCs. 

At this point, if you already have the output of a partial classification, you can add it using "load classification". Otherwise, ignore that button, and start selecting the UTC of your choice. 

Once you select the UTC, the corresponding beam map will be drawn below along with information on neighbouring pulsars (if any). Now you can filter and sort candidates using the corresponding dropdown menus and click `Go` to start viewing the candidates. Once you hit `Go`, if you have asked for a plotting interface, the corresponding window will appear where you can select the parameters of your choice to plot. On the main window, you can use the buttons for navigation and classification or use the corresponding keys (given in paranthesis). For smaller screens, you can also press the spacebar to open the current PNG file using your default PNG viewer in full resolution. If you choose the CandyCharts to open on the secondary window, hovering over the plot title will bring up the tool bar which will allow you to zoom, pan, and mark candidates. 

Once the classification is done, you can press the "Save classification" button to write out your classification to a CSV file. The application also automatically saves your classification every two minutes in the root candidate directory. In the event where the application crashes, you can resume from where you left off by loading this classification back when the application is restarted.  This file will need to me manually removed after the classification is over. 






