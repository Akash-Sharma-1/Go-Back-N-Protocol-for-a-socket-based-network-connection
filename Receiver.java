import java.io.BufferedReader;
import java.io.File;
import java.io.*;
import java.io.FileReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.Serializable;
import java.util.Arrays;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class Receiver {
	
	public static double probability = 0.4;
	public static void main(String[] args) throws Exception{
		InetAddress ip = InetAddress.getLocalHost();
		System.out.print(ip);
		DatagramSocket recieverSocket = new DatagramSocket(9876);
		// 83 is the base size (in bytes) of a serialized Packet object 
		byte[] receivedDataFromSender=new byte[83+Sender.packetSize];
		int expectedSeq=0;
		ArrayList<Packet> buffer=new ArrayList<Packet>();
		boolean end=false;
		
		while(end==false)
		{

			System.out.println("Waiting for packet");
			// Receive packet from socket
			DatagramPacket receivedPacket=new DatagramPacket(receivedDataFromSender, receivedDataFromSender.length);
			recieverSocket.receive(receivedPacket);
			Packet packet=(Packet) Serializer.toObject(receivedPacket.getData());
			// Unserialize to a Packet object

			System.out.println("Packet with seq no. " + packet.getSeq() + " recieved "+System.nanoTime());
			if(packet.getSeq()==expectedSeq && packet.isLast()){
				expectedSeq++; // hatana hai - test karna hai 
				buffer.add(packet);
				end = true;
				System.out.println("Transmission Completed");
			}
			else if(packet.getSeq()==expectedSeq){
				System.out.println("Packed stored in buffer");
				buffer.add(packet);
				expectedSeq++;
			}else{
				System.out.println("Packet rejected (not in order)");
				// rejects bluntly 
			}
			
			// Create an Acknowledgement object
			Ack ackObject=new Ack(expectedSeq);
			byte[] ackBytes=Serializer.toBytes(ackObject);
			DatagramPacket ack=new DatagramPacket(ackBytes, ackBytes.length, receivedPacket.getAddress(), receivedPacket.getPort());
			// checking if we need to drop the packet or not
			if(Math.random() > probability || packet.isLast()){

				System.out.println("Sending ACK to seq " + (expectedSeq-1) + " with " + ackBytes.length  + " bytes");
				recieverSocket.send(ack);
			}else{
				System.out.println("[xxxx] Ack with seq no. " + ackObject.getSeq() + " lost ");
			}
		}
		// Printing the recieved data buffer
		System.out.println(" ---Data Received--- ");
		for(int i=0;i<buffer.size();i++){
			for(byte b: buffer.get(i).getData()){
				System.out.print((char) b);
			}
		}		
	}
	
}
