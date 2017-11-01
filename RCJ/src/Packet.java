public class Packet {
    public int type;
    public int length;
    public int sequenceNumber;
    public byte[] data;

    public Packet(int t, int len, int seq, byte[] data) throws Exception {
        type = t;
        length = len;
        sequenceNumber = seq;
        if(data.length > 1000){
            throw new Exception("Data block exceeds 500");
        }
        this.data = data;
    }
    public String getTypeString(){
        String result = "";
        switch(type){
            case 0:
                result = "DAT";
                break;
            case 1:
                result = "ACK";
                break;
            case 2:
                result = "EOT";
                break;
        }
        return result;
    }

}
