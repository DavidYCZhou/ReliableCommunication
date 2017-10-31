import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;

// Lower Infrastructure
public class UdpService {


    public InetAddress targetIP = null;
    public InetAddress hostIP = null;
    public int targetPort = -1;
    public int hostPort = -1;

    private DatagramSocket udp;

    public UdpService() throws SocketException, UnknownHostException {
        hostIP = InetAddress.getLocalHost();
        udp = new DatagramSocket();
        hostPort = udp.getLocalPort();
    }

    public void setTargetPort(int p){
        this.targetPort = p;
    }

    public void setTargetIP(InetAddress addr){
        targetIP = addr;
    }

    public void sendPacket(Packet packet) throws IOException {
        byte[] data = new byte[packet.length];
        ByteBuffer dataBuffer = ByteBuffer.wrap(data);
        dataBuffer.putInt(packet.type);
        dataBuffer.putInt(packet.length);
        dataBuffer.putInt(packet.sequenceNumber);
        dataBuffer.put(packet.data);

        DatagramPacket sendPacket = new DatagramPacket(data, packet.length);
        sendPacket.setAddress(targetIP);
        sendPacket.setPort(targetPort);

        udp.send(sendPacket);
    }

    public Packet receivePacket() throws Exception {
        byte[] data = new byte[512];
        DatagramPacket receivePacket = new DatagramPacket(data, data.length);
        udp.receive(receivePacket);
        if(targetIP == null || targetPort == -1){
            targetIP = receivePacket.getAddress();
            targetPort = receivePacket.getPort();
            System.out.println(targetIP.getHostAddress() + ": " + targetPort);
        }
        // wrap data into Packet
        ByteBuffer dataBuffer = ByteBuffer.wrap(data);
        int type = dataBuffer.getInt();
        int len = dataBuffer.getInt();
        int seq = dataBuffer.getInt();
        byte[] d = new byte[len - 12];
        dataBuffer.get(d, 0 , len - 12);
        return new Packet(type, len, seq, d);
    }

    public static ArrayList<Packet> packBytesIntoPackets(byte[] f) throws Exception {
        ByteBuffer bb = ByteBuffer.wrap(f);
        ArrayList<Packet> result = new ArrayList<Packet>();
        int accumulatedDataLength = 0;
        while(accumulatedDataLength < bb.limit()){
            int len = 500;
            // adjust for last packet
            if(accumulatedDataLength + 500 > bb.limit()){
                len = bb.limit() - accumulatedDataLength;
            }

            byte[] chunk = new byte[len];
            // System.out.println(accumulatedLength + " " + accumulatedDataLength + " " +  len + " " + bb.limit());
            bb.get(chunk, 0, len);
            result.add(new Packet(0, len + 12, 0, chunk));

            accumulatedDataLength += len;
        }
        // add EOT packet
        result.add(new Packet(2, 12, 0, new byte[0]));
        return result;
    }

}
