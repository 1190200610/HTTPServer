package ui;


import data.Data;
import service.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainFrame extends JFrame {

    //要使用的组件申明
    private JLabel labPort;
    private JLabel labInfo;
    private JLabel labPath;
    private JTextField textPort;
    private JTextField textPath;
    private JButton btnStartServer;
    private JButton btnPushServer;
    private JButton btnStopServer;
    private JButton btnSetPath;
    private JPanel contentPanel;

    private JScrollPane scrollPane;
    private JTextArea textArea;

    public MainFrame(){
        init();
    }

    private void init() {
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setBounds(200,200,800,500);
        this.setTitle("张景阳的Http服务器");
        this.setResizable(false);

        //设置主面板
        contentPanel = new JPanel();
        contentPanel.setLayout(null);
        this.setContentPane(contentPanel);

        //端口设置
        labPort = new JLabel("监听的端口号:");
        labPort.setBounds(15,10,100,25);
        contentPanel.add(labPort);

        textPort = new JTextField("8088");
        textPort.setBounds(120,10,150,25);
        contentPanel.add(textPort);

        //三个按钮
        btnStartServer = new JButton("启动服务");
        btnStartServer.setBounds(300,10,120,25);
        contentPanel.add(btnStartServer);

        btnPushServer = new JButton("暂停服务");
        btnPushServer.setBounds(440,10,120,25);
        btnPushServer.setEnabled(false);
        contentPanel.add(btnPushServer);

        btnStopServer = new JButton("停止服务");
        btnStopServer.setBounds(580,10,120,25);
        btnStopServer.setEnabled(false);
        contentPanel.add(btnStopServer);

        //资源路径设置
        labPath = new JLabel("资源路径位置：");
        labPath.setBounds(15,45,100,25);
        contentPanel.add(labPath);

        textPath = new JTextField("");
        textPath.setText(Data.resourcePath);
        textPath.setBounds(130,45,500,25);
        contentPanel.add(textPath);

        btnSetPath = new JButton("设置资源位置");
        btnSetPath.setBounds(640,45,120,25);
        contentPanel.add(btnSetPath);

        //控制台
        textArea = new JTextArea("-- 控制台 --\r\n");
        textArea.setLineWrap(true);
        scrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setBounds(15,80,770,350);
        contentPanel.add(scrollPane);
        btnSetPath.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser();
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int op = jfc.showDialog(MainFrame.this, "请选择静态资源文件夹");
                if(op == JFileChooser.APPROVE_OPTION){
                    File file = jfc.getSelectedFile();
                    String filePath = file.getAbsolutePath();
                    textPath.setText(filePath);
                    Data.resourcePath = filePath;
                }
            }
        });

        //启动按钮事件
        btnStartServer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Data.isRun = true;
                Data.isPush = false;
                int port = 8088;
                try{
                    port = new Integer(textPort.getText().trim());
                }catch (Exception ex){}
                Server server = new Server(port,MainFrame.this);
                new Thread(server).start();
                btnStartServer.setEnabled(false);
                btnStopServer.setEnabled(true);
                btnPushServer.setEnabled(true);
            }
        });

        //暂停按钮事件
        btnPushServer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(Data.isPush){
                    Data.isPush = false;
                    btnPushServer.setText("暂停服务");
                    printLog("服务器继续运行!");
                }else{
                    Data.isPush = true;
                    btnPushServer.setText("继续运行");
                    printLog("服务器暂停运行!");
                }
            }
        });

        //停止按钮事件
        btnStopServer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Data.isRun = false;
                Data.isPush = false;
                int port = 8088;
                try{
                    port = new Integer(textPort.getText().trim());
                }catch (Exception ex){}
                try {
                    Socket socket = new Socket("127.0.0.1",port);//自动请求服务器
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                btnStartServer.setEnabled(true);
                btnPushServer.setEnabled(false);
                btnStopServer.setEnabled(false);
            }
        });
        this.setVisible(true);
    }

    //输出日志到控制台
    public void printLog(final String msg){
        new Thread(){
            public void run(){
                String date = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]").format(new Date());
                String info = textArea.getText()+ date+" "+ msg+"\r\n";
                textArea.setText(info);
            }
        }.start();
    }

    public static void main(String[] args) {
        MainFrame mf = new MainFrame();
    }
}
