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
            File dump = new File("dump.jpg");
            dump.delete();
            dump = new File("dump.jpg");
            FileOutputStream fos = new FileOutputStream(dump);
            fos.write(f);
            fos.close();

        } catch (SocketException e) {
            System.out.println("Socket Error!");
        } catch (UnknownHostException e) {
            System.out.println("Host name is invalid!");
        }catch(IOException e){
            System.out.println("File IO Error!");
        } catch (Exception e) {
            System.out.println("Socket Error!");
        }
    }
}
