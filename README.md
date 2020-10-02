# 文件说明

文本文件：

* test.txt是初始化文本域的内容

* test1.txt是远程传输过来的文本

* testCache.txt是定时备份后的文本

程序文件：

* FileProcess.java用来收、发文件
* MyFrame.java写完了vim里面要求的所有功能
* Vim.java实例化了一个MyFrame

# 制作大纲

![image-20201002163139582](https://img-blog.csdnimg.cn/20201002170110499.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2NTIxNzg1,size_16,color_FFFFFF,t_70#pic_cente)

# √基本的vim界面

文件：MyFrame

* 设置字符串pattern表示当前模式状态

* 写一个键盘监听类==MyListener extends KeyAdapter==，然后再绑定给JTextArea==myTextArea.addKeyListener(new MyListener());==

* 在键盘监听类里面判断是按下了什么按键
  * 判断全局按键，即任意模式
  * 如果是直接按了其他命令键，再去执行相应的命令

```java
package vim;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MyFrame extends JFrame{
    public String pattern = "Input";// 是什么模式  Ordinary普通模式  Input输入模式  Command命令模式
    public Robot myRobot;// 控制键盘的机器人
    public JTextArea myTextArea = new JTextArea();
    

    public MyFrame(){
        try {
            myRobot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        setBounds(200,200,820,640);
        myTextArea.setText("我爱你");
        addWindowListener(
                new WindowAdapter(){
                    public void windowClosing(WindowEvent e) {
                        setVisible(false);
                        System.exit(0);
                    }
                }
        );
        myTextArea.addKeyListener(new MyListener());
        add(myTextArea);
        setVisible(true);
    }
    //添加键盘事件监听
    private class MyListener extends KeyAdapter {
        //重写keyPressed方法
        public void keyPressed(KeyEvent e){
            //康康是哪个按钮被按下了.//enter:10  esc:27
            int keyCode = e.getKeyCode();
            String keyText = KeyEvent.getKeyText(keyCode);
            System.out.println(keyCode+"  "+keyText);
            if (keyText.equals("Enter")){// 回车被按下
                String s =myTextArea.getText();
                String[] lineText = s.split("\n");
                int lineCount =myTextArea.getLineCount();
                String cmd=lineText[lineCount-1];
                System.out.println(cmd);
            }
//            既然是键入应该是按了回车嘿嘿
//            else if(keyText.equals("I")){
//                System.out.println("输入模式");
//            }else if(keyText.equals("Esc")){
//                System.out.println("正常模式");
//            }else if(keyText.equals(":")){
//                System.out.println("命令模式");
//            }
        }
    }
    public static void main(String[] args) {
        new MyFrame();
    }
}
```



# √控制键盘游标上下左右移动

文件：MoveCursor.java

创建机器人，判断按下了h 、 j 、 k 、 l ，来进⾏光标的左 、 上 、 下 、 右 移动

```java
package vim;
import java.awt.*;
import java.awt.event.KeyEvent;

public class MoveCursor {
    private Robot myRobot;
    public MoveCursor(){
        try {
            myRobot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
    public void move(String position){
        if (position.equals("up")){
            myRobot.keyPress(KeyEvent.VK_UP);
            myRobot.keyRelease(KeyEvent.VK_UP);
        }else if(position.equals("down")){
            myRobot.keyPress(KeyEvent.VK_DOWN);
            myRobot.keyRelease(KeyEvent.VK_DOWN);
        }else if(position.equals("left")){
            myRobot.keyPress(KeyEvent.VK_LEFT);
            myRobot.keyRelease(KeyEvent.VK_LEFT);
        }else{
            myRobot.keyPress(KeyEvent.VK_RIGHT);
            myRobot.keyRelease(KeyEvent.VK_RIGHT);
        }
    }
}
```

# √监听键盘

```java
package vim;

import java.awt.*;
import java.awt.event.*;
public class TextAreaKeyEvent extends JFrame{
    private JTextArea t;
    public TextAreaKeyEvent(){
        t = new JTextArea();
    }
    //构建运行时候的窗体
    public void launch(){
        setBounds(200,200,400,300);
        setTitle("keyEvent Demo");
        add(t);
        addWindowListener(
                new WindowAdapter(){
                    public void windowClosing(WindowEvent e) {
                        setVisible(false);
                        System.exit(0);
                    }
                }
        );
        //加入键盘监听器
        t.setEditable(false);
        t.setBackground(Color.WHITE);
        t.addKeyListener(new KeyActionListener());
        setVisible(true);
    }
    //添加键盘事件监听
    private class KeyActionListener extends KeyAdapter {
        //重写keyPressed方法
        public void keyPressed(KeyEvent e){
            int keyCode = e.getKeyCode();
            String s = t.getText();
            if(s.equals("")){
                t.setText( "键盘" + KeyEvent.getKeyText(keyCode) + "被按下");
            }else{
                t.setText(s + "\n" + "键盘" + KeyEvent.getKeyText(keyCode) + "被按下");
            }
        }
    }
    public static void main(String[] args){
        new TextAreaKeyEvent().launch();
    }
}
```





# √socket实现UDP传输文件夹

最开始的思路是截屏然后对比两张图片的变化，后来发现没有逻辑性，然后就想保存后再发送（虽然不能实时了）。

其实我想的是只要文本内容变化了就发送，不管怎样，先把UDP写完再说吧

注意事项：

* 服务器默认端口号是9999，客户端默认端口号是8888
* 服务器是多线程运行的，while(true)一直阻塞接收

服务端：

```java
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
```

客户端：

```java
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
```

# √定时备份文件

写一个线程，一直去循环，获取文本域内容，然后写入备份文件。使用Thread.sleep()来实现定时备份

# √寄存器

打开寄存器后，把命令按键代码放入ArrayList里面，然后输入@a后就使用机器人，循环模拟按下按键

# 怎么判断是否按下了两个按键？

这个方法有点low，然后其实还有点小bug，但是因为当时就这么走下来的，就没改了。

给大家再提供一种思路，创建一个ArrayList或者Map用来存放谁按下了的状态，然后判断另外一个，如果两个都存在就从容器里删除，然后执行对应命令。

我的傻方法，以qa为例，

```java
if(cmd.equals("q") && qaCache.equals("")){qaCache="q";}
if(cmd.equals("q") && qaCache.equals("qa")){qaCache="";}
if(cmd.equals("a") && qaCache.equals("q")){
    qaCache="qa";
    System.out.println("打开寄存器");
}
```



