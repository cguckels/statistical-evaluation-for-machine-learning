# Setup #
STATS-ML uses R to perform statistical testing. R and the R bridge dynamic libraries are not included in the package and have to be installed separately for your operating system. Please make sure that all components are built for the same architecture (x64 or x86).

# Details (Windows) #
_Tested on Windows 8.1_
  1. Download R ([ttp://www.r-project.org/] and install it.
  1. Open the right binary (architecture wise), e.g. for x64: C:\Program Files\R\R-.1.0\bin\x64\R.exe (use admin rights).
  1. Use install.packages("rJava") to download rJava, remember the given folder path.
  1. Navigate to that path and extract the zip, preferably somewhere together with the R installation.
  1. Specify a PATH variable in the run congurations as an environmental variable (of your main or testing classes) and add the paths to the native libraries (jri.dll, R.dll). E.g., the following files should be referenced: C:\Program Files\R\R-3.1.0\rJava\jri\x64;C:\Program Files\R\R-3.1.0\bin\x64
  1. Add jvm.dll to the PATH variable if it not already there. E.g., "C:\Program Files\Java\jdk1.7.0.51\jre\bin\server" (jvm.dll is sometimes not in \server but in \client)
  1. Add a reference to the JRI libraries as additional VM arguments. E.g., "-Djava.library.path=C:\Users\TestUser\Documents\R\win-library\3.1\rJava\jri\x64"
  1. The program may ask you to install additional R packages during the first run. Just tell it you want a user library and it will install the needed packages into the project folder.

# Details (Linux) #
_Tested on Ubuntu 14.04 Trusty Tahr_
  1. Install R. Via apt: "sudo apt-get update; sudo apt-get install r-base"
  1. Install JRI library in R. Via command line: "r; install.packages('rJava')"
  1. Determine library folder. In R, type ".libPaths()"
  1. Add a reference to the JRI libraries as additional VM arguments. E.g., "-java.library.path=/Library/Frameworks/R.framework/Versions/3.1/Resources/library/rJava/jri/"
  1. Specify a R\_HOME as an environmental variable (in eclipse, use the "run configurations tab of your main or testing classes") and add the paths to the native libraries of R. E.g., "R\_HOME = /Library/Frameworks/R.framework/Versions/3.1/Resources/"
  1. The program may ask you to install additional R packages during the first run. Just tell it you want a user library and it will install the needed packages into the project folder.

# Details (MacOS) #
_Tested on OSX 10.10 Yosemite_
  1. Install R. Via Homebrew:  "brew tap homebrew/science; brew install gcc; brew install r;"
  1. Install JRI library in R. Via command line: "r; install.packages('rJava')"
  1. Determine library folder. In R, type ".libPaths()"
  1. Add a reference to the JRI libraries as additional VM arguments. E.g., "-java.library.path=/Library/Frameworks/R.framework/Versions/3.1/Resources/library/rJava/jri/"
  1. Specify a R\_HOME as an environmental variable (in eclipse, use the "run configurations tab of your main or testing classes") and add the paths to the native libraries of R. E.g., "R\_HOME = /Library/Frameworks/R.framework/Versions/3.1/Resources/"
  1. The program may ask you to install additional R packages during the first run. Just tell it you want a user library and it will install the needed packages into the project folder.