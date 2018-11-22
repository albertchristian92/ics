package user_monitoring.nctu_hscc_ac.ocs_v1;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;


public class StatsActivity extends AppCompatActivity {
    private static final int READ_PHONE_STATE_REQUEST = 37;
    public static final String EXTRA_PACKAGE = "ExtraPackage";

    AppCompatImageView ivIcon;
    Toolbar toolbar;

    TextView value_wifi;
    TextView value_mobile;
    TextView value_location;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        ivIcon = (AppCompatImageView) findViewById(R.id.avatar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestPermissions();
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    protected void onResume() {
        super.onResume();
        if (!hasPermissions()) {
            return;
        }
        initTextViews();
        try {
            checkIntent();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkIntent() throws RemoteException {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return;
        }
        String packageName = extras.getString(EXTRA_PACKAGE);
        if (packageName == null) {
            return;
        }
        try {
            ivIcon.setImageDrawable(getPackageManager().getApplicationIcon(packageName));
//            toolbar.setTitle(getPackageManager().getApplicationLabel(
//                    getPackageManager().getApplicationInfo(
//                            packageName, PackageManager.GET_META_DATA)));
//            toolbar.setSubtitle(packageName + ":" + PackageManagerHelper.getPackageUid(this, packageName));
  //          setSupportActionBar(toolbar);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (!PackageManagerHelper.isPackage(StatsActivity.this, packageName)) {
            return;
        }
        fillData(packageName);
    }

    private void requestPermissions() {
        if (!hasPermissionToReadNetworkHistory()) {
            return;
        }
        if (!hasPermissionToReadPhoneStats()) {
            requestPhoneStateStats();
        }
    }

    private boolean hasPermissions() {
        return hasPermissionToReadNetworkHistory() && hasPermissionToReadPhoneStats();
    }

    private void initTextViews() {
        value_wifi = (TextView) findViewById(R.id.value_wifi);
        value_mobile = (TextView) findViewById(R.id.value_mobile);
       // value_location = (TextView) findViewById(R.id.value_location);
    }

    private void fillData(String packageName) throws RemoteException {
        int uid = PackageManagerHelper.getPackageUid(this, packageName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkStatsManager networkStatsManager = (NetworkStatsManager) getApplicationContext().getSystemService(Context.NETWORK_STATS_SERVICE);
            NetworkStatsHelper networkStatsHelper = new NetworkStatsHelper(networkStatsManager, uid);
            fillNetworkStatsAll(networkStatsHelper);
            fillNetworkStatsPackage(uid, networkStatsHelper);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void fillNetworkStatsAll(NetworkStatsHelper networkStatsHelper) throws RemoteException {
        long mobileWifiRx = networkStatsHelper.getPackageRxBytesWifi();
        mobileWifiRx = mobileWifiRx/(1024*1024);
        long mobileWifiTx = networkStatsHelper.getPackageTxBytesWifi();
        mobileWifiTx = mobileWifiTx/(1024*1024);
        long WifiTotal = mobileWifiRx + mobileWifiTx;
        value_wifi.setText(WifiTotal + " MB Wifi");
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void fillNetworkStatsPackage(int uid, NetworkStatsHelper networkStatsHelper) throws RemoteException {
        long mobileWifiRx = networkStatsHelper.getPackageRxBytesMobile(this);
        mobileWifiRx = mobileWifiRx/(1024*1024);
        long mobileWifiTx = networkStatsHelper.getPackageTxBytesMobile(this);
        mobileWifiTx = mobileWifiTx/(1024*1024);
        long mobileTotal = mobileWifiRx+mobileWifiTx;
        value_mobile.setText(mobileTotal + " MB Mobile");
    }

    private boolean hasPermissionToReadPhoneStats() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
            return false;
        } else {
            return true;
        }
    }

    private void requestPhoneStateStats() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, READ_PHONE_STATE_REQUEST);
    }

    private boolean hasPermissionToReadNetworkHistory() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        final AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        if (mode == AppOpsManager.MODE_ALLOWED) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            appOps.startWatchingMode(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    getApplicationContext().getPackageName(),
                    new AppOpsManager.OnOpChangedListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onOpChanged(String op, String packageName) {
                            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                                    android.os.Process.myUid(), getPackageName());
                            if (mode != AppOpsManager.MODE_ALLOWED) {
                                return;
                            }
                            appOps.stopWatchingMode(this);
                            Intent intent = new Intent(StatsActivity.this, StatsActivity.class);
                            if (getIntent().getExtras() != null) {
                                intent.putExtras(getIntent().getExtras());
                            }
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            getApplicationContext().startActivity(intent);
                        }
                    });
        }
        requestReadNetworkHistoryAccess();
        return false;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void requestReadNetworkHistoryAccess() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }
}
