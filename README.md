# Soen342

- Alvin Biju       : 40278182
- Kevin Liu        : 40281197
- Jovan Gavranovic : 40282175

## How to Run the Application:

Prerequisites:
[Need SQLite installed](https://sqlite.org/download.html)
[Need Maven installed](https://maven.apache.org/install.html)
[Need Java installed](https://www.oracle.com/java/technologies/downloads/)

Once the Github is cloned, go to the Iteration folder and run these commands in order:

Step 1. Install dependencies and build the project
```sh
mvn clean install
```
Step 2. Compile the source code
```sh
mvn clean compile
```
Step 3. Package the project into a JAR
```sh
mvn package
```
Step 4. Run the generated JAR
```sh
java -jar target/Iteration-1.0-SNAPSHOT.jar
```

Once the above commands are executed, a text-based user interface (TUI) will appear directly in your terminal.
From there, you can interact with the system and perform all operations for the Train Connection System.
