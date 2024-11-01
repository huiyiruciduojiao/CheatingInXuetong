package top.lichuanjiu.cheatinginxuetong.tools;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ConnectedService {
    private final int PORT = 9102;
    public Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private String sessionKey = null;
    private String user = null;
    private String pwd = null;
    private String ip = null;
    private int[] randomNumber = new int[3];

    public void setParameter(String user, String pwd) {
        this.user = user;
        this.pwd = pwd;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (socket == null || socket.isClosed()) {
                        Log.d("ConnectedService", "connect");
                        socket = new Socket();
                        socket.connect(new java.net.InetSocketAddress(ip, PORT), 5000);
                        Log.d("ConnectedService", "connect success");
                    }
                    inputStream = socket.getInputStream();
                    outputStream = socket.getOutputStream();
                    receive();
                    if (sessionKey == null) {
                        send(createHandshakeBag(ConnectType.HANDSHAKE_BAG_1));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        if (socket != null) {
                            socket.close();
                        }
                        if (outputStream != null) {
                            outputStream.close();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        outputStream = null;
                        inputStream = null;
                        socket = null;
                        sessionKey = null;
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * 创建握手包
     * @param type 握手包类型
     * @return 数据包
     */
    private String createHandshakeBag(String type) {

        Map<String, Object> dataMap = new HashMap<>();
        switch (type) {
            case ConnectType.HANDSHAKE_BAG_1:
                randomNumber[0] = (int) (Math.random() * Integer.MAX_VALUE);
                dataMap.put("RandomNumber", randomNumber[0]);
                break;
            case "B":
                randomNumber[1] = (int) (Math.random() * Integer.MAX_VALUE);
                dataMap.put("RandomNumber", randomNumber[1]);
                break;
            case "C":
                randomNumber[2] = (int) (Math.random() * Integer.MAX_VALUE);
                dataMap.put("RandomNumber", randomNumber[2]);
                break;
            case "D":
                break;
            default:
                return null;
        }
        dataMap.put("Type", type);
        return JSON.createJsonData(dataMap);
    }

    /**
     * 解析握手包
     * @param data 数据包
     * @return 是否是一个握手包
     */
    private boolean unHandshakeBag(String data) {
        Map<String, Object> dataMap = JSON.parseJsonData(data);
        Log.d("dataMap", dataMap.toString());
        String type = (String) dataMap.get("Type");

        switch (type) {
            case ConnectType.HANDSHAKE_BAG_2:
                String publicKey = (String) dataMap.get("PublicKey");
                Log.d("publicKey", publicKey);
                randomNumber[2] = (int) dataMap.get("RandomNumber");
                break;
            case ConnectType.HANDSHAKE_BAG_4:
                break;
            default:
                return false;
        }
        return true;


    }

    public void send(String data) {
        new Thread(() -> {
            try {
                if (socket == null) {
                    socket = new Socket(ip, PORT);
                }
                if (outputStream == null) {
                    outputStream = socket.getOutputStream();
                }
                outputStream.write((data).getBytes());
            } catch (Exception e) {
                try {
                    if (socket != null) {
                        socket.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    outputStream = null;
                    inputStream = null;
                    socket = null;
                    sessionKey = null;
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                e.printStackTrace();
            }
        }).start();
    }

    private void receive() {
        new Thread(() -> {
            while (true) {
                byte[] buffer = new byte[1024];
                int read = 0;
                try {
                    if (socket == null) {
                        socket = new Socket(ip, PORT);
                    }
                    if (inputStream == null) {
                        inputStream = socket.getInputStream();
                    }
                    read = inputStream.read(buffer);
                    if (read == -1) {
                        break;
                    }
                    String data = new String(buffer, 0, read);
                    if (!unHandshakeBag(data)) {
                        //正常数据传输
                    } else {
                        //
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        if (socket != null) {
                            socket.close();
                        }
                        if (outputStream != null) {
                            outputStream.close();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        outputStream = null;
                        inputStream = null;
                        socket = null;
                        sessionKey = null;
                        break;

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void CreateSessionKey(int a, int b, int c) {

    }

}
