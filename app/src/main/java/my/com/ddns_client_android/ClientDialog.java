package my.com.ddns_client_android;

import android.graphics.Color;

public class ClientDialog {

    static String lastIp = "";
    static String answer = "";
    
    // default color for text
    static int color = Color.BLACK;

    // mainActivity textIP/setText
    static LogoutActivity mainActivity;

    // check if thread run
    static boolean threadClientRun = false;

    /* main tasks of this method:
    1) start thread when button "Update" was pressed
    2) create only one thread, if button pressed again, drop current thread and create new one
    3) if domain and IP are correct:
        - check if domain has this IP?
                      - yes: do nothing, show message "Domain updated." green color
                      - no: need to update on server:
                            - create client socket;
                            - call Login for Authorization; if auth failed,
                            - send data to server and get answer;
                            - show answer into view;
     */
    public static void clientStartDialog() {

        if (!threadClientRun) {

            Thread threadStartDialog = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        threadClientRun = true;

                        while (threadClientRun) {

                            // check domain and IP are correct?
                            if (Helper.checkDomainName(LogoutActivity.domain) && Helper.checkIP(Helper.realIP)) {

                                try {

                                        lastIp = Helper.realIP;

                                        // if Helper.realIP not equals domain name
                                        if (!Helper.realIP.equals(Helper.getNSLookupName(LogoutActivity.domain))) {

                                            // create new socket
                                            ClientDDNS clientDDNS = null;
                                            try {
                                                clientDDNS = new ClientDDNS();
                                            } catch (Throwable throwable) {
                                                throwable.printStackTrace();
                                            }
                                            clientDDNS.reCreateSocket();

                                            // auth
                                            if (!Login.authenticate(LogoutActivity.username, LogoutActivity.password, clientDDNS))
                                            {
                                                // if auth NOT OK break!
                                                answer = "Authentication failed!";
                                                Helper.writeMessage(answer + "ClientDialog level! It cannot be! It needs to be investigated ASAP!");
                                                checkAnswer(answer);
                                                break;
                                            }

                                            // wait server
                                            Helper.writeMessage("Waiting for the server...");
                                            Thread.sleep(1000);

                                            // update DNS zone
                                            clientDDNS.writeMessage(lastIp + " : " + LogoutActivity.domain);
                                            Helper.writeMessage("Send new IP and domain: " + lastIp + " : " + LogoutActivity.domain);

                                            // wait answer from server
                                            answer = clientDDNS.readInput();
                                            Helper.writeMessage("ANSWER from server : " + answer);
                                            checkAnswer(answer);

                                            Thread.sleep(1000);

                                        } else if (lastIp.equals(Helper.getNSLookupName(LogoutActivity.domain))) {
                                            answer = "Zone updated.";
                                            Helper.writeMessage(answer);
                                            color = Color.GREEN;
                                            setAnswer();
                                            Thread.sleep(2000);

                                        } else {
                                            Helper.writeMessage("Do nothing...");
                                            // load last color and last answer
                                            setAnswer();
                                            Thread.sleep(1000);
                                    }

                                } catch (Throwable e) {
                                    Helper.writeMessage("General Exception");
                                    //LogoutActivity.firstConnection = false;
                                    Helper.writeMessage(e.toString());
                                }

                            } else {
                                answer = "Domain/IP-address is INCORRECT!";
                                Helper.writeMessage(answer);
                                checkAnswer(answer);
                                Thread.sleep(1000);
                            }

                        } // end main loop

                        // unlock method
                        threadClientRun = false;
                        lastIp = "";
                        answer = "";
                        Helper.writeMessage("thread ClientDialog stopped");


                    } catch (Exception e) {

                        // unlock method
                        threadClientRun = false;
                        lastIp = "";
                        answer = "";
                        Helper.writeMessage("thread ClientDialog is interrupted!");

                    }
                }
            });
            threadStartDialog.start();

        }else {
            Helper.writeMessage("thread ClientDialog already running! do nothing...");
            //Helper.writeMessage("thread ClientDialog already running! break it and re-start");
            //threadClientRun = false;
            //clientStartDialog();
        }
    }

    public static void setAnswer()
    {
        mainActivity.setText(answer);
        mainActivity.color = color;
    }

    public static void checkAnswer(String text){

        // if "Zone google.lt is NOT correct!"
        if (text.contains("is NOT correct")){
            answer = "You do not have right for this domain " + LogoutActivity.domain + "!";
            color = Color.RED;
            setAnswer();
            // stop thread
            threadClientRun = false;

        // if "Domain/IP-address is INCORRECT!"
        } else if (text.contains("Domain/IP-address is INCORRECT!")) {
            answer = "Domain/IP-address is INCORRECT!";
            color = Color.RED;
            setAnswer();
            // stop thread
            threadClientRun = false;

        // if auth failed
        } else if (text.contains("Authentication failed!")){
            answer = "Authentication failed!";
            color = Color.RED;
            setAnswer();
            // stop thread
            threadClientRun = false;

        } else {
            answer = text;
            color = Color.DKGRAY;
            setAnswer();
        }
    }

}
