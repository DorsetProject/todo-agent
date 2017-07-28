# TodoAgent  

The TodoAgent is a Dorset intelligent agent that creates and manipulates a user's to do list. The todoAgent can place the list in either a file or a database, depending on the user's configurations. A user can add to, remove from, and get items from the to do list.  

## Configurations  

See sample.conf for configuration example.  
Configuration file must be named application.conf  

For database functionality, see sample.cfg.xml for additional configuration example.  
Configuration file must be named hibernate.cfg.xml  

## Build
mvn clean package  

## Run
...

## Example Requests  
* ADD item text  
* REMOVE keyword for item  
* REMOVE item number  
* GET ALL  
* GET ALL keyword for items  
* GET keyword  
* GET item number  
* GET ALL date formatted as MM/dd/yyyy  