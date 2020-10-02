package vim;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.file.Files;

class UdpTxtSender{
    private static BufferedReader reader=null;
    private static DatagramSocket socket=null;
    private static int fromPort=8888;
    private static String toIP="localhost";
    private static int toPort=9999;
    public UdpTxtSender(){
        try{
            socket=new DatagramSocket(fromPort);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void sendTxt() {
        try {
            // 读取文件
            File txtFile = new File("test.txt");
            FileInputStream fis  = new FileInputStream(txtFile);
            System.out.println(txtFile.exists());
            String txtData="";
            String txtTemp = "";
            reader = new BufferedReader(new FileReader(txtFile));
            while ((txtTemp = reader.readLine()) != null) {
                System.out.println("Line"+ ":" +txtTemp);
                txtData+=txtTemp;
            }
//            System.out.println(textData);
            byte[] datas=txtData.getBytes();
            DatagramPacket packet = new DatagramPacket(datas, 0, datas.length, new InetSocketAddress(toIP, toPort));
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class UdpTxtReceiver implements Runnable {
    DatagramSocket socket=null;
    private int port;
    public UdpTxtReceiver(int port) {
        this.port=port;
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        while (true) {
            try {
                File f =new File("test1.txt");
                //准备接收数据
                byte[] container = new byte[1024];
                DatagramPacket packet = new DatagramPacket(container, 0, container.length);
                socket.receive(packet);//阻塞监听
                byte[] data = packet.getData();
                String receiveData = new String(data);
                OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(f),"gbk");
                BufferedWriter writer=new BufferedWriter(write);
                writer.write(receiveData);
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
