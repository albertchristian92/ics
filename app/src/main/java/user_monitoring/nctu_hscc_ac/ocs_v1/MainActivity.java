package user_monitoring.nctu_hscc_ac.ocs_v1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.manager.MinukuNotificationManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private Button btn_permission, btn_Timeline;
    private ToggleButton btn_connectServer;
    private SharedPreferences sharedPrefs;

    String message ="";
    private static String defaultIP = "140.113.86.142";
    private String defaultName = "test";
    private final int PORT = 5000;

    public static ClientSocket client;
    private boolean startRec = false;
    private boolean connectedAvailable = false;
    private boolean mLocationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_connectServer = (ToggleButton) findViewById(R.id.connectToServer);
        connectClient();
        btn_permission = (Button) findViewById(R.id.permissionRequest);
        btn_permission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startpermission();
            }
        });

        btn_Timeline = (Button) findViewById(R.id.placeTimeline);
        btn_Timeline.setOnClickListener(watchingMyTimeline);

        sharedPrefs = getSharedPreferences(Constants.sharedPrefString, MODE_PRIVATE);

        boolean firstStartBackGround = sharedPrefs.getBoolean("firstStartBackGround", true);

        if(firstStartBackGround) {

            startService(new Intent(getBaseContext(), BackgroundService.class));

            sharedPrefs.edit().putBoolean("firstStartBackGround", false).apply();
        }

        int sdk_int = Build.VERSION.SDK_INT;
        if(sdk_int>=23) {
            checkAndRequestPermissions();
        }

//        Intent intent = new Intent(getApplicationContext(), Timeline.class);
//        MinukuNotificationManager.setIntentToTimeline(intent);



    }

    private Button.OnClickListener watchingMyTimeline = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent intent = new Intent(MainActivity.this, Timeline.class);

            startActivity(intent);

        }
    };

    public void onResume(){
        super.onResume();
        btn_connectServer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    client.checkSocketConnection();
                    startRecording();
                } else {
                    endRecording();
                    connectClient();

                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        sharedPrefs.edit().putString("lastActivity", getClass().getName()).apply();
    }

    public void startpermission(){

        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));  // 協助工具

        Intent intent1 = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);  //usage
        startActivity(intent1);

//        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS); //notification
//        startActivity(intent);

        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));	//location
    }

    private void connectClient() {
        //Add Client socket to client
        setClient(new ClientSocket(defaultIP, PORT));
        //Set client connection as background execution
        getClient().execute();
        connectedAvailable =true;

    }

    private void setClient(ClientSocket client) {
        this.client = client;
    }

    private ClientSocket getClient() {
        return client;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    private void checkAndRequestPermissions() {

        Log.d(TAG,"checkingAndRequestingPermissions");

        int permissionReadExternalStorage = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionWriteExternalStorage = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        int permissionFineLocation = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCoarseLocation = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionStatus= ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE);

        List<String> listPermissionsNeeded = new ArrayList<>();


        if (permissionReadExternalStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (permissionWriteExternalStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (permissionFineLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (permissionCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.READ_PHONE_STATE);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            Log.d(TAG, "!listPermissionsNeeded.isEmpty() : "+!listPermissionsNeeded.isEmpty() );

            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
        }

    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    public void startServiceWork(){

        Log.d(TAG, "startServiceWork");

    }


    private void startRecording(){
        try {
            //initial name
            String nameTemp = ("test \n");
            client.sendDataString(nameTemp);
            System.out.println("name has been sent");
        } catch (Exception e) {
            e.printStackTrace();
        }

        startRec = true;
    }
    private void endRecording(){
        client.sendDataString("#end#");
        startRec = false;
        client.disconnect();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
