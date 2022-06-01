# Performance gain using HTTP Connection Pool

In a microservices architecture, the number of server to server connections increases dramatically because the interactions which would traditionally have been an in-memory process in one application now often depends on remote calls to other REST based services over HTTP, meaning it is more important than ever to ensure these remote calls are both fast and efficient.  Let’s look at the lifecycle of a HTTP connection to understand the areas of improvement and possible solution.

## The HTTP Connection Lifecycle
Usually when talking about a HTTP call, only the orange section (in fig. below) is thought about. A client sends a request to an endpoint, describing what data is being requested, the server processes this request and sends back the data in a response. However it’s clear to see a lot more is happening, most importantly each time the arrows change directions latency will be measurable.

![The HTTP Connection Lifecycle](https://raw.githubusercontent.com/ViveKapoor/http-connection-pooling/main/HTTP-Connection-Lifecycle.png)

### DNS Resolution
When calling another service, it usually has some kind of hostname (e.g. [reqres.in](https://reqres.in/)).  In order to call this service, this hostname needs to be resolved into an IP address.

DNS resolution typically happens over the UDP transport, which is a connectionless protocol unlike TCP, which means there is no handshake necessary between the client and the DNS server and instead the request can just be sent and then wait for the response.

### TCP Connect
For data to flow between a client and server, the TCP handshake needs to occur.  This handshake is used to initialise values which are later used to ensure reliability in the data exchange.  This enables checks to take place which ensure that data is being received intact and without errors, and allows for retransmissions to occur if there are any problems.  Once this handshake is completed the connection is said to be in an “established” state.

### TLS Handshake
If you are securing your connections with TLS, then a TLS handshake occurs next.  This handshake is much more heavy weight than the TCP handshake.  Fairly large pieces of data need to be transferred between client and server such as the certificate being used by the server, and in Mutual TLS (client certificate authentication) the client certificate also needs to be sent to the server.  Cryptographic functions need to be executed at each time which can be CPU intensive and also block if there is a lack of “entropy” available.

Once the TLS handshake is completed, the actual HTTP request can now be sent.

### HTTP Request/Response
This is the part that we are actually bothered about most of the time.  The client sends a request (containing elements such as the HTTP method and path, along with any request headers and a body if required).  The server receives this requests, processes it however it needs to, and sends the response (containing a status code, headers and body).  The client can then parse this response to obtain the data requested.

### Close
As we started this conversation with a handshake, we now need to say goodbye to the server and close the connection.  Again this is a 3 way handshake similar to the start of the conversation.

## Connection Pooling
Connection Pooling is a feature available in a number of HTTP Client libraries, so how does it work?

Conceptually, this is actually quite simple.  Taking an example of 2 requests made back to back, the first request will proceed as normal through the lifecycle shown above, until it gets to close the connection.  When connection pooling is in use, instead of closing the connection it will instead be put to one side for use later.  When a second request then needs to be made to the same host it can skip DNS resolution, TCP connect and TLS handshake, and just reuse the connection we put to one side earlier.

### Performace gain with connection pool
Below results show the stability and performace gain with coonection pool.

![Performance comparison](https://raw.githubusercontent.com/ViveKapoor/http-connection-pooling/main/13x-performance-gain.png)

As we can see, the performance get's improved by ~13 times if we use a connection pool vs without a connection pool.

![Response time graph](https://raw.githubusercontent.com/ViveKapoor/http-connection-pooling/main/response-time-graph.png)

The above graph clearly shows how the response time becomes stable with a connection pool.
The spikes indicate that without pool the response times are quite unstable because for each every new request made to the server, client has has to establish a new connection and time to make new connection is not constant as it depends on the network as well as server load.

## Note
Please note that the performace gain with HTTP connection pooling is inversely proportional to the HTTP request/response (processing) time, i.e. it's not guaranteed that we will always get a constant performance gain of 13x, i.e,

<div align="center">
  performance gain ∝ 1/(𝑠𝑒𝑟𝑣𝑒𝑟 𝑝𝑟𝑜𝑐𝑒𝑠𝑠𝑖𝑛𝑔 𝑡𝑖𝑚𝑒)
</div>

## Steps to run POC code
Please check steps [here](https://github.com/ViveKapoor/http-connection-pooling/blob/main/httpConnectionPool/HELP.md)
