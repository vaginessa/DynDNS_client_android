package my.com.ddns_client_android;


import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ResourceBundle;

public class ClientDDNS {

    private static ResourceBundle res = ResourceBundle.getBundle("settings");

    public static Socket socket = null;
    public static InputStream inputSocket;
    public static OutputStream outputSocket;

    public boolean isSocketGood = false;

    public ClientDDNS() throws Throwable {
        createSocket();
    }

    public Socket createSocket(){
        if (socket == null) {
            InetAddress serverAddress = null;
            Socket socket;
            try {
                // create connection
                serverAddress = InetAddress.getByName(res.getString("SOCKET.SERVER.ADDRESS"));
                socket = new Socket(serverAddress, Integer.parseInt(res.getString("SOCKET.SERVER.PORT")));

                isSocketGood = true;
                Helper.writeMessage("Connected to server successfully!");

                this.socket = socket;
                this.inputSocket = socket.getInputStream();
                this.outputSocket = socket.getOutputStream();

            } catch (Exception e) {
                Helper.writeMessage("Create Socket Exception");
                Helper.writeMessage(e.toString());
                isSocketGood = false;
            }
        }
        return getSocket();
    }

    public static void writeMessage(String message) throws Throwable {
        DataOutputStream out = new DataOutputStream(outputSocket);

        /* encode message */
        message = Helper.encodeString(message);
        /* encode message */

        out.writeUTF(message); //write message
        out.flush();
    }

    public static String readInput() throws Throwable {
        // Берем входной поток сокета, теперь можем получать данные от сервера.
        InputStream sin = socket.getInputStream();
        // Конвертируем поток в другой тип, чтоб легче обрабатывать текстовые сообщения.
        DataInputStream in = new DataInputStream(sin);
        // ожидаем пока сервер пришлет строку текста.
        String line = in.readUTF();

        /* decode message */
        line = Helper.decodeString(line);
        /* decode message */

        return line;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void reCreateSocket()
    {
        try {
            // here can be Exception "java.net.SocketException: Socket closed"
            getSocket().close();
            setSocket(null);
            createSocket();

        }catch (Exception e){
            setSocket(null);
            createSocket();
        }
    }

    public void setInputSocket() throws IOException {
        this.inputSocket = socket.getInputStream();
    }

    public void setOutputSocket() throws IOException {
        this.outputSocket = socket.getOutputStream();
    }
}
