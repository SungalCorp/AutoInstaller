/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package General;

import static General.Globals.KILL_EXECUTION;
import RFIDClasses.GlobalRFIDScannerMap;
import autoinstaller.InstallationRecord;
import autoinstaller.OnePropTestClass;
//import gen.SysVars;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextArea;
//import java.util.Hashtable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import static General.Globals.processArrayList;
import RFIDClasses.TagLocation;

/**
 *
 * @author danrothman
 */
public class Globals {

    //configurable from config.ini file
    public static String EXECUTION_MODE = "DEV";  // this is set in the config file
    public static Integer STORE_ID;

    //public static Integer CLIENT_ID;
    public static boolean ENABLE_AUTOINSTALL = false;
    public static boolean SCANNING_COMPLETE = false;
    public static ArrayList<String> MAIN_OUTPUT_ARRAY = new ArrayList<String>();

    public static ArrayList<String> MAIN_DISPLAY = new ArrayList<String>();
    public static TextArea MAIN_DISPLAY_TEXT = new TextArea();
    public static boolean KILL_EXECUTION = false;
    public static int DISCOVERY_INTERVAL = 100000;
//    public static String ACTIVE_SHELFS_API = SysVars.API_HOST_DOMAIN
//            + "dbGet_activatedshelfs_by_id?id=ALL"
//            + "&filter=storeID=";
    
    // default to local but we can have a different value in config.ini
    public static String API_HOST_DOMAIN = "http://localhost:8080/";

    public static String ACTIVE_SHELFS_API = "dbGet_activatedshelfs_by_id?id=ALL"
            + "&filter=storeID="; //+ "&filter=clienID";
//    public static String DEVICETYPES_API = SysVars.API_HOST_DOMAIN + "dbGet_devicetypes";
    public static String DEVICETYPES_API = "dbGet_devicetypes";
    
    
    

    // this will now be set after values are retrieved through the config.ini file
    public static JSONArray ACTIVE_SHELFS;
    public static JSONArray HARDWAREIDS;

//    public static String FACING_POSITIONS_API = SysVars.API_HOST_DOMAIN
//            + "dbGetFacingPositions?storeID=";  //provide storeID parameter to functions using FACING_POSITIONS_API
    public static String FACING_POSITIONS_API = "dbGetFacingPositions?storeID=";  //provide storeID parameter to functions using FACING_POSITIONS_API

    //
    //hash maps
    //********************************************************************
    
    public static HashMap<Integer, JSONObject>API_HOST_DOMAIN_MAP = new HashMap<>();
    
    static {
        API_HOST_DOMAIN_MAP.put()
    }
    
    public static HashMap<Integer, JSONObject> ACTIVE_SHELFS_MAP = new HashMap<>();
    static {
        ACTIVE_SHELFS_MAP.put()
    }

    public static HashMap<Integer, JSONObject> DEVICETYPE_MAP = new HashMap<>();

    public static HashMap<Integer, ArrayList<JSONObject>> FACING_POSITIONS_MAP = new HashMap<>();
    public static HashMap<String, JSONObject> SN_FACING_MAP = new HashMap<>();
    // find facings by shelfID and shelf relative address
    public static HashMap<Integer, JSONObject> SHELF_RELATIVEADDRESS_FACING_MAP = new HashMap<>();

    public static HashMap<String, JSONObject> HARDWAREIDS_MAP = new HashMap<>();
    public static HashMap<Integer, GlobalRFIDScannerMap> ALL_SCANNERS_MAP = new HashMap<>();  // each active shelfID has its own hashmap of RFID scanner readouts.

    //********************************************************************
    public static ArrayList<ScheduledFuture<?>> processArrayList = new ArrayList<>();
    // display matrix as retrieved from database
    public static JSONArray FACINGS;

//    public static ObservableList<InstallationRecord> INSTALLABLE_TRACKS = FXCollections.observableArrayList(new InstallationRecord("display", "shelf", "ipaddy", 10, 20, 30, "rfidtag", 50, 60, 70, "00", 80, "prodname","Dummy Record"));
    public static ObservableList<InstallationRecord> INSTALLABLE_TRACKS = FXCollections.observableArrayList();
    public static ObservableList<TagLocation> TAG_LOCATIONS = FXCollections.observableArrayList();
    public static ObservableList<OnePropTestClass> ONE_PROPDATA = FXCollections.observableArrayList(new OnePropTestClass("CB125OMG"));

    //set these through config ini file now
    public static int REFRESH_SHELFS_INTERVAL;
    public static int SENSOR_READING_INTERVAL;
    public static int PRESET_INITIALVALUE = 1000;
    public static int MAIN_LOOP_DELAY = 200;  //  millisecond delay per loop
    public static int MAX_DATA_REFRESH_ATTEMPTS = 5; //how many times we try to read data before giving up
    public static boolean VERBOSE_MODE = false;

    public static Integer process_number = 0;
    public static boolean first_scan = true;
    static int SOCKET_REQUEST_TIMEOUT = 3000;
    public static Integer SCANNING_PASS_NUMBER = 1;

    //public static GlobalRFIDScannerMap RFID_SCANNER_MAP = new GlobalRFIDScannerMap();
    private Globals() {
    }

    ///////////////////
    public static void resetExecutionThreads() {
        //kill all the threads
        // have one dummy record in the installations table so that we see data on the screen
        if (EXECUTION_MODE.equalsIgnoreCase("DEV")) {
            INSTALLABLE_TRACKS.add(new InstallationRecord());
        }

        if (VERBOSE_MODE) {
            System.out.println("***KILLING/REFRESHING PROCESS, ssArrayList.size = " + processArrayList.size());
        }
        System.out.println("We are about to kill " + processArrayList.size() + " processes in resetthreads method of static class Global;");
        killProcesses();

        processArrayList.clear();
        // create a parallel executing thread for each active shelf
        for (int i = 0; i < ACTIVE_SHELFS.length(); i++) {
            System.out.println("***In Active Shelfs loop, there are " + ACTIVE_SHELFS.length() + " active shelfs");

            try {
                System.out.println("------------- TRYING...");
                JSONObject f = ACTIVE_SHELFS.getJSONObject(i);
                ALL_SCANNERS_MAP.put(f.getInt("shelfID"), new GlobalRFIDScannerMap());
                RFIDScannerReader sr = new RFIDScannerReader(f);
                processArrayList.add(new ScheduledThreadPoolExecutor(1).scheduleAtFixedRate(sr, 0, SENSOR_READING_INTERVAL,
                        TimeUnit.MILLISECONDS));

                ALL_SCANNERS_MAP.put(f.getInt("shelfID"), new GlobalRFIDScannerMap());
                System.out.println("------------- TRY IS COMPLETE!!!...");

            } catch (JSONException ex) {
                Logger.getLogger(RFIDScannerReader.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(RFIDScannerReader.class.getName()).log(Level.SEVERE, null, ex);

            }

        }
        // now set the end of the scheduled for n seconds
        /////////////////////////////////////
        Timer timer = new Timer();
        timer.schedule(new ProcessKiller(), DISCOVERY_INTERVAL);

        ///////////////////////////////////
    }

    public static void writeToMainDisplay(String msg) {
        writeToMainDisplay(msg, true);
    }

    public static void writeToMainDisplay(String msg, Boolean toNextLine) {
        // MAIN_DISPLAY.setText(MAIN_DISPLAY.getText() + (toNextLine ? "\n" : "") + msg);
        if (toNextLine || MAIN_DISPLAY.isEmpty()) {
            MAIN_DISPLAY.add(msg);
        } else {
            Integer lastElem = MAIN_DISPLAY.size() - 1;
            MAIN_DISPLAY.set(lastElem, MAIN_DISPLAY.get(lastElem) + msg);
        }
        dumpMainDisplayToGlobalTextArea();
    }

    public static void writeToMainDisplay(String msg, String mode) {
        // this is an overloaded method to replace the last line of the Main Display
        // for now it doesn't matter what the mode is as long as it's not empty
        if (mode.equals("") || MAIN_DISPLAY.isEmpty()) {
            writeToMainDisplay(msg);
        } else {
            MAIN_DISPLAY.set(MAIN_DISPLAY.size() - 1, msg);
        }

        dumpMainDisplayToGlobalTextArea();
    }

    private static void dumpMainDisplayToGlobalTextArea() {
        Globals.MAIN_DISPLAY_TEXT.setText("");
        String textContent = "";
        for (String s : MAIN_DISPLAY) {
            textContent += (s + "\n");
        }
        Globals.MAIN_DISPLAY_TEXT.setText(textContent);
    }

    ////////////////////
    public static boolean refreshData() {
        // try to refresh shelves
        try {
            JSONArray j = databaseIO.DataRetrieval.getRecords(API_HOST_DOMAIN + ACTIVE_SHELFS_API);
            if (j.length() > 0) {
                System.out.println("There are active shelves...");
                ACTIVE_SHELFS = j;
                rebuildActiveShelfMap();
                //resetExecutionThreads();
            } else {
                System.out.println("There are no active shelves...");

            }
            // dump out the RFIDScanner map for s and g
            System.out.println("SHELF REFRESHED FROM CENTRAL DATABASE COMPLETE");

        } catch (Exception e) {
            //throw(e);
            System.out.println("Failed To Retreve Shelves from Database Records - " + e.getMessage());
            return false;
        }

        //try to refresh facing position and hardware SN maps
        try {
            JSONArray j = databaseIO.DataRetrieval.getRecords(API_HOST_DOMAIN + FACING_POSITIONS_API + STORE_ID);
            if (j.length() > 0) {
                rebuildFacingPositionMaps(j);
            }
            // dump out the RFIDScanner map for s and g
            System.out.println("FACINGPOSITION MAPS REFRESHED FROM CENTRAL DATABASE");
        } catch (Exception e) {
            //System.out.println("FAILED TO REFRESH FACING POSITION MAPS - " + e.getMessage());
            //throw (e);

            Logger.getLogger(ShelfRefresher.class.getName()).log(Level.SEVERE, null, e);
            return false;

        }
        // try to refresh devicetypemaps
        try {
            JSONArray j = databaseIO.DataRetrieval.getRecords(API_HOST_DOMAIN + DEVICETYPES_API);

            if (j.length() > 0) {
                DEVICETYPE_MAP.clear();
                for (int i = 0; i < j.length(); i++) {
                    JSONObject currentRec = j.getJSONObject(i);
                    DEVICETYPE_MAP.put(currentRec.getInt("deviceTypeID"), currentRec);
                }
            }

            // dump out the RFIDScanner map for s and g
            System.out.println("SHELF REFRESHED FROM CENTRAL DATABASE");

        } catch (Exception e) {
            //throw(e);
            System.out.println("Failed To Retreve Devicetypes from Database Records - ");
            Logger.getLogger(ShelfRefresher.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
        return true;
    }

    private static void rebuildActiveShelfMap() {
        ACTIVE_SHELFS_MAP.clear();
        for (int i = 0; i < ACTIVE_SHELFS.length(); i++) {
            try {
                ACTIVE_SHELFS_MAP.put(ACTIVE_SHELFS.getJSONObject(i).getInt("shelfID"), ACTIVE_SHELFS.getJSONObject(i));
            } catch (JSONException ex) {
                Logger.getLogger(ShelfRefresher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void rebuildHardwareidMap() {
        //rebuild from hardwareid table not facing positions
        HARDWAREIDS_MAP.clear();
        for (int i = 0; i < HARDWAREIDS.length(); i++) {
            try {
                HARDWAREIDS_MAP.put(HARDWAREIDS.getJSONObject(i).getString("serialnumber"), HARDWAREIDS.getJSONObject(i));
            } catch (JSONException ex) {
                Logger.getLogger(ShelfRefresher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void rebuildFacingPositionMaps(JSONArray facingPositionJSONARRAY) {
        //iterate through Facing Positions view, build map

        // notes on adding SHELF_RELATIVEADDRESS_FACING_MAP build
        //public static HashMap<Integer, JSONObject> SHELF_RELATIVEADDRESS_FACING_MAP = new HashMap<>();
        //facingShelfRelativeAddress
        SHELF_RELATIVEADDRESS_FACING_MAP.clear();
        FACING_POSITIONS_MAP.clear();
        SN_FACING_MAP.clear();

        Integer shelfID = -99;
        //facingPositionJSONARRAY is in store//shelf//facing relative-to-the-shelf-address
        for (int i = 0; i < facingPositionJSONARRAY.length(); i++) {
            try {
                String facingIDString = facingPositionJSONARRAY.getJSONObject(i).get("facingID").toString();
                //  if it has a facingID we are going to map it
                //
                JSONObject currentRecord = facingPositionJSONARRAY.getJSONObject(i);
                if (currentRecord.get("facingID") != null) {
                    try {
                        int facingID = Integer.valueOf(facingIDString);
                        int currentShelfID = currentRecord.getInt("shelfID");
                        if (currentShelfID != shelfID) {
                            FACING_POSITIONS_MAP.put(currentShelfID, new ArrayList<JSONObject>());
                            shelfID = currentShelfID;
                        }
                        FACING_POSITIONS_MAP.get(currentShelfID).add(currentRecord);

                        //now process SN/Facing map which matches track serial number to the planogram facing
                        String facingSN = facingPositionJSONARRAY.getJSONObject(i).getString("facingSN");
                        if (!facingSN.equals("")) {
                            SN_FACING_MAP.put(facingSN, facingPositionJSONARRAY.getJSONObject(i));
                        }
                        //populate Shelf_RELATIVEADDRESS_FACING Map
                        Integer relativeAddrKey = generateRelativeAddrKey(currentRecord.getInt("shelfID"), currentRecord.getInt("facingShelfRelativeAddress"));
                        SHELF_RELATIVEADDRESS_FACING_MAP.put(relativeAddrKey, currentRecord);
                    } catch (Exception e) {

                    }
                }

            } catch (JSONException ex) {
                Logger.getLogger(ShelfRefresher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public static Integer generateRelativeAddrKey(Integer shelfID, Integer facingShelfRelativeAddress) {
        return shelfID * 1000 + facingShelfRelativeAddress;
    }

    public static void killProcesses() {
        System.out.println("We are about to kill " + processArrayList.size() + " processes:");
        for (int i = 0; i < processArrayList.size(); i++) {
            processArrayList.get(i).cancel(true);
        }
        System.out.println(" Executed killProcesses");
    }
}

class ProcessKiller extends TimerTask {

    public void run() {
        Globals.killProcesses();
        KILL_EXECUTION = true;
        Globals.ENABLE_AUTOINSTALL = true;
        System.out.println(" Enabling User Interface");
        ////////////////////////////////////////
        // fill up Global outputlist.
        ///////////////////////////////////////

    }
}
