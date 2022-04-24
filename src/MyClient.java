import java.net.*;
import java.io.*;
import javax.swing.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class MyClient extends JFrame implements MouseListener, MouseMotionListener {
    PrintWriter out;//出力用のライター
    private JButton buttonArray[][], passButton;//ボタン用の配列
    private int myColor;//偶数→黒、奇数→白
    private int myTurn;//
    private int countPass = 0;
    private ImageIcon myIcon, yourIcon;
    private Container c;
    private ImageIcon blackIcon, whiteIcon, boardIcon;

    public MyClient() {
        //名前の入力ダイアログを開く
        String myName = JOptionPane.showInputDialog(null, "名前を入力してください", "名前の入力", JOptionPane.QUESTION_MESSAGE);
        String myIpAddress = JOptionPane.showInputDialog(null, "サーバのIPアドレスを入力してください", "サーバのIPアドレスの入力", JOptionPane.QUESTION_MESSAGE);
        if (myName.equals("")) {
            myName = "No name";//名前がないときは，"No name"とする
        }

        //ウィンドウを作成する
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//ウィンドウを閉じるときに，正しく閉じるように設定する
        setTitle("MyClient");//ウィンドウのタイトルを設定する
        setSize(1100, 650);//ウィンドウのサイズを設定する
        c = getContentPane();//フレームのペインを取得する

        //アイコンの設定
        whiteIcon = new ImageIcon("./resources/White.jpg");
        blackIcon = new ImageIcon("./resources/Black.jpg");
        boardIcon = new ImageIcon("./resources/GreenFrame.jpg");

        c.setLayout(null);//自動レイアウトの設定を行わない
        //ボタンの生成
        buttonArray = new JButton[8][8];//ボタンの配列を５個作成する[0]から[4]まで使える
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                buttonArray[i][j] = new JButton(boardIcon);//ボタンにアイコンを設定する
                c.add(buttonArray[i][j]);//ペインに貼り付ける
                buttonArray[i][j].setBounds(j * 50, i * 50, 50, 50);//ボタンの大きさと位置を設定する．(x座標，y座標,xの幅,yの幅）
                buttonArray[i][j].addMouseListener(this);//ボタンをマウスでさわったときに反応するようにする
                buttonArray[i][j].addMouseMotionListener(this);//ボタンをマウスで動かそうとしたときに反応するようにする
                buttonArray[i][j].setActionCommand(Integer.toString(i * 8 + j));//ボタンに配列の情報を付加する（ネットワークを介してオブジェクトを識別するため）
            }
        }
        buttonArray[3][3].setIcon(blackIcon);
        buttonArray[3][4].setIcon(whiteIcon);
        buttonArray[4][4].setIcon(blackIcon);
        buttonArray[4][3].setIcon(whiteIcon);
        //パスボタンの生成
        passButton = new JButton("PASS");
        c.add(passButton);
        passButton.setBounds(400, 0, 100, 400);
        passButton.addMouseListener(this);

        //サーバに接続する
        Socket socket = null;
        try {
            //"localhost"は，自分内部への接続．localhostを接続先のIP Address（"133.42.155.201"形式）に設定すると他のPCのサーバと通信できる
            //10000はポート番号．IP Addressで接続するPCを決めて，ポート番号でそのPC上動作するプログラムを特定する
            if (myIpAddress == null) {
                socket = new Socket("localhost", 10000);
            } else {
                socket = new Socket(myIpAddress, 10000);
            }
        } catch (UnknownHostException e) {
            System.err.println("ホストの IP アドレスが判定できません: " + e);
        } catch (IOException e) {
            System.err.println("エラーが発生しました: " + e);
        }

        MesgRecvThread mrt = new MesgRecvThread(socket, myName);//受信用のスレッドを作成する
        mrt.start();//スレッドを動かす（Runが動く）
    }

    public static void main(String[] args) {
        MyClient net = new MyClient();
        net.setVisible(true);
    }

    public void mouseClicked(MouseEvent e) {//ボタンをクリックしたときの処理
        if (myTurn == 0) {
            //相手のターンのとき

        } else {
            //自分のターンのとき
            System.out.println("クリック");
            JButton theButton = (JButton) e.getComponent();//クリックしたオブジェクトを得る．型が違うのでキャストする
            String theArrayIndex = theButton.getActionCommand();//ボタンの配列の番号を取り出す
            Icon theIcon = theButton.getIcon();//theIconには，現在のボタンに設定されたアイコンが入る
            //パスボタンを押したときの処理
            if (theButton == passButton) {
                //サーバに情報を送る
                String msg = "PASS";
                out.println(msg);
                out.flush();
            }
            if (theIcon == boardIcon) {
                int temp = Integer.parseInt(theArrayIndex);
                int y = temp / 8;
                int x = temp % 8;

                if (judgeButton(y, x)) {
                    //置ける
                    String msg = "PLACE" + " " + theArrayIndex + " " + myColor;
                    //サーバに情報を送る
                    out.println(msg);//送信データをバッファに書き出す
                    out.flush();//送信データをフラッシュ（ネットワーク上にはき出す）する
                } else {
                    //置けない
                    System.out.println("そこには配置できません");
                }

                repaint();//画面のオブジェクトを描画し直す
            }
        }
    }

    public void mouseEntered(MouseEvent e) {//マウスがオブジェクトに入ったときの処理
        /*System.out.println("マウスが入った")*/
        ;
    }

    public void mouseExited(MouseEvent e) {//マウスがオブジェクトから出たときの処理
        /*System.out.println("マウス脱出");*/
    }

    public void mousePressed(MouseEvent e) {//マウスでオブジェクトを押したときの処理（クリックとの違いに注意）
        /*System.out.println("マウスを押した");*/
    }

    public void mouseReleased(MouseEvent e) {//マウスで押していたオブジェクトを離したときの処理
        /*System.out.println("マウスを放した")*/
    }

    public void mouseDragged(MouseEvent e) {//マウスでオブジェクトとをドラッグしているときの処理

    }

    public void mouseMoved(MouseEvent e) {//マウスがオブジェクト上で移動したときの処理

    }

    public boolean judgeButton(int y, int x) {
        boolean flag = false;

        //色々な条件からflagをtrueにするか判断する
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                if (flipButtons(y, x, j, i) >= 1) {
                    //ひっくり返せるコマがあるとき

                    for (int dy = j, dx = i, k = 0; k < flipButtons(y, x, j, i); k++, dy += j, dx += i) {
                        if (dy == 0 && dx == 0) {
                            continue;
                        }
                        //ボタンの位置情報を作る
                        int msgy = y + dy;
                        int msgx = x + dx;
                        int theArrayIndex = msgy * 8 + msgx;

                        //サーバに情報を送る
                        String msg = "FLIP" + " " + theArrayIndex + " " + myColor;
                        out.println(msg);
                        out.flush();
                    }
                    flag = true;
                }
            }
        }

        return flag;
    }

    public int flipButtons(int y, int x, int j, int i) {//ひっくり返せるコマの個数を返す
        int flipNum = 0;
        for (int dy = j, dx = i; ; dy += j, dx += i) {
            if ((y + dy) < 0 || 7 < (y + dy) || (x + dx) < 0 || 7 < (x + dx))
                //場外
                return 0;
            if (buttonArray[y + dy][x + dx].getIcon() == boardIcon) {
                //判定終了
                return 0;
            } else if (buttonArray[y + dy][x + dx].getIcon() == myIcon) {
                //連鎖ストップ
                return flipNum;
            } else if (buttonArray[y + dy][x + dx].getIcon() == yourIcon) {
                //連鎖が続く
                flipNum++;
            }
        }
    }

    //メッセージ受信のためのスレッド
    public class MesgRecvThread extends Thread {

        Socket socket;
        String myName;

        public MesgRecvThread(Socket s, String n) {
            socket = s;
            myName = n;
        }

        //通信状況を監視し，受信データによって動作する
        public void run() {
            try {
                InputStreamReader sisr = new InputStreamReader(socket.getInputStream());
                BufferedReader br = new BufferedReader(sisr);
                out = new PrintWriter(socket.getOutputStream(), true);
                out.println(myName);//接続の最初に名前を送る
                boolean isGameSet = false;
                String myNumberStr = br.readLine();//サーバが送った番号を受け取る
                int myNumberInt = Integer.parseInt(myNumberStr);//文字を数字に変換する
                if (myNumberInt % 2 == 0) { // 偶数のとき
                    myColor = 0; //黒
                    myTurn = 0;
                    myIcon = blackIcon;
                    yourIcon = whiteIcon;
                } else {
                    myColor = 1; //白
                    myTurn = 1;
                    myIcon = whiteIcon;
                    yourIcon = blackIcon;
                }


                while (true) {
                    String inputLine = br.readLine();//データを一行分だけ読み込んでみる
                    if (inputLine != null) {//読み込んだときにデータが読み込まれたかどうかをチェックする
                        System.out.println(inputLine);//デバッグ（動作確認用）にコンソールに出力する
                        String[] inputTokens = inputLine.split(" ");    //入力データを解析するために、スペースで切り分ける
                        String cmd = inputTokens[0];//コマンドの取り出し．１つ目の要素を取り出す
                        if (cmd.equals("PLACE")) {
                            //PLACEの時の処理(コマの配置の処理)
                            myTurn = 1 - myTurn;
                            String theBName = inputTokens[1];//ボタンの名前（番号）の取得
                            int theBnum = Integer.parseInt(theBName);//ボタンの名前を数値に変換する
                            int theColor = Integer.parseInt(inputTokens[2]);//数値に変換する
                            int y = theBnum / 8;
                            int x = theBnum % 8;
                            if (theColor == myColor) {
                                buttonArray[y][x].setIcon(myIcon);
                            } else {
                                buttonArray[y][x].setIcon(yourIcon);
                            }
                            //ゲーム終了の判定
                            int countBoardIcon = 0;
                            for(JButton array[]: buttonArray){
                                for (JButton value:array){
                                    if(value.getIcon().toString().equals("./resources/GreenFrame.jpg")) {
                                        //コマが置かれていないマスがあったとき
                                        countBoardIcon++;
                                    }
                                }
                            }
                            if(countBoardIcon == 0){
                            isGameSet = true;
                            }
                        } else if (cmd.equals("FLIP")) {
                            //FLIPの時の処理(コマをひっくり返す処理)
                            String theBName = inputTokens[1];//ボタンの名前（番号）の取得
                            int theBnum = Integer.parseInt(theBName);//ボタンの名前を数値に変換する
                            int theColor = Integer.parseInt(inputTokens[2]);//数値に変換する
                            int y = theBnum / 8;
                            int x = theBnum % 8;
                            if (theColor == myColor) {
                                buttonArray[y][x].setIcon(myIcon);
                            } else {
                                buttonArray[y][x].setIcon(yourIcon);
                            }
                            countPass = 0;
                        } else if (cmd.equals("PASS")) {
                            //2回連続でパスが選択されたときの処理
                            if(countPass != 0){
                                isGameSet = true;
                            }
                            countPass++;
                            System.out.println(countPass);
                            myTurn = 1 - myTurn;
                        }
                        if(isGameSet){
                            //勝敗の判定
                            System.out.println("GAMESET!!");
                            int countBlackIcon = 0;
                            for(JButton array[]: buttonArray){
                                for (JButton value:array){
                                    System.out.println(value.getIcon().toString());
                                    if(value.getIcon().toString().equals("./resources/Black.jpg")) {
                                        countBlackIcon++;
                                    }
                                }
                            }
                            if(countBlackIcon == 32){
                                System.out.println("DRAW");
                            } else if(countBlackIcon > 32){
                                System.out.println("WIN:BLACK");
                            } else{
                                System.out.println("WIN:WHITE");
                            }
                        }
                    } else {
                        break;
                    }
                }
                socket.close();
            } catch (IOException e) {
                System.err.println("エラーが発生しました: " + e);
            }
        }
    }
}