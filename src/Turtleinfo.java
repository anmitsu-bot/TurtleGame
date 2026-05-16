import tg.Turtle;

class TurtleInfo {
    String id;      // ソケットのRemoteアドレス
    String name;    // クライアントの名前
    double x, y;    // 位置
    double a;       // 角度
    double e;       // エネルギー

    public TurtleInfo(String id, String name, double x, double y, double a, double e) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.a = a;
        this.e = e;
    }

    @Override
    public String toString() {
        return "generate " + id + " " + name + " " + x + " " + y + " " + a + " " + e;
    }
}