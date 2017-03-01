package my.com.ddns_client_android;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

    public static String PREFS_NAME="PREFERENCE_FILE_KEY_ddnd_client";
    public static String PREF_USERNAME="username";
    public static String PREF_PASSWORD="password";
    public static String PREF_DOMAIN="domain";
    // get permissions
    final static int REQUEST_CODE_ASK_PERMISSIONS = 1;

    // auth
    ClientDDNS clientDDNS;
    static boolean isAuth = false;
    private static int count; // failed counts
    static boolean StopAll = false;

    // check remember me for ClientDialog load
    static boolean rememberMe = false;

    // CREATE
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get security permissions
        showPhoneStatePermission();

        // resolve android.os.NetworkOnMainThreadException !
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // START
    public void onStart(){
        super.onStart();
        //read username and password from SharedPreferences
        getUser();
    }

    // auth block ////////////////////////////////////
    // button login pushed
    public void doLogin(View view) throws InterruptedException {
        // get views
        EditText txtuser=(EditText)findViewById(R.id.txt_user);
        EditText txtpwd=(EditText)findViewById(R.id.txt_pwd);
        //
        // create connection to server
        // start client socket
        try {
            clientDDNS = new ClientDDNS();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        // get data for auth
        String username=txtuser.getText().toString();
        String password=txtpwd.getText().toString();

        // don't send empty data
        if ((username.length() <= 0 ) || (password.length() <= 0 )){
            Toast.makeText(this, "Invalid username or password",Toast.LENGTH_LONG).show();
        } else {
            // send data to socket server
            if (!isAuth) {
                if (Login.authenticate(username, password, clientDDNS)) {
                    Helper.writeMessage("ACCESS GRANTED!");
                    isAuth = true;
                    count = 0; // clear counts
                } else {
                    Helper.writeMessage("ACCESS DENIED!");
                    count++;
                    // clear line
                    txtuser.setText("");
                    txtpwd.setText("");
                    // vibrocall
                    vibroCall();
                }
            }
            if (isAuth) {
                CheckBox ch = (CheckBox) findViewById(R.id.ch_rememberme);
                if (ch.isChecked())
                    rememberMe(username, password); //save username and password
                //show logout activity
                showLogout(username, password);

            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_LONG).show();
                if (count == 10) {
                    Helper.writeMessage("Close program!");
                    Toast.makeText(this, "DENIED!" + "\n" + "Please restart program and try again!",Toast.LENGTH_LONG).show();
                    Thread.sleep(1000);
                    StopAll = true;
                    finishAffinity();
                    System.exit(1);
                }
            }
        }
    }
    /////////////////////////////////////////////////

    // http://developer.alexanderklimov.ru/android/theory/vibrator.php
    public void vibroCall(){
        long mills = 1000L;
        Vibrator vibrator = (Vibrator) getSystemService(MainActivity.VIBRATOR_SERVICE);
        vibrator.vibrate(mills);
        vibrator.cancel();
    }

    public void getUser(){
        // get file, MODE_PRIVATE - access for only our appl
        SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        String username = pref.getString(PREF_USERNAME, null);
        String password = pref.getString(PREF_PASSWORD, null);

        if (username != null || password != null) {
            //directly show logout form
            rememberMe = true;
            showLogout(username, password);
        }
    }

    public void rememberMe(String user, String password){
        // save username and password in SharedPreferences
        // https://developer.android.com/training/basics/data-storage/shared-preferences.html?hl=ru
        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(PREF_USERNAME,user);
        editor.putString(PREF_PASSWORD,password);
        editor.putString(PREF_DOMAIN,"");
        editor.commit();
        rememberMe = true;
    }

    public void showLogout(String username, String password){
        //display log out activity
        Intent intent = new Intent(this, LogoutActivity.class);
        intent.putExtra("user",username);
        intent.putExtra("password",password);
        //intent.putExtra("domain",domain);
        startActivity(intent);
        // show dynamic messages
        Toast.makeText(this, "Login successfully!" + "\n" + "Welcome " + username, Toast.LENGTH_LONG).show();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    // security section
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void showPhoneStatePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {
                showExplanation("Permission Needed", "Rationale", Manifest.permission.READ_PHONE_STATE, REQUEST_CODE_ASK_PERMISSIONS);
            } else {
                requestPermission(Manifest.permission.READ_PHONE_STATE, REQUEST_CODE_ASK_PERMISSIONS);
            }
        } //else {
        //Toast.makeText(MainActivity.this, "Permission (already) Granted!", Toast.LENGTH_SHORT).show();
        //}
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void showExplanation(String title,
                                 String message,
                                 final String permission,
                                 final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
}