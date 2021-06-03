package service;

import data.Data;
import ui.MainFrame;

import java.net.ServerSocket;
import java.net.*;

//来做服务监听
public class Server implements Runnable{
    private int port;
    private MainFrame frame;

    public Server(int port, MainFrame frame) {
        this.port = port;
        this.frame = frame;
    }

    public void run(){
        //线程的主程序
        ServerSocket serverSocket = null;
        try{
            //创建ServerSocket对象
            serverSocket = new ServerSocket(port);
            System.out.println("Start monitoring......");
            frame.printLog("Service is monitoring......");
            frame.printLog("Monitoring:" + port);
            frame.printLog("Path of the static recourse" + Data.resourcePath);
            while(Data.isRun){
                Socket socket = serverSocket.accept();
                System.out.println("Request received......");
                RequestExecute re = new RequestExecute(socket);
                re.start();
            }
            //停止监听
            serverSocket.close();
            serverSocket = null;
            frame.printLog("Monitoring service stops!");
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("port" + port + "Monitoring failed" + e.getMessage());
        }

    }

}
