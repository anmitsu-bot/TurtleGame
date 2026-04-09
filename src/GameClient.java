import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

import tg.*;

public class GameClient {
    Socket chatS = null;
    BufferedReader in = null;
    PrintStream out = null;
    TurtleFrame f;
    HashMap<String, Turtle> turtles = new HashMap<>();

    static String sName;   // サーバIP
    static int portN;      // ポート番号
    static String uName;   // ユーザ名
    String userName;

    public void start() {
        userName = uName;
        f = new TurtleFrame();
        initNet(sName, portN, userName);
        sendMessage("connect " + userName);
        
        // サーバからの応答処理スレッド
        new Thread(() -> { startChat(); }).start();
        
        // ターミナルでの入力スレッド
        new Thread(() -> { TerminalIn(); }).start();
    }

    public void startChat() {
        String fromServer;
        try {
            while ((fromServer = in.readLine()) != null) {
                System.out.println(fromServer);
                double memo_e = 10000;

                if (fromServer.startsWith("generate ")) {
                    String[] tokens = fromServer.split(" ");
                    String id = tokens[1];
                    String name = tokens[2];
                    double x = Double.parseDouble(tokens[3]);
                    double y = Double.parseDouble(tokens[4]);
                    double a = Double.parseDouble(tokens[5]);
                    double e = Double.parseDouble(tokens[6]);
                    memo_e = e;

                    Turtle t = new Turtle(x, 400 - y, 90 - a);
                    f.add(t);
                    t.setTScale(e / 10000.0);
                    turtles.put(id, t);
                    
                    // 自分のタートルだけ赤く
                    String myID = chatS.getLocalSocketAddress().toString().substring(1);
                    if (id.equals(myID)) {
                        t.setTColor(javafx.scene.paint.Color.RED); // or t.setColor(Color.RED);
                    }

                } else if (fromServer.startsWith("moveto ")) {
                    String[] tokens = fromServer.split(" ");
                    String id = tokens[1];
                    double x = Double.parseDouble(tokens[2]);
                    double y = Double.parseDouble(tokens[3]);
                    double a = Double.parseDouble(tokens[4]);
                    double e = Double.parseDouble(tokens[5]);
                    memo_e = e;

                    Turtle t = turtles.get(id);
                    if (t != null) {
                        t.moveTo(x, 400 - y, 90 - a);
                        t.setTScale(e / 10000.0);
                    }
                }
                else if (fromServer.startsWith("remove ")) {
                    String id = fromServer.substring(7).trim();
                    Turtle t = turtles.remove(id);
                    if (t != null) f.remove(t);
                    String myID = chatS.getLocalSocketAddress().toString().substring(1);
                    if (id.equals(myID)) {
                        System.out.println("サーバから切断されました。終了します。");
                        System.exit(1);
                    }
                } else if (fromServer.startsWith("attack ")) {
                    String myID = chatS.getLocalSocketAddress().toString().substring(1);
                    String[] tokens = fromServer.split(" ");
                    String id = tokens[1];
                    double x = Double.parseDouble(tokens[2]);
                    double y = Double.parseDouble(tokens[3]);
                    double dx = Double.parseDouble(tokens[4]);
                    double dy = Double.parseDouble(tokens[5]);
                    
                    Turtle t = turtles.get(id);
                    if (t != null) {
                        Turtle tmp = new Turtle(t.getX(), t.getY(), t.getAngle());
                        f.add(tmp);
                        tmp.setTColor(javafx.scene.paint.Color.WHITE);
                        tmp.moveTo(dx, 400 - dy);
                        f.remove(tmp);
                    }
                    
                    
                }
            }
        } catch (IOException e) {
            System.out.println("サーバとの通信に問題が発生しました: " + e);
            System.exit(1);
        }
    }
    public void TerminalIn() {
        try (BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = stdin.readLine()) != null) {
                if (line.equalsIgnoreCase("/bye")) {
                    sendMessage("disconnect");
                    end();
                    System.exit(0);
                } else {
                    sendMessage(line);
                }
            }
        } catch (IOException e) {
            System.out.println("標準入力エラー: " + e);
        }
    }

    public void sendMessage(String msg) {
        System.out.println("sendMessage: " + msg);
        out.println(msg);
    }

    public void initNet(String serverName, int port, String uName) {
        userName = uName;
        try {
            chatS = new Socket(InetAddress.getByName(serverName), port);
            in = new BufferedReader(new InputStreamReader(chatS.getInputStream()));
            out = new PrintStream(chatS.getOutputStream());
        } catch (IOException e) {
            System.out.println("ネットワーク接続に失敗しました: " + e);
            System.exit(1);
        }
    }

    public void end() {
        try {
            out.close();
            in.close();
            chatS.close();
        } catch (IOException e) {
            System.out.println("終了処理でエラー: " + e);
        }
    }

    public static void main(String... args) {
        if (args.length != 3) {
            System.out.println("Usage: java GameClient サーバのIPアドレス ポート番号 ユーザ名");
            System.out.println("例: java GameClient 127.0.0.1 50002 taro");
            System.exit(1);
        }
        sName = args[0];
        portN = Integer.parseInt(args[1]);
        uName = args[2];
        new GameClient().start();
    }
}