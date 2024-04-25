# Connect ACT-R to a Pepper robot

This is a proof-of-concept for the integration of a cognitive architecture (ACT-R) with the humanoid social robot Pepper preparing a technical basis for a more human-like perception of human interaction partners.
The use of cognitive architectures is promising in order to achieve more human-like reactions and behavior in social robots. For example, ACT-R can be used to create a dynamic cognitive person model of a human cooperation partner of the robot. 
This code contains a sample application programmed in Kotlin for the Android version of the Pepper robot and an example ACT-R model *pepper-mood.lsp* in the act-r folder.
The ACT-R model is updated with dynamic data from emotion recognition by the robot.

## System setup for ACT-R and the robot

The standalone version of ACT-R is intended for use here, i.e. the application provided at https://act-r.psy.cmu.edu/. To establish a remote connection from the robot to ACT-R, the remote interface – the *dispatcher* – has to be used, which is implemented by a central command server. The ACT-R core software connects to this dispatcher to provide access to its commands, and the dispatcher accepts TCP/IP socket connections that allow clients to access these commands and provide their own commands for use. 

By default, the standalone version of ACT-R forces the dispatcher to use the localhost IP address of the computer on which it is running for connections. This means that only programs on the same computer can establish a connection, and once ACT-R has been started, this can no longer be changed. To disable this function, the file *force-local.lisp* must be removed from the ACT- R/patches directory before ACT-R is executed. Then it will use the machine’s real IP address for the dispatcher’s connections and setting *allow-external-connections* in the model file will let other machines connect. Another option is to place the model file in the ACT-R/user-loads directory. External connections are then always permitted. The address and port used by the dispatcher is displayed at the top of the ACT-R terminal window. This information must be used on the remote computer or, in our case, in the Pepper application for the connection.

The Pepper application is able to interact directly with the ACT-R model by calling commands. The *run-full-time* command, for example, together with a number of seconds, starts and runs the model for the specified time. The *evaluate* method is used to evaluate commands from the dispatcher. It requires the name of the command to evaluate.

![pepper-act-r](https://raw.githubusercontent.com/holyhat/pepper-robot-to-act-r/master/pepper-act-r.png)
