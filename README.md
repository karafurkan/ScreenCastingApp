# Screen Casting Application

This application is an example to demonstrate how UDP works in Java. 


The application uses the centralized server model. The server broadcasts its screen output using UDP in the 
local network. The server side captures screenshots in every 20 ms, then sends them to the clients.


In order to avoid overwhelming the network, the server side compress the screenshots into Jpeg format.


Clients send a 'Hello' message to the server. Then, they wait for a response. If the response timeouts, the client 
will send the 'Hello' message again. After sending it 100 times, the client will assume that the server is down.


When a client is connected to the server, the server can kick the client whenever the user wants.


Once the clients receive a screenshot, they display it on the screen. They can record the session whenever they want
as well as they can take a screenshot. Those will be saved in the Desktop folder.


For the sake of reliability, client-side checks every single packet that it received to determine 
whether the checksum, the chunk number, the size of the packet and the total size value are correct. 
If there is a mismatch, the client-side drops the packet, since asking the server to send the packet again would take
considerable amount of time and it'd be kind of against the nature of broadcasting :)


When the clients want to leave from the session, they send a 'FIN' message to the server, so that the Server would know
which clients are still active.

