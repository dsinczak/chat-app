You're required to write a backend + REST APIs for a simple chat application. The application will allow users to create 1-1 chat with other users.

    - any user can start chat with any other user
    - there is no approval process, if Alice starts a chat with Bob, 1-1 chat thread between Alice and Bob is automatically created without Bob doing anything
    - there can be maximum 1 chat thread between same users
    - list of chat threads should display small snippet of the last message
    - for simplicity, we'll assume that message can't be edited or deleted once it's posted.
    - for the simplicity, users cannot leave chat threads once they are created
    - for the simplicity, we'll assume text-only messages, no files, no images, no text formatting

 Required APIs:

    - get list of chat threads for user (those he participates in)
    - start a new chat
    - get list of chat messages for given chat thread
    - send a message to chat thread

Constraints
    - let's assume User to be defined as Name and ID (no avatar or profile)
    - let's assume highly simplified authentication where user is identified by his ID
    - choose whatever stack you want and whatever persistance you want, the only requirement is that you use Scala and Akka (in any way you see fit)
    - application should be easy to run on localhost via `sbt run` or similar. If there are some external requirements that need to be installed
      prior to running it (eg. some database, etc), please add a ReadMe file with a necessary description
    - deliver your solution on any git-based system where we can access it (GitHub, GitLab, etc.). Might even be private, as long as we get read permissions to it
    - preferably, task should be done as a normal project, with each step being identified as a commit instead of a single commit for the whole thing


Above are minimum requirements. You're welcome to come up with your own improvements if you wish so, but only as long as complexity is the same or higher than above.

Sample usage:

    - Run application (no parameters required, by default it bind to localhost:8080)
    - Log-in 2 users:
        - curl -v -X POST --header "Content-Type: application/json" -d '{"userId":"moonMan","name":"E. Musk"}' http://localhost:8080/chat/session
        - curl -v -X POST --header "Content-Type: application/json" -d '{"userId":"awsMan","name":"J. Bezos"}' http://localhost:8080/chat/session
    
    - Get users:
        - curl -v -X GET http://localhost:8080/chat/session 
    
    - Send few messages:
        - curl -v -X POST --header "Content-Type: text/html" -d 'Why AWS is so expensive?' http://localhost:8080/chat/moonMan/thread/awsMan
        - curl -v -X POST --header "Content-Type: text/html" -d 'Btw. would you like to get to the moon?' http://localhost:8080/chat/moonMan/thread/awsMan
        - curl -v -X POST --header "Content-Type: text/html" -d 'If you dont like the pricing then dors are open' http://localhost:8080/chat/awsMan/thread/moonMan
        - curl -v -X POST --header "Content-Type: text/html" -d 'And first try to send a dog' http://localhost:8080/chat/awsMan/thread/moonMan
    
    - Get users threads:
        - curl -v -X GET  http://localhost:8080/chat/awsMan/thread 
        - curl -v -X GET  http://localhost:8080/chat/moonMan/thread 
    
    - Get thread messages:
        - curl -v -X GET  http://localhost:8080/chat/awsMan/thread/moonMan
        - curl -v -X GET  http://localhost:8080/chat/moonMan/thread/awsMan