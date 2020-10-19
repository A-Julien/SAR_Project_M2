#  SAR_Project


[![Build Status](https://travis-ci.com/A-Julien/SAR_Project_M2.svg?branch=master)](https://travis-ci.com/A-Julien/SAR_Project_M2) 
[![codeinsp](https://www.code-inspector.com/project/13873/score/svg)](https://frontend.code-inspector.com/public/project/13873/SAR_Project_M2/dashboard)
[![codebeat badge](https://codebeat.co/badges/714feef9-ef4e-45d9-b2e9-e6319bb2b32a)](https://codebeat.co/projects/github-com-a-julien-sar_project_m2-master)

## Project architecture:

*	Github : https://github.com/A-Julien/SAR_Project_M2
*	CI (Travis) : https://travis-ci.com/A-Julien/SAR_Project_M2

## Documentation
Project are build with java 11
La documentation des fonctions est disponnible dans la [javadoc](https://a-julien.github.io/SAR_Project_M2/apidocs/index.html)

## Installation
* Download and include the jar to your project [here](https://github.com/A-Julien/SAR_Project_M2/releases/tag/SNAPSHOT) 
* Clone repo and run maven : ```mvn compile```
## Usage
### Declare Read/Write methods by add annotations in an Interface

```java
        @JvnAnnotation(type="_W")
        public void write(...);
    
        @JvnAnnotation(type ="_R")
        public ... read();
```
### Share your object

```java
myObj obj = (myObj) JvnProxy.newInstance(new myObj(), "<NAMETOSHARE>"); 
```

### Use your object

```java
obj.read(); 
```

```java
obj.write(...); 
```
