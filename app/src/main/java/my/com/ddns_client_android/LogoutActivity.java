package my.com.ddns_client_android;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LogoutActivity extends Activity {

    // vars //////////////////////////////
    // for using in fix block (FIX for setText into view) into this class
    private static TextView textViewAnswer;
    private static TextView textViewIP;

    // run or not this Activity
    static boolean ActivityStarted = true;

    // get keys for dataFile
    String fileName = MainActivity.PREFS_NAME;
    String fileUserName = MainActivity.PREF_USERNAME;
    String filePassword = MainActivity.PREF_PASSWORD;
    String fileDomain = MainActivity.PREF_DOMAIN;
    static String username;
    static String password;
    static String domain = null;

    // resolve error "Only the original thread that created a view hierarchy can touch its views"
    // http://www.seostella.com/ru/article/2012/02/14/android-oshibka-only-the-original-thread-that-created-a-view-hierarchy-can-touch-its-views.html
    private Handler handler;
    // color for text - answer from server
    static int color = Color.BLACK;
    // for set from Helper setIP
    static String textIP = "";

    //////////////////////////////////////


    // CREATE
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);

        Helper.writeMessage("level onCreate()");

        // check if stop all
        if (MainActivity.StopAll){
            finishAffinity();
            System.exit(1);
        }

        //get username sent from the log in activity
        Intent intent=getIntent();
        Bundle b=intent.getExtras();
        username=b.getString("user");
        password=b.getString("password");
        domain=b.getString("domain");


        // resolve android.os.NetworkOnMainThreadException !
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }


        // resolve error "Only the original thread that created a view hierarchy can touch its views"
        // http://www.seostella.com/ru/article/2012/02/14/android-oshibka-only-the-original-thread-that-created-a-view-hierarchy-can-touch-its-views.html

        // get id of views
        textViewIP = (TextView) findViewById(R.id.result_IP);
        textViewAnswer = (TextView) findViewById(R.id.message2);

        // FIX for setText into view
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String text = (String) msg.obj;
                // set IP to view
                textViewIP.setText( textIP );
                // set text to server answer - call from Helper setIP and ClientDialog
                textViewAnswer.setText( text );
                textViewAnswer.setTextColor(color);
            }
        };
        ////////////////////////////////////
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.logout, menu);
        return true;
    }


    // START
    public void onStart(){
        super.onStart();
        Helper.writeMessage("level onStart()");
    }


    // RESUME general work
    @Override
    protected void onResume(){
        super.onResume();

        Helper.writeMessage("level onResume()");
        ActivityStarted = true;

        // get phone number
        Helper.writeMessage("********************");
        Helper.writeMessage("Our phone number is: " + getMyPhoneNumber());
        Helper.writeMessage("********************");
        ///////////////////

        // set real IP - start Thread of setting
        Helper.writeMessage(" **** START getIP/setIP");
        Helper.mainActivity = this;
        Helper.setRealIP();

        // get id of editText
        final EditText textEdit = (EditText)findViewById(R.id.edit_message);

        // button update
        final Button button = (Button) findViewById(R.id.button_update);
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v)
            {
                String text = textEdit.getText().toString();
                updateIP(text);
            }
        });

        if (MainActivity.rememberMe) {
            loadDataFromFile();
            try {
                Thread.sleep(1000);
            } catch (Exception e) {}
            updateIP(domain);
        }

    }

    // button update
    private void updateIP(String text) {

        Helper.writeMessage("We got the text from button: " + text);
        domain = text;
        // send this object
        ClientDialog.mainActivity = this;
        // start thread ClientDialog
        ClientDialog.clientStartDialog();

        // save data to file
        if (MainActivity.rememberMe) {
            saveDataToFile();
        }
    }

    // setText into view (send text to hangler - fix for text view in Create() block)
    public void setText(String text){
        Message msg = new Message();
        msg.obj = text;
        handler.sendMessage(msg);
    /////////////////////////////////////
    }

    // get my phone number
    private String getMyPhoneNumber() {
        // get phone number
        TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return mTelephonyMgr.getLine1Number();
    }

    // get data from file
    public void loadDataFromFile(){
        Helper.writeMessage("Start method: loadDataFromFile()");
        SharedPreferences pref = getSharedPreferences(fileName,MODE_PRIVATE);
        username = pref.getString(fileUserName, null);
        password = pref.getString(filePassword, null);
        domain = pref.getString(fileDomain, null);

        // get id of editText - load saved domain
        final EditText textEdit = (EditText)findViewById(R.id.edit_message);
        textEdit.setText(domain);
    }

    // save data from file
    public void saveDataToFile(){
        Helper.writeMessage("Start method: saveDataToFile()");
        SharedPreferences sharedPref = getSharedPreferences(fileName,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();
        editor.putString(fileUserName,username);
        editor.putString(filePassword,password);
        editor.putString(fileDomain,domain);
        editor.commit();
    }


    // PAUSE
    @Override
    protected void onPause(){
        super.onPause();

        Helper.writeMessage("level onPause()");

        // uses by Helper
        ActivityStarted = false;
    }

    static void stopAllThreads()
    {
        Helper.threadSetIpRun = false;
        ClientDialog.threadClientRun = false;
    }

    // LOGOUT (button)
    public void logout(View view){
        // stop threads
        stopAllThreads();
        //
        SharedPreferences sharedPrefs =getSharedPreferences(MainActivity.PREFS_NAME,MODE_PRIVATE);
        Editor editor = sharedPrefs.edit();
        editor.clear();
        editor.commit();
        username="";
        password="";
        domain="";
        MainActivity.rememberMe = false;
        //show login form
        Intent intent=new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}



