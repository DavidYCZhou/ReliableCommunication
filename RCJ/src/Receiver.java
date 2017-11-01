import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Receiver {

    public static void main(String argv[])
    {
        try {
            String protocolSelector = argv[0];
            String timeout = argv[1];
            String outputFile = argv[2];

            UdpService udp = new UdpService();
            GoBackN gbn = new GoBackN(udp);
            // create a "recvInfo" file
            File ri = new File("recvInfo");
            ri.delete();
            ri = new File("recvInfo");

            FileWriter fw = new FileWriter(ri, false);
            fw.write(udp.hostIP.getHostAddress() + " " + udp.hostPort);
            fw.close();

            byte[] f = gbn.receivePacket();
            File dump = new File(outputFile);
            dump.delete();
            dump = new File(outputFile);
            FileOutputStream fos = new FileOutputStream(dump);
            fos.write(f);
            fos.close();

        } catch (SocketException e) {
            System.out.println("Socket Error!");
        } catch (UnknownHostException e) {
            System.out.println("Host name is invalid!");
        }catch(IOException e){
            System.out.println("IO Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Socket Error!");
        }
    }
}
