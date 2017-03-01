package my.com.ddns_client_android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import android.util.Base64;

public class Helper {

    private static ResourceBundle res = ResourceBundle.getBundle("settings");
    /* crypt */
    private static String encodeKey = res.getString("ENCODE.KEY");
    private static final String DEFAULT_ENCODING = "UTF-8";
    /* crypt */

    static String realIP = "";
    static String lastIP = "";
    // check if thread run
    static boolean threadSetIpRun = false;

    // mainActivity textIP/setText
    static LogoutActivity mainActivity;

    public static void writeMessage(String line)
    {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String message = dateFormat.format(date) + ": " + line;
        //System.out.println(message);
    }

    // get real IP
    public static void getRealIP() {
        try {
            URL whatismyip = new URL(res.getString("URL1"));
            BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
            realIP = in.readLine();
            writeMessage("Now IP is: " + realIP);
        }catch (Exception e){
            Helper.writeMessage("Method getRealIP Exception!");
            Helper.writeMessage(e.toString());
        }
    }

    // set real IP to text view
    /* main tasks of this method:
    1) get real IP
    2) set view mainActivity.setText of IP if Activity are running!
     */
    public static void setRealIP() {

        if (!threadSetIpRun) {

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        // lock this method
                        threadSetIpRun = true;

                        // main loop
                        while (threadSetIpRun) {

                            // get realIP
                            getRealIP();

                            // check that IP is not null
                            if (!realIP.equals("")) {

                                // if lastIP not equals realIP OR view does not have this IP (after pause), - then need to change "mainActivity.textIP"
                                if (!lastIP.equals(realIP) || (!mainActivity.textIP.contains(lastIP)) ) {

                                    writeMessage("IP was changed! Old IP: " + lastIP + "; New IP is: " + realIP);
                                    lastIP = realIP;

                                    // SET view ONLY IF ActivityStarted STARTED
                                    if (LogoutActivity.ActivityStarted) {
                                        // set new IP in view
                                        mainActivity.textIP = "Your IP-address: " + lastIP;
                                        // get last text and color from client dialog and send also for handler (only here handler will be applied!)
                                        mainActivity.setText(ClientDialog.answer);
                                        mainActivity.color = ClientDialog.color;

                                    } else {
                                        Helper.writeMessage("Activity not started! Waiting...");
                                    }
                                }
                            }

                            // wait before start the next loop
                            Thread.sleep(2000);

                        } // end main lop

                        Helper.writeMessage("thread setIP stopped");
                        // clear data
                        lastIP = "";
                        realIP = "";
                        // unlock method
                        threadSetIpRun = false;

                    } catch (InterruptedException e) {
                        // do nothing, just stop
                        // clear data
                        lastIP = "";
                        realIP = "";
                        // unlock method
                        threadSetIpRun = false;
                        writeMessage("thread setIP is interrupted!");

                    } catch (Exception e) {
                        // clear data
                        lastIP = "";
                        realIP = "";
                        // unlock method
                        threadSetIpRun = false;

                        writeMessage("EXCEPTION textView! RESTART THREAD");
                        writeMessage(e.toString());
                        // restart
                        run();
                    }
                }
            });
            thread.start();
        }else {
            Helper.writeMessage("thread setIP already running! Do nothing..");
        }
    }

    public static String md5(String input) throws NoSuchAlgorithmException {

        String md5 = null;
        if(null == input) return null;

        try {
            //Create MessageDigest object for MD5
            MessageDigest digest = MessageDigest.getInstance("MD5");
            //Update input string in message digest
            digest.update(input.getBytes(), 0, input.length());
            //Converts message digest value in base 16 (hex)
            md5 = new BigInteger(1, digest.digest()).toString(16);

        } catch (NoSuchAlgorithmException e) {

            e.printStackTrace();
        }
        return md5;
    }

    public static boolean checkDomainName(String domain){
        boolean result = false;
        //writeMessage("Cheking domain " + domain);
        if ( (domain.length() <= 0) || (domain.contains(",")) || (!domain.contains(".")) )
        {
            result = false;
        } else if (getNSLookupName(domain).equals("FALSE")){
            result = false;
        } else {
            result = true;
        }
        return result;
    }

    public static boolean checkIP(String ipAddressString) {
        String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
        return ipAddressString.matches(PATTERN);
    }

    public static String getNSLookupName(String domain){

        InetAddress address = null;
        try {
            address = InetAddress.getByName(domain);
        } catch (UnknownHostException e) {
            writeMessage(e.toString());
            return "FALSE";
        }
        return address.getHostAddress();
    }


    /* crypt */
    public static String base64encode(String text) {
        try {

            byte[] encodeValue = Base64.encode(text.getBytes(), Base64.DEFAULT);
            String result = new String(encodeValue, "UTF-8");
            return result;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }//base64encode

    public static String base64decode(String text) {
        try {
            byte[] decodeValue = Base64.decode(text, Base64.DEFAULT);
            String result = new String(decodeValue, "UTF-8");
            return result;
        } catch (IOException e) {
            return null;
        }
    }//base64decode
    public static String xorMessage(String message, String key) {
        try {
            if (message == null || key == null) return null;

            char[] keys = key.toCharArray();
            char[] mesg = message.toCharArray();

            int ml = mesg.length;
            int kl = keys.length;
            char[] newmsg = new char[ml];

            for (int i = 0; i < ml; i++) {
                newmsg[i] = (char)(mesg[i] ^ keys[i % kl]);
            }//for i

            return new String(newmsg);
        } catch (Exception e) {
            return null;
        }
    }//xorMessage

    public static String encodeString(String line) {
        //System.out.println("Line for encode: " + line);
        String line1 = Helper.xorMessage(line, encodeKey);
        String encodedLine = Helper.base64encode(line1);
        //System.out.println("Encoded line: " + encodedLine);
        return encodedLine;
    }
    public static String decodeString(String line){
        //System.out.println("Line for decode: " + line);
        String line1 = Helper.base64decode(line);
        String decodedLine = Helper.xorMessage(line1, encodeKey);
        //System.out.println("Decoded line: " + decodedLine);
        return decodedLine;
    }
    /* crypt */





}
