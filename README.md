# TurtleGame

Javaで開発したクライアントサーバ型の複数人対戦ゲームです。  
Turtleライブラリを用いてプレイヤーを描画しています。

## 使用技術

- Java
- Socket通信
- Turtleライブラリ
- Eclipse

## システム構成

- `ChatServer.java`
  - クライアントから送信されたコマンドを処理
  - プレイヤーの位置情報や体力などを管理

- `GameClient.java`
  - サーバから送られてきた情報を描画
  - ユーザーの入力をサーバへ送信

- `TurtleInfo.java`
  - プレイヤー情報（位置、攻撃力、体力など）を管理

## 必要ライブラリ

- `javaEveryone302.jar`

`lib` ディレクトリに配置したあと(多分すでにlibファイル内にあります),Eclipseで Build Path に追加してください。

## 実行方法

### 1. binディレクトリへ移動

```bash
cd bin
```

### 2. サーバ起動

binディレクトリでターミナルを開いて以下を実行：

```bash
java -classpath .:../lib/javaEveryone302.jar ChatServer 50000
```

### 3. クライアント起動

binディレクトリで別ターミナルを開いて以下を実行：

```bash
java -classpath .:../lib/javaEveryone302.jar GameClient localhost 50000 a
```

##遊び方
クライアント起動した後クライアントのターミナルで
```
rotate 50
walk 100
```
とすれば50回転,100進むことができます。
```
attack 100
```
とすれば自分の方向の100経路100以内にいる相手を攻撃できます。
Turtleはエネルギーを10000保有しておりrotate,walk,attackの数値分だけ減っています。
Turtleの攻撃が当たった際に相手のTurtleのエネルギーを大幅に減らすことができます！！

## 開発環境

- Java 21
- Eclipse
- macOS
