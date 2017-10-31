import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

// This class demonstrate Go Back N Protocol for both sender and receiver
public class GoBackN {

    UdpService udp;
    private final ReentrantLock lock = new ReentrantLock();
    public static int WINDOW_SIZE = 10;


    ArrayList<Packet> chunkList;
    private int windowStart;

    // ctor
    public GoBackN(UdpService udp){
        this.udp = udp;
    }

    public void sendFile(String filePath) throws Exception {
        this.windowStart = 0;
        byte[] file = Files.readAllBytes(Paths.get(filePath));
        chunkList = UdpService.packBytesIntoPackets(file);

        ExecutorService executor = Executors.newSingleThreadExecutor();

        //AckTask ackTask = new AckTask();
        //new Thread(ackTask).start();
        while(windowStart < chunkList.size()){
            Future<String> future = executor.submit(new SendTask());
            try {
                // set timeout running future task
                future.get(2, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                future.cancel(true);
                System.out.println("Timeout!");
            }
        }

        executor.shutdown();


    }

    // Receiver Side:
    public byte[] receivePacket() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while(true){
            Packet p = udp.receivePacket();
            // print receive msg
            printPacketMsg(false, p);

            // send ack packet
            Packet ack = new Packet(1, 12, p.sequenceNumber, new byte[0]);
            udp.sendPacket(ack);

            baos.write(p.data);
            if(p.type != 2) break;

        }
        // send EOT to sender
        Packet ack = new Packet(2, 12, 0, new byte[0]);
        udp.sendPacket(ack);
        return baos.toByteArray();
    }

    public void printPacketMsg(boolean isSend, Packet p){
        String send = isSend? "PKT SEND": "PKT RECV";
        System.out.println(send + " " + p.getTypeString() + " " + p.length + " " + p.sequenceNumber);
        return;
    }
    // Sender Side:
    private class SendTask implements Callable<String>{

        @Override
        public String call() throws Exception {
            // sender should not update windowStart
            lock.lock();
            int start = windowStart;
            lock.unlock();
            int end = start + WINDOW_SIZE - 1;
            if(start + GoBackN.WINDOW_SIZE > chunkList.size()){
                end = chunkList.size() - 1;
            }
            for(int i = start; i <= end; i ++){
                Packet p = chunkList.get(i);
                p.sequenceNumber = i - start;
                printPacketMsg(true, p);
                udp.sendPacket(p);
            }
            // if no error
            lock.lock();
            windowStart = end + 1;
            lock.unlock();
            return "Done";
        }
    }

    private class AckTask implements Runnable {

        @Override
        public void run() {
            while(true){
                try {
                    while(true){
                        Packet p = udp.receivePacket();
                        if(p.type == 2) break;
                        // TODO: implement logic for ack packet
                        printPacketMsg(false, p);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }




}
