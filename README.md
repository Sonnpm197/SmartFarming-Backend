# SmartFarming-Backend (RestfulAPI)
Spring Boot project associated with Google Cloud storage, Hibernate Search (Lucene) and MSSQL Server

A platform based on StackOverFlow in which users can post their own questions, answers and comments.

In users mode, users can upvote (hit like) a question which they may find useful/unrelated. For instance, if I create a question and then another upvotes (likes) it, I'll get one point for each tag I defined in that question. Eventually, my question also increases 1 view count (I use a separate thread for counting a view based on ipaddress).

In administrator mode, we can view users stats based on their points of their tags. Hence, admins can see which ones have the highest point in relation to which department they are good at. To be more precise, if a user post a lot of questions as regards "tomatos" and they get tons of likes in such questions (maybe others find them useful), admins can see that profile as the one who is good at "tomatos". Additionally, admins can see which tags are trendy and then they can post more articles in relation to these tags. On top of that, we use a graph lib of Angular to see the person who is the most active user of our website, such information is actually helpful when recruiters need to see if this candidates (users of this website) have a lot of knowledge about some agricultural aspects.

For login we use GMail authentication on Angular. 

1. Maven 3.3.9 & Java 8 installed
2. IDE: Webstorm & IntelliJ 2018.3
3. Database: MSSQL Server
4. How to deploy:
5. Checkout SmartFarming-Common at https://github.com/Sonnpm197/SmartFarming-Common.git (Here I specify all the entities) and run mvn clean install to add to local maven repository
6. Checkout this project and change some properties in application.properties to fit your database
7. Checkout SmartFamring (UI - Angular) at https://github.com/Sonnpm197/SmartFarming.git and run ng s.

* Update 1/2/2020:
- I have deactivated my gg cloud storage due to the expiration of my account so if you want to try out this project you may need to create your own storage. More information can be found at https://cloud.google.com/storage
- On UI, the CKEditor lib is expired on Angular so you should create a new one. More information can be found at https://dashboard.ckeditor.com/
