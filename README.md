# Go-Back-N-Protocol-for-a-socket-based-network-connection

There are two files (Sender.java and Receiver.java) which will run on two seperate systems. 

First of all, run the Receiver.java file on the command prompt in one system, which represents the Receiver side code.
Type the following on the command prompt:
javac Receiver.java
java Receiver

Get the IP address for receiver by running the following lines in the Java code-
InetAddress ip = InetAddress.getLocalHost();
System.out.println(ip);

The value which will be printed should be written in Sender.java file. 
Socket socket=new Socket("--here--",5000)

Place a text file named 'in.txt' in the same directory as the Sender.java file as it is the input file to be read. 
Next, run the Sender.java file on the second system. (The Receiver should already be running in the first system)
javac Sender.java
java Sender

The outputs for both sides will be displayed. (The number of packets transmitted accordding to Go-Back N protocol)
