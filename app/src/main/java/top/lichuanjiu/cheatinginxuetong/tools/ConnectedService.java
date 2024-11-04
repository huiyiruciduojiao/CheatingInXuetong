package top.lichuanjiu.cheatinginxuetong.tools;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class ConnectedService {
    private final int PORT = 9102;
    public SSLSocket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
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
                        socket = createSSLSocket(ip, PORT);
                       Log.d("ConnectedService", "connect success");
                    }
                    inputStream = socket.getInputStream();
                    outputStream = socket.getOutputStream();
                    receive();

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
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }).start();
    }




    public void send(String data) {
        new Thread(() -> {
            try {
                if (socket == null) {
                    socket = createSSLSocket(ip, PORT);
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
                byte[] buffer = new byte[4096];
                int read = 0;
                try {
                    if (socket == null) {
                       socket = createSSLSocket(ip, PORT);
                    }
                    read = inputStream.read(buffer);
                    if (read == -1) {
                        break;
                    }
                    String data = new String(buffer, 0, read);

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
                        break;

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }).start();
    }
    private SSLSocket createSSLSocket(String ip, int port) {
        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslSocket = null;
        try {
            sslSocket = (SSLSocket) sslSocketFactory.createSocket(ip, port);
            sslSocket.setEnabledProtocols(new String[]{"TLSv1.2"});
            sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            inputStream = sslSocket.getInputStream();
            outputStream = sslSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sslSocket;
    }

    private void CreateSessionKey(int a, int b, int c) {

    }

}
