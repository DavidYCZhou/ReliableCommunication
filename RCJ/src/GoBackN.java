import java.io.ByteArrayOutputStream;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

// This class demonstrate Go Back N Protocol for both sender and receiver
public class GoBackN {

    UdpService udp;
    public static int WINDOW_SIZE = 10;
    public static int TIMEOUT = 100;


    ArrayList<Packet> chunkList;
    // start index of window: this need to be locked
    private static int windowStart;
    private static int windowEnd;
    private static boolean transferFinished = false;
    // ctor
    public GoBackN(UdpService udp){
        this.udp = udp;
    }


    public void sendFile(String filePath) throws Exception {
        this.windowStart = 0;
        udp.setUDPTimeout(30);
        byte[] file = Files.readAllBytes(Paths.get(filePath));
        chunkList = UdpService.packBytesIntoPackets(file);

        while(!transferFinished) {
            // sender sends window sized packets
            long startTime = System.currentTimeMillis();
            windowEnd = windowStart + WINDOW_SIZE - 1;
            if (windowStart + WINDOW_SIZE > chunkList.size()) {
                // dont send too many EOT to channel cuz it has a bug on EOT
                // this bug costs me at least 12 hours :(
                windowEnd = chunkList.size() - 2;
            }

            if(windowStart > windowEnd){
                // send EOT
                udp.sendPacket(chunkList.get(windowEnd + 1));
                printPacketMsg(true, chunkList.get(windowEnd + 1));
            }else{
                // send Data Packet
                for (int i = windowStart; i <= windowEnd; i++) {
                    Packet p = chunkList.get(i);
                    udp.sendPacket(p);
                    printPacketMsg(true, p);
                }
            }


            // receive ack packets
            while(true){
                try {
                    Packet p = udp.receivePacket();
                    printPacketMsg(false, p);

                    if (p.type == 2 && p.sequenceNumber == 0) {
                            transferFinished = true;
                            break;
                    }
                    if(p.type == 1){
                        // update window lowerbound
                        windowStart = p.sequenceNumber + 1;
                    }

                    if (System.currentTimeMillis() - startTime > TIMEOUT ||
                            p.sequenceNumber == chunkList.get(windowEnd).sequenceNumber) break;


                }catch(SocketTimeoutException ste){
                    break;
                }
            }
        }



    }

    // Receiver Side:
    public byte[] receivePacket() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int nextSeq = 0;
        while(true){
            Packet p = udp.receivePacket();
            // print receive msg
            printPacketMsg(false, p);
            if(p.type == 2){
                // printPacketMsg(false, p);
                break;
            }
            Packet ack;
            //System.out.println(nextSeq);
            if(nextSeq == p.sequenceNumber){
                // data to application layer
                //printPacketMsg(false, p);
                baos.write(p.data);
                ack = new Packet(1, 12, nextSeq, new byte[0]);
                nextSeq ++;
            }else{
                ack = new Packet(1, 12, nextSeq - 1, new byte[0]);
            }
            printPacketMsg(true, ack);
            udp.sendPacket(ack);


        }
        // send EOT to sender
        Packet ack = new Packet(2, 12, 0, new byte[0]);
        udp.sendPacket(ack);
        return baos.toByteArray();
    }
    // print message
    public void printPacketMsg(boolean isSend, Packet p){
        String send = isSend? "PKT SEND": "PKT RECV";
        int seq = p.sequenceNumber % 256;
        System.out.println(send + " " + p.getTypeString() + " " + p.length + " " + seq);
        return;
    }


}
