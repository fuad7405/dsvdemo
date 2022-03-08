# DSV Demo

## How to build and run test script ?

1. Clone this repository `git clone https://github.com/fuad7405/dsvdemo.git`
2. ```bash 
   $  cd dsvdemo
   ``` 
3. ```bash 
   $  mvn clean package
   ``` 

## How to run from terminal ?

1. ```bash 
   $  mvn clean package
   ``` 
2. java -jar appname-jar-with-dependencies.jar InputFileName FileDelimiter

    ```bash 
   $  java -jar .\target\dsvdemo-jar-with-dependencies.jar "DSV input 1.txt" ","     
    ``` 