package vim;


import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyFrame extends JFrame{
    public String txtCache="";
    BufferedReader reader=null;
    public String pattern = "Ordinary";// 是什么模式  Ordinary普通模式  Input输入模式  Command命令模式
    public String cmd="";
    public String ordinaryCmdCache="";
    public String qaCache="";
    public String doaCache="";
    public int commandFlag=0;
    public ArrayList<String> positionList=new ArrayList<>();
    public ArrayList<String> ordinartCmdList=new ArrayList<>();
    public Robot myRobot;// 控制键盘的机器人
    public JTextArea myTextArea = new JTextArea();
    public int offset;
    public int start;
    public int end;
    public String paste;
    public JTextField cmdFiled= new JTextField();
    public File txtFile = new File("test.txt");
    public File backupFile = new File("testCache.txt");
    public ArrayList<Integer> keyEvents = new ArrayList<Integer>();

    public MyFrame(){
        readTxt();
        new Thread(new UdpTxtReceiver(9999)).start();
        backupFile();
        positionList.add("h");
        positionList.add("j");
        positionList.add("k");
        positionList.add("l");
        ordinartCmdList.add("dd");
        ordinartCmdList.add("yy");
        ordinartCmdList.add("p");
        try {
            myRobot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        setLayout(null);
        setBounds(200,200,813,660);
        addWindowListener(
                new WindowAdapter(){
                    public void windowClosing(WindowEvent e) {
                        setVisible(false);
                        System.exit(0);
                    }
                }
        );
        myTextArea.addKeyListener(new MyListener());
        cmdFiled.addKeyListener(new MyListener());
        myTextArea.getCaret().addChangeListener(new ChangeListener()   {
            public   void   stateChanged(ChangeEvent e)   {
                myTextArea.getCaret().setVisible(true);   //使Text区的文本光标显示
            }
        });
        myTextArea.setLineWrap(true);
        myTextArea.setEditable(false);
        myTextArea.setBounds(0, 0, 800, 600);
        cmdFiled.setBounds(0, 600, 800, 20);
        cmdFiled.setEditable(false);
        add(myTextArea);
        add(cmdFiled);
        setVisible(true);
    }
    //添加键盘事件监听
    private class MyListener extends KeyAdapter {
        //重写keyPressed方法
        public void keyPressed(KeyEvent e){
            //康康是哪个按钮被按下了.//enter:10  esc:27
            int keyCode = e.getKeyCode();
            String keyText = KeyEvent.getKeyText(keyCode);
            //切换到命令模式，即:
            if(keyCode==16 || keyCode==59){ commandFlag+=1; }
            if(commandFlag>2){ commandFlag=0; }
            System.out.println(keyCode+"  "+keyText);
            //普通模式
            if(pattern.equals("Ordinary")) {
                cmd = keyText.toLowerCase();
                for (String ordinartCmd:ordinartCmdList) {
                    if(ordinartCmd.startsWith(cmd.toLowerCase())){
                        ordinaryCmdCache+=cmd.toLowerCase();
                        cmdFiled.setText(ordinaryCmdCache);
                    }
                    if(ordinartCmd.equals(ordinaryCmdCache)){
                        cmd=ordinaryCmdCache;
                        ordinaryCmdCache="";
                        cmdFiled.setText("");
                        break;
                    }
                }
                doOrdinaryCmd();
            }else if(pattern.equals("Command") && keyText.equals("Enter")) {
                cmd =cmdFiled.getText();
                cmdFiled.setText("");
                doCommandCmd();
                cmdFiled.setEditable(false);
                pattern="Ordinary";
            }
            //全局可以切换
            if(keyText.equals("I")){
                System.out.println("输入模式");
                pattern="Input";
                myTextArea.setEditable(true);
                cmdFiled.setEditable(false);
                cmdFiled.setText("Insert");
                myRobot.keyPress(KeyEvent.VK_BACK_SPACE);
            }else if(keyText.equals("Esc")){
                System.out.println("普通模式");
                pattern="Ordinary";
                myTextArea.setEditable(false);
                myTextArea.setCursor(new Cursor(Cursor.TEXT_CURSOR));
                myTextArea.requestFocus();
                cmdFiled.setEditable(false);
                cmdFiled.setText("");
            }else if((keyText.equals("分号")&&commandFlag==2&&pattern.equals("Ordinary")) || (pattern.equals("Ordinary") && keyText.equals("斜杠"))){
                System.out.println("命令模式");
                commandFlag=0;
                pattern="Command";
                myTextArea.setEditable(false);
                cmdFiled.setEditable(true);
                cmdFiled.requestFocus();
                if(keyText.equals("斜杠")){
                    cmdFiled.setText("/");
                }else{
                    cmdFiled.setText(":");
                }
            }
        }
    }
    // 读取文件
    private void readTxt(){
        System.out.println(txtFile.exists());
        String txtData="";
        String txtTemp = "";
        try {
            reader = new BufferedReader(new FileReader(txtFile));
            while ((txtTemp = reader.readLine()) != null) {
                txtData+=txtTemp+"\n";
            }
            txtCache=txtData;
            System.out.println(txtCache);
            myTextArea.setText(txtCache);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // 写入文件
    private void writeTxt(int fileType) {
        File fileName=txtFile;
        if(fileType==2){fileName=backupFile;}
        try {
            OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(fileName),"gbk");
            BufferedWriter writer=new BufferedWriter(write);
            writer.write(myTextArea.getText());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //普通模式
    private void moveCursor(String position) {
        if (position.equals("h")) {
            myRobot.keyPress(KeyEvent.VK_LEFT);
            myRobot.keyRelease(KeyEvent.VK_LEFT);
            if(qaCache.equals("qa")){keyEvents.add(0x25);}
        } else if (position.equals("j")) {
            myRobot.keyPress(KeyEvent.VK_UP);
            myRobot.keyRelease(KeyEvent.VK_UP);
            if(qaCache.equals("qa")){keyEvents.add(0x26);}
        } else if (position.equals("k")) {
            myRobot.keyPress(KeyEvent.VK_DOWN);
            myRobot.keyRelease(KeyEvent.VK_DOWN);
            if(qaCache.equals("qa")){keyEvents.add(0x28);}
        } else if (position.equals("l")) {
            myRobot.keyPress(KeyEvent.VK_RIGHT);
            myRobot.keyRelease(KeyEvent.VK_RIGHT);
            if(qaCache.equals("qa")){keyEvents.add(0x27);}
        }
    }

    private void doOrdinaryCmd(){
        //寄存器
        if(cmd.equals("q") && qaCache.equals("")){qaCache="q";}
        if(cmd.equals("q") && qaCache.equals("qa")){qaCache="";}
        if(cmd.equals("a") && qaCache.equals("q")){
            qaCache="qa";
            System.out.println("打开寄存器");
        }
        System.out.println(qaCache);
        if(cmd.equals("2") && doaCache.equals("")){doaCache="@";}
        if(cmd.equals("a") && doaCache.equals("@")){
            System.out.println("执行寄存器");
            doaCache="";
            for (int key:keyEvents
                 ) {
                myRobot.keyPress(key);
                myRobot.keyRelease(key);
            }
            keyEvents.clear();
        }
        //平常命令
        if (positionList.indexOf(cmd)>=0){// 写好了
            moveCursor(cmd);
        }
        System.out.println(cmd);
        if(cmd.equals("dd")){// 写好了
            if(qaCache.equals("qa")){keyEvents.add(0x44);keyEvents.add(0x44);}
            //删除一行
            System.out.println("删除一行");
            //获取行
            try{
                offset = myTextArea.getLineOfOffset(myTextArea.getCaretPosition());
                start = myTextArea.getLineStartOffset(offset);
                end = myTextArea.getLineEndOffset(offset);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            System.out.println(start+" "+end);
            try {
                myTextArea.setText(myTextArea.getText(0,start)+myTextArea.getText(end,myTextArea.getLineEndOffset(myTextArea.getLineCount()-1)-end));
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }else if(cmd.equals("yy")){
            if(qaCache.equals("qa")){keyEvents.add(0x59);keyEvents.add(0x59);}
            //复制一行
            System.out.println("复制一行");
            try {
                offset = myTextArea.getLineOfOffset(myTextArea.getCaretPosition());
                start = myTextArea.getLineStartOffset(offset);
                end = myTextArea.getLineEndOffset(offset);
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
            try {
                paste = myTextArea.getText(start,end-start);
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        }else if(cmd.equals("p")){
            if(qaCache.equals("qa")){keyEvents.add(0x50);}
            //粘贴到光标所在下一行
            System.out.println("粘贴一行");
            try {
                offset = myTextArea.getLineOfOffset(myTextArea.getCaretPosition());
                start = myTextArea.getLineStartOffset(offset);
                end = myTextArea.getLineEndOffset(offset);
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
            try {
                myTextArea.setText(myTextArea.getText(0,end)+paste+myTextArea.getText(end,myTextArea.getLineEndOffset(myTextArea.getLineCount()-1)-end));
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        }
    }

    //命令模式
    /*
     * 普通模式传过来的命令，已经去除了:
     * 1.q不保存退出(如果⽂件被修改，则提⽰未保存，并终⽌退出命令，即只有⽂件未做修改是才能退出)。
     * 2.w保存但不退出。
     * 3.x保存并推出。
     * 4.q!⽂件已修改但不想保存修改，使⽤则放弃修改并退出。
     * 5.%s/foo/fool ，查找/foo并替换为/fool（⽰例!foo、fool均为⽤⼾输⼊）
     * 6./foo，则匹配⽂本中的/foo字符串
     * */
    private void doCommandCmd(){
        if(cmd.equals(":q")){
            System.out.println("不保存退出");
            String nowTxt=myTextArea.getText();
            if(!(nowTxt.equals(txtCache))){
                cmdFiled.setText("文件未保存");
            }else{
                dispose();
            }
        }else if(cmd.equals(":w")){
            System.out.println("保存不退出");
            txtCache=myTextArea.getText();
            writeTxt(1);
            UdpTxtSender.sendTxt();
        }else if(cmd.equals(":x")){
            System.out.println("保存并退出");
            txtCache=myTextArea.getText();
            writeTxt(1);
            UdpTxtSender.sendTxt();
            dispose();
        }else if(cmd.equals(":q!")){
            System.out.println("放弃修改退出");
            dispose();
        }else if(cmd.startsWith(":%s")){
            System.out.println("查找并替换");
            System.out.println(cmd);
            String[] split = cmd.split("/");
            for (String i:split
                 ) {
                System.out.println(i);
            }
            Pattern p = Pattern.compile(split[1]);
            Matcher m = p.matcher(myTextArea.getText());
            if(m.find()){
                System.out.println(m.replaceAll(split[2]));
                myTextArea.setText(m.replaceAll(split[2]));
            } else {
                cmdFiled.setText("未匹配");
            }
        }else if(cmd.startsWith("/")){
            System.out.println("查找字符");
            String text=myTextArea.getText();
            String[] split = text.split("\n");
            int index = 0;
            for(int i=0;i<split.length;i++){
                if(split[i].contains(cmd.substring(1,cmd.length()))){
                    index += split[i].indexOf(cmd.substring(1,cmd.length()));
                    break;
                }else{
                    index+=split[i].length()+1;
                }
            }
            System.out.println(index);
            myTextArea.requestFocus();
            myTextArea.setEditable(true);
            myTextArea.setSelectionStart(index);
            myTextArea.setSelectionEnd(index+cmd.length()-1);
            myRobot.keyPress(KeyEvent.VK_SHIFT);
            myRobot.keyRelease(KeyEvent.VK_SHIFT);
            myTextArea.setEditable(false);
        }
    }
    // 备份文件
    public void backupFile(){
        //匿名类
        Thread t1= new Thread(() -> {
            while (true) {

                String txtContent = myTextArea.getText();
                if(backupFile.exists()){
                    boolean delete = backupFile.delete();
                    System.out.println("存在了"+delete);
                }
                try {
                    writeTxt(2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("文件已备份");
                try {
                    Thread.sleep(1000*5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t1.start();
    }

    public static void main(String[] args) {
        new MyFrame();
    }
}