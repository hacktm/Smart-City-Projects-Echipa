package ro.hacktm.cashacab;

import java.net.Socket;

public interface PrinterServerListener {
    public void onConnect(Socket socket);
}
