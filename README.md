Java-Client-Server-Architecture
===============================

Free to use and modify.

Overview
--------
In the Server class we have a main class that initiates server settings and begins accepting connections.  In order to accept multiple connections, it must open multiple threads- one thread per connection.  A Subclass is used to handle what occurs during each thread.  One key design decision made was to use ‘implements Runnable’ over ‘extends Thread’ because we are not really overriding any thread behavior.  So in a strict definition, it is advantageous to use ‘implements Runnable.’  On the client class, we have a more basic structure consisting of only one main class that takes in input from the user and communicates this to the output stream to the server.  

How to Run
--------------
This program has been tested full on Windows machines using Eclipse.  To run it, first compile and run Server.java, then once the Server is running in the background, you can compile and run Client.java.  You can run more than one Client at the same time.
