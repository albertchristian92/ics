/*
 * Copyright (c) 2016.
 *
 * DReflect and Minuku Libraries by Shriti Raj (shritir@umich.edu) and Neeraj Kumar(neerajk@uci.edu) is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * Based on a work at https://github.com/Shriti-UCI/Minuku-2.
 *
 *
 * You are free to (only if you meet the terms mentioned below) :
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 *
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package user_monitoring.nctu_hscc_ac.ocs_v1;

import android.content.Context;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;
import java.util.Date;

import labelingStudy.nctu.minuku.DBHelper.DBHelper;
import labelingStudy.nctu.minuku.dao.AccessibilityDataRecordDAO;
import labelingStudy.nctu.minuku.dao.ActivityRecognitionDataRecordDAO;
import labelingStudy.nctu.minuku.dao.AppUsageDataRecordDAO;
import labelingStudy.nctu.minuku.dao.BatteryDataRecordDAO;
import labelingStudy.nctu.minuku.dao.ConnectivityDataRecordDAO;
import labelingStudy.nctu.minuku.dao.LocationDataRecordDAO;
import labelingStudy.nctu.minuku.dao.RingerDataRecordDAO;
import labelingStudy.nctu.minuku.dao.SensorDataRecordDAO;
import labelingStudy.nctu.minuku.dao.TelephonyDataRecordDAO;
import labelingStudy.nctu.minuku.dao.TransportationModeDAO;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.manager.MinukuDAOManager;
import labelingStudy.nctu.minuku.manager.MinukuSituationManager;
import labelingStudy.nctu.minuku.model.DataRecord.AccessibilityDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.ActivityRecognitionDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.AppUsageDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.BatteryDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.ConnectivityDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.LocationDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.RingerDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.SensorDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.TelephonyDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.TransportationModeDataRecord;
import labelingStudy.nctu.minuku.model.UserSubmissionStats;
import labelingStudy.nctu.minuku.streamgenerator.AccessibilityStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.ActivityRecognitionStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.AppUsageStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.BatteryStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.ConnectivityStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.LocationStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.RingerStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.SensorStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.TelephonyStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.TransportationModeStreamGenerator;

/**
 * Created by neerajkumar on 8/28/16.
 */
public class InstanceManager {
    private static InstanceManager instance = null;
    private Context mApplicationContext = null;
    private static Context mContext = null;
    private static Intent mintent;
    private UserSubmissionStats mUserSubmissionStats = null;
    private static String LOG_TAG = "InstanceManager";

    private InstanceManager(Context applicationContext) {
        this.mApplicationContext = applicationContext;
        initialize();
    }

    public static InstanceManager getInstance(Context applicationContext) {
        if (instance == null) {
            instance = new InstanceManager(applicationContext);
        }
        return instance;
    }

    public static boolean isInitialized() {
        return instance != null;
    }

    private Context getApplicationContext() {
        return mApplicationContext;
    }

    public static void setContexttoActivityRecognitionservice(Context context) {
        mContext = context;
    }

    private void initialize() {
        // Add all initialization code here.
        // DAO initialization stuff

        DBHelper dBHelper = new DBHelper(getApplicationContext());

        MinukuDAOManager daoManager = MinukuDAOManager.getInstance();

        //build new DAO here.
        LocationDataRecordDAO locationDataRecordDAO = new LocationDataRecordDAO(getApplicationContext());
        daoManager.registerDaoFor(LocationDataRecord.class, locationDataRecordDAO);

        ActivityRecognitionDataRecordDAO activityRecognitionDataRecordDAO = new ActivityRecognitionDataRecordDAO(getApplicationContext());
        daoManager.registerDaoFor(ActivityRecognitionDataRecord.class, activityRecognitionDataRecordDAO);

        TransportationModeDAO transportationModeDAO = new TransportationModeDAO(getApplicationContext());
        daoManager.registerDaoFor(TransportationModeDataRecord.class, transportationModeDAO);

        ConnectivityDataRecordDAO connectivityDataRecordDAO = new ConnectivityDataRecordDAO(getApplicationContext());
        daoManager.registerDaoFor(ConnectivityDataRecord.class, connectivityDataRecordDAO);

        BatteryDataRecordDAO batteryDataRecordDAO = new BatteryDataRecordDAO(getApplicationContext());
        daoManager.registerDaoFor(BatteryDataRecord.class, batteryDataRecordDAO);

        RingerDataRecordDAO ringerDataRecordDAO = new RingerDataRecordDAO(getApplicationContext());
        daoManager.registerDaoFor(RingerDataRecord.class, ringerDataRecordDAO);

        AppUsageDataRecordDAO appUsageDataRecordDAO = new AppUsageDataRecordDAO(getApplicationContext());
        daoManager.registerDaoFor(AppUsageDataRecord.class, appUsageDataRecordDAO);

        TelephonyDataRecordDAO telephonyDataRecordDAO = new TelephonyDataRecordDAO(getApplicationContext());
        daoManager.registerDaoFor(TelephonyDataRecord.class, telephonyDataRecordDAO);

        AccessibilityDataRecordDAO accessibilityDataRecordDAO = new AccessibilityDataRecordDAO(getApplicationContext());
        daoManager.registerDaoFor(AccessibilityDataRecord.class, accessibilityDataRecordDAO);

        SensorDataRecordDAO sensorDataRecordDAO = new SensorDataRecordDAO(getApplicationContext());
        daoManager.registerDaoFor(SensorDataRecord.class, sensorDataRecordDAO);


        // Create corresponding stream generators. Only to be created once in Main Activity
        // creating a new stream registers it with the stream manager

        //TODO build new StreamGenerator here.

        LocationStreamGenerator locationStreamGenerator =
                new LocationStreamGenerator(getApplicationContext());

        ActivityRecognitionStreamGenerator activityRecognitionStreamGenerator =
                new ActivityRecognitionStreamGenerator(getApplicationContext());

        TransportationModeStreamGenerator transportationModeStreamGenerator =
                new TransportationModeStreamGenerator(getApplicationContext());

        ConnectivityStreamGenerator connectivityStreamGenerator =
                new ConnectivityStreamGenerator(getApplicationContext());

        BatteryStreamGenerator batteryStreamGenerator =
                new BatteryStreamGenerator(getApplicationContext());

        RingerStreamGenerator ringerStreamGenerator =
                new RingerStreamGenerator(getApplicationContext());

        AppUsageStreamGenerator appUsageStreamGenerator =
                new AppUsageStreamGenerator(getApplicationContext());

        TelephonyStreamGenerator telephonyStreamGenerator =
                new TelephonyStreamGenerator(getApplicationContext());

        AccessibilityStreamGenerator accessibilityStreamGenerator =
                new AccessibilityStreamGenerator(getApplicationContext());

        SensorStreamGenerator sensorStreamGenerator =
                new SensorStreamGenerator(getApplicationContext());


        // All situations must be registered AFTER the stream generators are registers.
        MinukuSituationManager situationManager = MinukuSituationManager.getInstance();


        //TODO additional function
        //for testing to trigger qualtrics
        //QuestionnaireManager questionnaireManager = new QuestionnaireManager(getApplicationContext());

    }

    public UserSubmissionStats getUserSubmissionStats() {
            if((mUserSubmissionStats == null) || !areDatesEqual((new Date().getTime()), mUserSubmissionStats.getCreationTime())) {
                if(mUserSubmissionStats == null)
                    Log.d(LOG_TAG, "getUserSubmissionStats: userSubmissionStats is null");

                Log.d(LOG_TAG, "getUserSubmissionStats: userSubmissionStats is either null or we have a new date." +
                                "Creating new userSubmissionStats object");
            mUserSubmissionStats = new UserSubmissionStats();
        }
        return mUserSubmissionStats;
    }

    public synchronized void setUserSubmissionStats(UserSubmissionStats aUserSubmissionStats) {
        try {
            MinukuDAOManager.getInstance().getDaoFor(UserSubmissionStats.class).update(null,
                    aUserSubmissionStats);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Could not upload user stats via DAO.");
        }

        mUserSubmissionStats = aUserSubmissionStats;
        EventBus.getDefault().post(mUserSubmissionStats);
    }

    protected boolean areDatesEqual(long currentTime, long previousTime) {
        Log.d(LOG_TAG, "Checking if the both dates are the same");

        Calendar currentDate = Calendar.getInstance();
        Calendar previousDate = Calendar.getInstance();

        currentDate.setTimeInMillis(currentTime);
        previousDate.setTimeInMillis(previousTime);
        Log.d(LOG_TAG, "Current Year:" + currentDate.get(Calendar.YEAR) + " Previous Year:" + previousDate.get(Calendar.YEAR));
        Log.d(LOG_TAG, "Current Day:" + currentDate.get(Calendar.DAY_OF_YEAR) + " Previous Day:" + previousDate.get(Calendar.DAY_OF_YEAR));
        Log.d(LOG_TAG, "Current Month:" + currentDate.get(Calendar.MONTH) + " Previous Month:" + previousDate.get(Calendar.MONTH));

        boolean sameDay = (currentDate.get(Calendar.YEAR) == previousDate.get(Calendar.YEAR)) &&
                (currentDate.get(Calendar.DAY_OF_YEAR) == previousDate.get(Calendar.DAY_OF_YEAR)) &&
                (currentDate.get(Calendar.MONTH) == previousDate.get(Calendar.MONTH));

        if(sameDay)
            Log.d(LOG_TAG, "it is the same day, should not create a new object");
        else
            Log.d(LOG_TAG, "it is not the same day - a new day, should create a new object");
        return sameDay;
    }
}
