import java.io.*;
import java.net.*;

public class Sender {


    public static void main(String argv[])
    {
        String protocolSelector = argv[0];
        String inputFile = argv[1];
        // System.out.println(argv.length);
        try {

            // setup UDP
            UdpService udp = new UdpService();

            // read "channelInfo"
            File ci = new File("channelInfo");
            BufferedReader br = new BufferedReader(new FileReader(ci));
            String[] firstLine = br.readLine().split(" ");

            udp.setTargetIP(InetAddress.getByName(firstLine[0]));
            udp.setTargetPort(Integer.parseInt(firstLine[1]));

            // Go Back N protocol
            GoBackN gbn = new GoBackN(udp);
            gbn.sendFile(inputFile);



        } catch (SocketException e) {
            System.out.println("Socket Error!");
        } catch (UnknownHostException e) {
            System.out.println("Host name is invalid!");
        } catch (FileNotFoundException e) {
            System.out.println("channelInfo not found!");
        } catch (IOException e) {
            System.out.println("File read Error!");
        } catch (Exception e) {
            System.out.println("Unknown Error: " + e.getMessage());
        }
    }
}
