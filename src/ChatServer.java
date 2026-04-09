import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import tg.*;
public class ChatServer {
    static int turtleCount = 0;
    public static TurtleInfo generateTurtle(String id, String name) {
        double x = turtleCount * 50.0 + 100.0;
        double y = 200.0;
        double a = 90.0;
        double e = 10000.0;
        turtleCount++;
        new Turtle();
        return new TurtleInfo(id, name, x, y, a, e);
    }
    public static void main(String[] args) throws IOException {
        
        if(args.length != 1) {
            System.exit(1);
        }
        int port = Integer.valueOf(args[0]).intValue();
        ServerSocket serverS = null;
        boolean end = true;
        try {
            serverS = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("ポートにアクセスできません。");
            System.exit(-1);
        }
        while(end){
            new ChatMThread(serverS.accept()).start();
            
        }
        serverS.close();   
    }
}


class ChatMThread extends Thread {
    public static PrintWriter logFile;
    static HashMap<String, TurtleInfo> chatTurtle = new HashMap<>();
    TurtleInfo myTurtle;
            
    static synchronized void writeLog(String s) {
        System.out.println(s);
        if (logFile != null) {
            logFile.println(s);
            logFile.flush();
        }
    }
    public static synchronized ArrayList<TurtleInfo> getAllTurtles() {
        return new ArrayList<>(chatTurtle.values());
    }
    //引数をソケットとしてクライアントと接続するサブクラスChatMThreadを作成
    // クライアント接続ごとにスレッド(ChatMThread)を生成して処理を分離する
    //分離したスレッドはArrayListで格納
    Socket socket;
    PrintWriter out;
    BufferedReader in;
    String TurtleID;
    static ArrayList<ChatMThread> member;
    ChatMThread(Socket s) {
        super("ChatMThread");
        socket = s;

        if (member == null) {
            member = new ArrayList<ChatMThread>();
        }
        member.add(this);
    }

    public synchronized void run() {
        try {
            if (logFile == null) {
                logFile = new PrintWriter("ChatServerLog-" + java.time.LocalDateTime.now() + ".txt");
            }

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            TurtleID = socket.getRemoteSocketAddress().toString().substring(1);
            
            String fromClient;
            while ((fromClient = in.readLine()) != null) {
                if (fromClient.startsWith("connect ")) {
                 // Turtleを全て送信
                    for (TurtleInfo t : getAllTurtles()) {
                        out.println(t.toString());
                    }
                    String TurtleName = fromClient.substring(8).trim();
                    myTurtle = ChatServer.generateTurtle(TurtleID, TurtleName);
                    chatTurtle.put(TurtleID, myTurtle);
                    writeLog("connect " + TurtleName);
                    //自分のタートルを他のクライアントに送信
                    writeLog(myTurtle.toString());
                    for (ChatMThread client : member) {
                            client.out.println(myTurtle.toString());
                    }
                } else if (fromClient.startsWith("rotate ")) {
                    try {
                        double d = Double.parseDouble(fromClient.substring(7));
                        if(d > 20) {
                            d = 20;
                        }else if(d < -20) {
                            d = -20;
                        }
                        myTurtle.a = (myTurtle.a + d) % 360;
                        if (myTurtle.a < 0) myTurtle.a += 360;
                        myTurtle.e -= Math.abs(d);
                        String msg = String.format("moveto %s %.1f %.1f %.1f %.1f",
                                myTurtle.id, myTurtle.x, myTurtle.y, myTurtle.a, myTurtle.e);
                        
                        for (ChatMThread client : member) {
                                client.out.println(msg);
                        }
                        writeLog(msg);
                        if(myTurtle.e <= 10){
                            for (ChatMThread client : member) {
                                client.out.println("remove " + TurtleID);
                            }
                        }
                    } catch (NumberFormatException e) {
                        writeLog("不正なrotateコマンド: " + fromClient);
                    }

                } else if (fromClient.startsWith("walk ")) {
                    try {
                        double d = Double.parseDouble(fromClient.substring(5));
                        if (d > 50) {
                            d = 50;
                        }else if (d < -50) {
                            d = -50;
                        }
                        myTurtle.e -= Math.abs(d);
                        double rad = Math.toRadians(myTurtle.a);
                        myTurtle.x += d * Math.cos(rad);
                        myTurtle.y += d * Math.sin(rad);
                        String msg = String.format("moveto %s %.1f %.1f %.1f %.1f",
                                myTurtle.id, myTurtle.x, myTurtle.y, myTurtle.a, myTurtle.e);
                        
                        for (ChatMThread client : member) {
                                client.out.println(msg);
                        }
                        writeLog(msg);
                        if(myTurtle.e <= 10){
                            for (ChatMThread client : member) {
                                client.out.println("remove " + TurtleID);
                            }
                        }
                    } catch (NumberFormatException e) {
                        writeLog("不正なwalkコマンド: " + fromClient);
                    }

                }else if (fromClient.startsWith("attack ")) {
                    try {
                        double n = Double.parseDouble(fromClient.substring(7));
                        if ( n < 0 ) {
                            writeLog("不正なattack距離: " + n + "（-100.0〜100.0の範囲）");
                            continue;
                        }
                        myTurtle.e -= Math.abs(n);
                        double rad = Math.toRadians(myTurtle.a);
                        double damage_x = myTurtle.x + n * Math.cos(rad);
                        double damage_y = myTurtle.y + n * Math.sin(rad);
                        String msg = String.format("attack %s %.1f %.1f " + damage_x +" "+ damage_y,
                                myTurtle.id, myTurtle.x, myTurtle.y);
                        for (ChatMThread client : member) {
                                client.out.println(msg);
                        }
                        writeLog(msg);
                        String attacked_id = null;
                        TurtleInfo attacked;
                        for(int i = (int) Math.floor(n) ; i >= 1 ; i--) {
                                for(Entry<String, TurtleInfo> entry : chatTurtle.entrySet()) {
                                    attacked = chatTurtle.get(entry.getKey());
                                    if(attacked != myTurtle) {
                                    double move_x = myTurtle.x + i * Math.cos(rad);
                                    double move_y = myTurtle.y + i * Math.sin(rad);
                                    double large = Math.hypot(Math.abs(move_x - attacked.x), Math.abs(move_y - attacked.y));
                                    if(large <= 10 || large == 0) {
                                        attacked_id = entry.getKey();
                                    }
                                }
                            }
                        }
                        if(attacked_id != null) {
                            attacked = chatTurtle.get(attacked_id);
                            attacked.e = attacked.e - 2000;
                            String move = String.format("moveto %s %.1f %.1f %.1f %.1f",
                                    attacked.id, attacked.x, attacked.y, attacked.a, attacked.e);
                            
                            for (ChatMThread client : member) {
                                client.out.println(move);
                            }
                            if(attacked.e <= 10){
                                for (ChatMThread client : member) {
                                    client.out.println("remove " + attacked_id);
                                }
                            }
                        }else {
                            myTurtle.e = myTurtle.e - Math.pow((n / 2),2);
                            String move = String.format("moveto %s %.1f %.1f %.1f %.1f",
                                    myTurtle.id, myTurtle.x, myTurtle.y, myTurtle.a, myTurtle.e);
                            for (ChatMThread client : member) {
                                client.out.println(move);
                            }
                            if(myTurtle.e <= 10){
                                for (ChatMThread client : member) {
                                    client.out.println("remove " + attacked_id);
                                }
                            }
                        }
                    } catch (NumberFormatException e) {
                        writeLog("不正なattackコマンド: " + fromClient);
                    }
                }
                else if(fromClient.equals("exit")) {
                    
                    String msg = "remove " + TurtleID;
                    for (ChatMThread client : member) {
                        client.out.println(msg);
                    }
                    writeLog(msg);
                    
                    
                }else {
                    writeLog("不明なコマンド: " + fromClient);
                }
            }

        } catch (IOException e) {
            System.out.println("run: " + e);
        }
        end();
    }

    public void end() {
        //接続終了時に入出力もソケットも終了してArrayListも削除する
        try {
            String TurtleID = socket.getRemoteSocketAddress().toString().substring(1);
            chatTurtle.remove(TurtleID);
            String removeMsg = "remove " + TurtleID;
            out.println(removeMsg);
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) { System.out.println("end:" + e); }
        member.remove(this);
    }
}