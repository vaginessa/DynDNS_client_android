package my.com.ddns_client_android;

import java.io.IOException;

public class Login {

    public static boolean authenticate(String username, String password, ClientDDNS clientDDNS) {

        boolean isAuth = false;

        try {
            String line;

            String passwordMD5 = Helper.md5(password);
            Helper.writeMessage("MD5 in hex: " + Helper.md5(password));

            Helper.writeMessage("Send auth data: " + username + " : " + passwordMD5);

            clientDDNS.writeMessage(username + " : " + passwordMD5);

            Helper.writeMessage("Wait response from auth server");

            line = clientDDNS.readInput(); // wait response from server

            if (line.equals("Authentication successfully!")) {
                isAuth = true;
                LogoutActivity.username = username;
                LogoutActivity.password = password;
            } else {
                Helper.writeMessage("Authentication failed!");
                clientDDNS.getSocket().close();
                clientDDNS.setSocket(null);
                clientDDNS.createSocket();
            }

        } catch (Throwable throwable) {
            Helper.writeMessage("Login class Exception");
            Helper.writeMessage(throwable.toString());
            try {
                clientDDNS.getSocket().close();
                clientDDNS.setSocket(null);
            } catch (IOException e) {
                Helper.writeMessage(e.toString());
            }

        }
        return isAuth;
    }
}
