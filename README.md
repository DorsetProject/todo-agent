# TodoAgent  

The TodoAgent is a Dorset intelligent agent that creates and manipulates a user's to do list. The todoAgent can place the todo list in either a file or a database. A user can add to, remove remove, and get items from the to do list.  

## Configurations  

See sample.conf for an example of configurations.  
Configuration file must be named application.conf  

## Build
mvn clean package

## Run
...

## Example Requests  
* ADD insert item text  
* REMOVE insert keyword for item  
* REMOVE insert item number  
* GET ALL  
* GET ALL insert keyword for items  
* GET insert keyword  
* GET insert item number  
* GET ALL insert date formatted as MM/dd/yyyy