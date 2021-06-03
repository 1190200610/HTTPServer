package service;

import data.Data;

import java.io.*;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.SQLOutput;

//对接受到的socket包进行操作，请求处理的线程类
public class RequestExecute extends Thread{
    private Socket socket;

    public RequestExecute(Socket socket) {
        this.socket = socket;
    }

    public void run(){
        //实现服务器请求信息的获取，从socket输入流中取出数据
        InputStream in = null;
        InputStreamReader reader = null;
        BufferedReader bufferedReader = null;
        OutputStream out = null;
        PrintWriter pw = null;

        try{
            //从socket中获取字节输入流
            in = socket.getInputStream();
            reader = new InputStreamReader(in);
            bufferedReader = new BufferedReader(reader);

            //从socket中获取字节输出流
            out = socket.getOutputStream();
            pw = new PrintWriter(out);

            String line = null;
            int lineNum = 1;
            String reqPath = "", host = "";

            while((line = bufferedReader.readLine()) != null){
                System.out.println(line);
                //解析请求行
                if(lineNum == 1) {
                    //取出第一行
                    String[] info = line.split(" ");
                    if(info != null || line.length() > 2){
                        reqPath = info[1];
                    }
                    else{
                        throw new RuntimeException("Request line resolution failed......");
                    }
                }
                else{
                    String[] info = line.split(": ");
                    if(info != null || info.length == 2){
                        //取出host
                        if(info[0].equals("Host")){
                            host = info[1];
                        }
                    }
                }
                lineNum++;
                if(line.equals(""))         //因为是长连接，读取到空行就结束
                    break;
            }

            if(!reqPath.equals("")){                //path不为空时才能讨论
                System.out.println("The processing request is: http://" + host + reqPath);
                //根据请求响应客户端 分类讨论
                if(reqPath.equals("/")){            //对于没有资源的响应
                    pw.println("HTTP/1.1 200 OK");
                    pw.println("Content-Type: text/html;charset=utf-8");
                    pw.println();                   //输出空行，表示响应头结束，开始写响应内容
                    FileInputStream inLogin = new FileInputStream("C:/Temp/res/Login.html");
                    InputStreamReader readerLogin = new InputStreamReader(inLogin);
                    BufferedReader bufferedReaderLogin = new BufferedReader(readerLogin);

                    String lineLogin = null;
                    while((lineLogin = bufferedReaderLogin.readLine())!=null){
                        pw.println(lineLogin);
                        pw.flush();
                    }
                }

                else{                               //如果不是 就查找相应的资源
                    String ext = reqPath.substring(reqPath.lastIndexOf(".") + 1);   //取出后缀，根据最后一个点后面的内容来判断类型
                    reqPath = reqPath.substring(1);
                                                    //去除前面的"/"
                    if(reqPath.contains("/")){      //在子目录下
                        File file = new File(Data.resourcePath + reqPath);
                        if(file.exists() && file.isFile()){
                            response200(out, file.getAbsolutePath(), ext);
                        }
                        else{
                            response404(out);
                        }
                    }

                    else{                           //在根目录下
                        File root = new File(Data.resourcePath);
                        if(root.isDirectory()){
                            File[] list = root.listFiles();
                            boolean isExist = false;
                            for(File file : list){
                                if(file.isFile() && file.getName().equals(reqPath)){    //文件存在
                                    isExist = true;
                                    break;
                                }
                            }
                            if(isExist){
                                response200(out, Data.resourcePath + reqPath, ext);
                            }
                            else{
                                response404(out);
                            }
                        }
                        else{
                            throw new RuntimeException("Static resource directory does not exist" + Data.resourcePath);
                        }
                    }
                }

            }
        }catch (IOException e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }finally {
            try {
                if(in != null){
                    in.close();
                }
                if(reader != null){
                    reader.close();
                }
                if(bufferedReader != null){
                    bufferedReader.close();
                }
                if(pw != null){
                    pw.close();
                }
                if(out != null){
                    out.close();
                }
            }catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }

    private void response200(OutputStream out, String filePath, String ext){
        PrintWriter pw = null;                                              //准备输入流读取磁盘上的文件
        InputStream in = null;
        InputStreamReader reader = null;
        BufferedReader bufferedReader = null;
        try {
            if(ext.equals("jpg") || ext.equals("png") || ext.equals("gif")){
                out.write("HTTP/1.1 200 OK\r\n".getBytes());                //输出响应行
                switch (ext) {
                    case "jpg" -> out.write("Content-Type: image/jpg\r\n".getBytes());
                    case "png" -> out.write("Content-Type: image/png\r\n".getBytes());
                    case "gif" -> out.write("Content-Type: image/gif\r\n".getBytes());    //输出一个空行
                }
                out.write("\r\n".getBytes());                               //输出空行，表示响应头结束

                in = new FileInputStream(filePath);
                int len = -1;
                byte [] buff = new byte[1024];
                while((len = in.read(buff))!=-1){
                    out.write(buff,0,len);
                    out.flush();
                }
            }
            else if(ext.equals("html") || ext.equals("js") || ext.equals("css") ||
                    ext.equals("json") || ext.equals("txt") || ext.equals("md")){
                pw = new PrintWriter(out);
                pw.println("HTTP/1.1 200 OK");                              //输出响应行
                switch (ext) {
                    case "html" -> pw.println("Content-Type: text/html;charset=utf-8");
                    case "js" -> pw.println("Content-Type: application/x-javascript");
                    case "css" -> pw.println("Content-Type: text/css");
                    case "json" -> pw.println("Content-Type: application/json;charset=utf-8");
                    case "txt" -> pw.println("Content-Type: text/txt");
                    case "md" -> pw.println("Content-Type: text/markdown");
                }
                pw.println();                                               //输出空行标志响应头结束

                in = new FileInputStream(filePath);
                reader = new InputStreamReader(in);
                bufferedReader = new BufferedReader(reader);

                String line = null;
                while((line = bufferedReader.readLine())!=null){
                    pw.println(line);
                    pw.flush();
                }
            }
            else{
                response404(out);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try{
                if(pw!=null)
                    pw.close();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    private void response404(OutputStream out){
        PrintWriter pw = null;
        try{
            pw = new PrintWriter(out);
            pw.println("HTTP/1.1 404");
            pw.println("Content-Type: text/html;charset=utf-8");
            pw.println();           //输出空行，表示响应头结束，开始写响应内容
            pw.println("<h2>Welcome to visit My HTTP-Server</h2>");
            pw.write("The resource you are looking for is lost");
            pw.flush();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try{
                if(pw != null){
                    pw.close();
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
}
