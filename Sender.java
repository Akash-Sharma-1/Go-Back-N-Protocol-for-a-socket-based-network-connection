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

import java.util.Timer;
import java.util.TimerTask;
class Serializer {
	public static byte[] toBytes(Object obj) throws IOException {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		ObjectOutputStream o = new ObjectOutputStream(b);
		o.writeObject(obj);
		return b.toByteArray();
	}
	public static Object toObject(byte[] bytes) throws IOException, ClassNotFoundException {
		ByteArrayInputStream b = new ByteArrayInputStream(bytes);
		ObjectInputStream o = new ObjectInputStream(b);
		return o.readObject();
	}
}
class Ack implements Serializable{
	
	int seq;
	public Ack(int seq) {
		super();
		this.seq = seq;
	}
	public int getSeq() {return seq;}
}
class Packet implements Serializable {
	int seq;
	byte[] data;
	boolean last;

	public Packet(int seqe, byte[] datae, boolean laste) {
		super();
		this.last = laste;
		this.data = datae;
		this.seq = seqe;
	}
	public int getSeq() {return seq;}
	public byte[] getData() {return data;}
	public boolean isLast() {return last;}
	@Override
	public String toString() {
		return "UDPPacket [seq=" + seq + ", data=" + Arrays.toString(data)
				+ ", last=" + last + "]";
	}
	
}

public class Sender {
	// defining timeout time interval
	public static int timeoutTimer=30;
	//defining packet size 
	public static int packetSize=11;
	//defining dropout probability 
	public static double probability=0.4;
	//transmit window Size
	public static int transmitWindow=7;

	public static void main(String[] args) throws Exception{
		// Sender's Socket
		DatagramSocket senderSocket=new DatagramSocket();
		InetAddress recieverIP=InetAddress.getByName("localhost");
		ArrayList<Packet> buffer=new ArrayList<Packet>();
		int latestAcked=0;
		int latestSent=0;
		
		//file reader
		FileInputStream fileInputStream=new FileInputStream("in.txt");
        byte[] bytesFile=new byte[(int) fileInputStream.getChannel().size()];
        fileInputStream.read(bytesFile,0,bytesFile.length); 
		System.out.println("File size: " + bytesFile.length + " bytes");
		int lastSeqNo=(int) Math.ceil( (double) bytesFile.length / packetSize);
		System.out.println("Number of packets to send: " + (lastSeqNo));
		int frameNumbers[]=new int[lastSeqNo];

		while(!false){
			while(latestSent-latestAcked<transmitWindow && latestSent<lastSeqNo){

				byte[] packetBytes=new byte[packetSize];
				// making a new packet from file (in bytes)
				packetBytes=Arrays.copyOfRange(bytesFile, latestSent*packetSize, latestSent*packetSize + packetSize);
				Packet selfMadePacket=new Packet(latestSent, packetBytes, (latestSent == lastSeqNo-1) ? true : false);
				byte[] packetInBytes=Serializer.toBytes(selfMadePacket);
				// Add packet to buffer
				buffer.add(selfMadePacket);
				DatagramPacket packet=new DatagramPacket(packetInBytes, packetInBytes.length,recieverIP, 9876);

				// checking if we need to drop the packet or not
				if(Math.random()>probability){
					// final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
					//     executorService.scheduleAtFixedRate(new Runnable() {
					//         @Override
					//         public void run() {
					//             senderSocket.send(packet);
					// 			System.out.println("Sending packet with seq no. " + latestSent +  " and size " + packetInBytes.length + " bytes at time: "+System.nanoTime());
					// 			frameNumbers[latestSent]++;
					//         }
					//     }, 0, 1, TimeUnit.SECONDS);
								senderSocket.send(packet);
								System.out.println("Sending packet with seq no. " + latestSent +  " and size " + packetInBytes.length + " bytes at time: "+System.nanoTime());
								frameNumbers[latestSent]++;
								Thread.sleep(2000);
					       
				}else{
					System.out.println("[xxxx] Packet with seq no. " + latestSent+ " lost");
				}
				// Increase the latest sent packet
				latestSent++;
			}
			
			byte[] ackBytes = new byte[40];
			DatagramPacket ack = new DatagramPacket(ackBytes, ackBytes.length);
			try{
				senderSocket.receive(ack);
				senderSocket.setSoTimeout(timeoutTimer);				
				Ack selfMadeAck = (Ack) Serializer.toObject(ack.getData());
				System.out.println(" ACK  " + selfMadeAck.getSeq()+ " Received at time: "+System.nanoTime()); // timestamp addd here
				if(selfMadeAck.getSeq()==lastSeqNo-1){
					break;
				}
				latestAcked = Math.max(latestAcked, selfMadeAck.getSeq());
				
			}catch(SocketTimeoutException e){
				// retransimission since we didn't recieve any ack				
				for(int i=latestAcked; i<(latestSent);i++){
					byte[] packetInBytes = Serializer.toBytes(buffer.get(i));
					DatagramPacket packet = new DatagramPacket(packetInBytes, packetInBytes.length, recieverIP, 9876 );
					// checking if we need to drop the packet or not
					if(Math.random()>probability){
						senderSocket.send(packet);
						System.out.println("RESENDING packet with seq no. " + buffer.get(i).getSeq() +  " and size " + packetInBytes.length + " bytes in time:"+System.nanoTime());
						frameNumbers[buffer.get(i).getSeq()]++;

					}else{
						System.out.println("[xxxx] Packet with seq no. " + buffer.get(i).getSeq() + " lost");
					}
				}
			}
		}
		
		System.out.println("Transmission Completed");
		double avg=0;
		for (int i=0;i<lastSeqNo;i++)
		{
			avg+=frameNumbers[i];
		}
		avg/=lastSeqNo;
		System.out.println("Average number of times a frame was sent : "+ avg);

	}

}
