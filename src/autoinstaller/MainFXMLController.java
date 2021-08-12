/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autoinstaller;

import General.Globals;
import General.RFIDScannerReader;
import static General.Globals.*;
import General.TrackInstaller;
import RFIDClasses.TagLocation;
import gen.SysVars;
import gen.Utils;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author danrothman
 */
// controller for the main app screen. Contains properties and methods for overall user directed auto install process
public class MainFXMLController implements Initializable {

    @FXML
    private TextArea mainDisplay;

    @FXML
    private Label label1;

    @FXML
    private Label lblReady;

    double lblReady_height = 27;

    @FXML
    private Button startButton;

    @FXML
    private Button rescanButton;

    @FXML
    private Button quitButton;

    @FXML
    private TableView tblviewInstallations;

    @FXML
    private TableView tblviewDetectedTracks;

    @FXML
    private ImageView imgWaitIcon;

    @FXML
    private CheckBox chkSelectAll;
    
//   @FXML 
//    private WebView mWebview1;

    private final double waitIcon_height = 49;

//
//    @FXML
//    private TableView installationTableView;
    ScheduledThreadPoolExecutor mScheduledThreadPoolExecutor1 = new ScheduledThreadPoolExecutor(1);
    ScheduledThreadPoolExecutor mScheduledThreadPoolExecutor2 = new ScheduledThreadPoolExecutor(1);

//    private ObservableList<InstallationRecord> zoneData = FXCollections.observableArrayList(
//            new InstallationRecord()
//    );
    @FXML
    private void handleButtonAction(ActionEvent event) {
        
        String source = ((Node) event.getTarget()).getId();

        ////
        ///  QUIT 
        ///
        if (source.equalsIgnoreCase("quitButton")) {
            // get a handle to the stage
            Stage stage = (Stage) quitButton.getScene().getWindow();
            stage.close();
            System.exit(0);
            return;
        }
        ///
        /// DO ANOTHER SCAN
        ///
        if (source.equalsIgnoreCase("rescanButton")) {
            ///////////
            KILL_EXECUTION = false;
            Globals.ENABLE_AUTOINSTALL = false;

            Globals.writeToMainDisplay("Initalizing App...");

            // when this stage closes, the app is terminated
            Platform.setImplicitExit(true);

            //load the config values, and set dependent globals
            this.mScheduledThreadPoolExecutor2.scheduleAtFixedRate(new MainDisplayRefresher(this.mScheduledThreadPoolExecutor2),
                    0, 1000,
                    TimeUnit.MILLISECONDS);

            startScan();
            Globals.writeToMainDisplay("RESCANNING");
            return;
        }
        //
        // install the selected records
        //
        if (source.equalsIgnoreCase("startButton")) {
            doInstallation();
            return;
        }

        ///
        /// select all records (this doesn't reall belong here, should relocate bc it's a different control)
        ///
        if (source.equalsIgnoreCase("chkSelectAll")) {
            selectInstallationData();
        }

    }

    private void selectInstallationData() {
        for (InstallationRecord ir : Globals.INSTALLABLE_TRACKS) {
            ir.setDoInstall(this.chkSelectAll);
        }
        this.tblviewInstallations.refresh();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Globals.writeToMainDisplay("Initalizing App...");
      // mWebview1.getEngine().load("https://lpog-angular-app.ue.r.appspot.com/");
        // when this stage closes, the app is terminated
        Platform.setImplicitExit(true);

        //load the config values, and set dependent globals
        this.mScheduledThreadPoolExecutor2.scheduleAtFixedRate(new MainDisplayRefresher(this.mScheduledThreadPoolExecutor2),
                0, 1000,
                TimeUnit.MILLISECONDS);

        getVarValuesFromIni();
        setupDetectedTracksTableView();
        startScan();

    }

    private void startScan() {

        this.mScheduledThreadPoolExecutor1 = new ScheduledThreadPoolExecutor(1);

        Globals.MAIN_DISPLAY.clear();
        Globals.writeToMainDisplay("STARTING SCAN...");
        Globals.INSTALLABLE_TRACKS.clear();
        tblviewInstallations.setItems(null);
        Globals.TAG_LOCATIONS.clear();
        configureReadyWaitingState(false);

        if (startAutoInstaller()) {

            this.tblviewInstallations.refresh();
            Globals.writeToMainDisplay("Data retrieved for activated shelf, starting scan for uninstalled tracks ...");

            Globals.SCANNING_COMPLETE = false;
            Globals.ENABLE_AUTOINSTALL = false;
            this.startButton.setDisable(true);
            this.rescanButton.setDisable(true);

            if (Globals.first_scan) {
                this.mScheduledThreadPoolExecutor1.scheduleAtFixedRate(new ActivationChecker(this.mScheduledThreadPoolExecutor1), 0, 1000,
                        TimeUnit.MILLISECONDS);
                Globals.first_scan = false;
            }

        } else {
            Globals.writeToMainDisplay("No Database Connection...Process Is Halted");
        }
    }

    private void configureReadyWaitingState(boolean isReady) {
        this.lblReady.setVisible(isReady);
        this.lblReady.prefHeight(isReady ? this.lblReady_height : 0);
        this.imgWaitIcon.prefHeight(isReady ? 0 : this.waitIcon_height);
        this.imgWaitIcon.setVisible(!isReady);
        startButton.setDisable(!isReady);
        rescanButton.setDisable(!isReady);

    }

    private void getVarValuesFromIni() {
        //SysVars.API_HOST_DOMAIN = <<current domain for API>>;
        Globals.writeToMainDisplay("Reading Configuration File ...");
        //app variables are read from a text file which must reside in the same folder as the Jar file
        String configFile = "config.ini";
        HashMap<String, String> configVals = Utils.getIniVals(configFile);
        Globals.EXECUTION_MODE = configVals.get("EXECUTION_MODE");
        Globals.STORE_ID = Integer.parseInt(configVals.get("STORE_ID"));
        Globals.REFRESH_SHELFS_INTERVAL = Integer.parseInt(configVals.get("REFRESH_FACINGS_INTERVAL"));
        Globals.SENSOR_READING_INTERVAL = Integer.parseInt(configVals.get("SENSOR_READING_INTERVAL"));
        Globals.VERBOSE_MODE = Integer.parseInt(configVals.get("VERBOSE_MODE")) > 0 ? true : false;
        Globals.DISCOVERY_INTERVAL = Integer.parseInt(configVals.get("DISCOVERY_INTERVAL"));
        // do some processing now that we have ini values
        Globals.ACTIVE_SHELFS_API += Globals.STORE_ID;
        //Globals.API_HOST_DOMAIN = "http://localhost:8080/";
        Globals.ACTIVE_SHELFS = databaseIO.DataRetrieval.getRecords(Globals.API_HOST_DOMAIN + Globals.ACTIVE_SHELFS_API);

        
        
        
        Globals.writeToMainDisplay("****Refresh shelf interval:" + Globals.REFRESH_SHELFS_INTERVAL);
        Globals.writeToMainDisplay("****Scanner reading interval:" + Globals.SENSOR_READING_INTERVAL);
        System.out.println("***************************************************************");
        System.out.println("");
        System.out.println("");
    }

    private boolean startAutoInstaller() {

        boolean gotData = refreshData();
        for (int loopCount = 0; gotData == false && loopCount < MAX_DATA_REFRESH_ATTEMPTS; loopCount++) {
            gotData = refreshData();
        }
        if (!gotData) {
            return false;
        }
        try {

            // get out if there is nothing to do
            if (ACTIVE_SHELFS.length() == 0) {
                System.out.println("");
                System.out.println("No Active Shelves for this store");
                System.out.println("");
                //return false;
            } else {
                System.out.println("");
                System.out.println("*******Processing Shelves *****");
                for (int i = 0; i < ACTIVE_SHELFS.length(); i++) {
                    System.out.println("Shelf ID = " + ACTIVE_SHELFS.getJSONObject(i).getInt("shelfID"));
                }
            }

            resetExecutionThreads();

        } catch (Exception e) {
            System.out.println("An Exception Has Been Thrown");
            Logger.getLogger(RFIDScannerReader.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }

        return true;
    }

    private void setupDetectedTracksTableView() {
        String[] colNames = {"trackID", "gondola", "shelf", "facing", "scannersInComputation"};
        for (int i = 0; i < colNames.length; i++) {

            TableColumn<TagLocation, String> mTableColumn;

            mTableColumn
                    = ((TableColumn<TagLocation, String>) tblviewDetectedTracks.getColumns().get(i));

            mTableColumn.setCellValueFactory(new PropertyValueFactory<>(colNames[i]));

            mTableColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            mTableColumn.setEditable(false);
        }

        tblviewDetectedTracks.setItems(TAG_LOCATIONS);
        tblviewDetectedTracks.setEditable(false);

    }

    private void setupInstallationTableView() {
        //fill the zone table

        String[] colNames = {"doInstall", "displayfixtureIDForUser", "shelfIDForUser", "facingShelfRelativeAddress", 
                             "productName", "notes", "shelfIPAddress", "RFIDTagID"};

        // first column is a boolean select whether to install or not
        TableColumn<InstallationRecord, CheckBox> mTableColumn_0;

        mTableColumn_0
                = ((TableColumn<InstallationRecord, CheckBox>) tblviewInstallations.getColumns().get(0));

        mTableColumn_0.setCellValueFactory(new PropertyValueFactory<>(colNames[0]));

        mTableColumn_0.setOnEditCommit((CellEditEvent<InstallationRecord, CheckBox> e) -> {

            InstallationRecord irec = ((InstallationRecord) e.getTableView().getItems().get(e.getTablePosition().getRow()));

            int colNum = e.getTablePosition().getColumn();

            CheckBox newValue = e.getNewValue();
            System.out.println("IN EDIT COMITT");
            System.out.println("e.getOldValue = " + e.getOldValue());
            irec.setDoInstall(newValue);

        });

        for (int i = 1; i < colNames.length; i++) {

            TableColumn<InstallationRecord, String> mTableColumn;

            mTableColumn
                    = ((TableColumn<InstallationRecord, String>) tblviewInstallations.getColumns().get(i));

            mTableColumn.setCellValueFactory(new PropertyValueFactory<>(colNames[i]));

            mTableColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            mTableColumn.setEditable(false);
        }

        tblviewInstallations.setItems(INSTALLABLE_TRACKS);
        tblviewInstallations.setEditable(true);

    }

    private boolean isNumeric(String s) {
        try {
            Integer.parseInt(s);

        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private void doInstallation() {

        // loop through the selected installation records and install each one
        for (InstallationRecord ir : Globals.INSTALLABLE_TRACKS) {
            if (ir.getDoInstall().isSelected()) {
                Globals.writeToMainDisplay("Installing " + ir.getRFIDTagID());
                //deactivate whether it's a new track (nothing to deactivate) or a relocation.
                TrackInstaller.deactivateFacing(Integer.parseInt(ir.getFacingID()));
                TrackInstaller.activateTrack(ir);
                Globals.writeToMainDisplay(ir.getRFIDTagID() + " installed!");
            }

        }

    }

    private void getCenterTracksForFacings() {

        if (Globals.INSTALLABLE_TRACKS.isEmpty()) {
            return;
        }
        ArrayList<InstallationRecord> tempList = new ArrayList<>();
        String saveSortMode = Globals.INSTALLABLE_TRACKS.get(0).getSortMode();
        InstallationRecord.sortInstallationRecordList(Globals.INSTALLABLE_TRACKS, "POSITION");
        int currentFacingID = Integer.parseInt(Globals.INSTALLABLE_TRACKS.get(0).getFacingID());
        int currentSameIDCount = 0;
        int midpointIndex = 0;
        for (int i = 0; i < Globals.INSTALLABLE_TRACKS.size(); i++) {
            int facingID = Integer.parseInt(Globals.INSTALLABLE_TRACKS.get(i).getFacingID());
            if (facingID != currentFacingID) {
                midpointIndex = i - ((int) (currentSameIDCount / 2) + 1);
                tempList.add(Globals.INSTALLABLE_TRACKS.get(midpointIndex));
                currentFacingID = Integer.parseInt(Globals.INSTALLABLE_TRACKS.get(i).getFacingID());
                currentSameIDCount = 0;
            }
            currentSameIDCount++;
        }
        midpointIndex = Globals.INSTALLABLE_TRACKS.size() - ((int) (currentSameIDCount / 2) + 1);
        tempList.add(Globals.INSTALLABLE_TRACKS.get(midpointIndex));

        Globals.INSTALLABLE_TRACKS.clear();
        for (InstallationRecord ir : tempList) {
            Globals.INSTALLABLE_TRACKS.add(ir);
        }
        InstallationRecord.sortInstallationRecordList(Globals.INSTALLABLE_TRACKS, saveSortMode);

    }

    class ActivationChecker implements Runnable {

        // run is a abstract method that defines task performed at scheduled time.
        // run method executes once a second
        private final ScheduledThreadPoolExecutor mScheduledThreadPoolExecutor;

        public void run() {
            if (Globals.SCANNING_COMPLETE) {
                return;
            }
            configureReadyWaitingState(Globals.ENABLE_AUTOINSTALL);
            if (ENABLE_AUTOINSTALL) {
                Globals.SCANNING_PASS_NUMBER = 1;
                Globals.SCANNING_COMPLETE = true;
                System.out.println("POLLING SCANNERS COMPLETE");
                // 1. get all facings by order of facings in hashmap
                //    add facingID, facingRelativeAddress, and product to INSTALLABLE_TRACKS elements

                // 2. remove already installed facings from installable map
                // 3. separate not installed vs intalled in different facing in planogram
                // getCenterTracksForFacings();
                //sort by shelfID,locationfrom left
                processInstallationRecords();
                setupInstallationTableView();

                return;
            }
            boolean firstTime = Globals.SCANNING_PASS_NUMBER == 1;
            // Globals.writeToMainDisplay("**** POLLING RFID SCANNERS PASS " + Globals.SCANNING_PASS_NUMBER++, (firstTime ? "" : "SAMELINE"));
            System.out.println("POLLING SCANNERS");

        }

        private void processInstallationRecords() {
            InstallationRecord currentRec = Globals.INSTALLABLE_TRACKS.get(0);
            String sortModeSave = currentRec.getSortMode();
            InstallationRecord.sortInstallationRecordList(INSTALLABLE_TRACKS, "SHELFANDPOSITION");
            Integer currentShelfID = currentRec.getShelfID();
            Integer facingShelfRelativeAddress = 1;
            // ######################################################################################################
            //  PASS 1: get facingShelfRelativeAddress,facingID, product and populate the Installation Records
            // #####################################################################################################
            System.out.println("Installation Record Collection Length : " + Globals.INSTALLABLE_TRACKS.size());
            //int loopcounter = 0;
            ObservableList<InstallationRecord> INSTALLABLE_TRACKS_C = FXCollections.observableArrayList();

            for (InstallationRecord ir : Globals.INSTALLABLE_TRACKS) {
                if (!Objects.equals(currentShelfID, ir.getShelfID())) {
                    facingShelfRelativeAddress = 1;
                }
                ir.setFacingShelfRelativeAddress((facingShelfRelativeAddress) + "");
                // now that we have the facingrelative address, look it up in the hashmap 
                Integer relativeAddrKey = Globals.generateRelativeAddrKey(ir.getShelfID(), facingShelfRelativeAddress);
                // get the facing for this shelf and the facing relative address
                JSONObject facingObj = Globals.SHELF_RELATIVEADDRESS_FACING_MAP.get(relativeAddrKey);

                if (facingObj == null) {
                    // we have no such facing position for this shelf in the planogram, so ignore this track completely
                    continue;
                }
//                System.out.println("relative Addr Key : " + relativeAddrKey);

                try {

                    facingShelfRelativeAddress++;
                    String facingSN = facingObj.getString("facingSN");
                    if (facingSN.equalsIgnoreCase(ir.getRFIDTagID())) {
                        // facing is already installed, dont do the rest of this loop
                        continue;
                    }
                    if (!facingSN.equalsIgnoreCase("")) {
                        // we have an existing SN and it's different from location record's RFIDTagID
                        ir.setNotes("Relocation");
                    }
                    // get:
                    // 1. facingID
                    ir.setFacingID(facingObj.getInt("facingID") + "");
                    System.out.println("set FACING ID DONE! FACINGID : " + ir.getFacingID());
                    // 2. product
                    ir.setProductName(facingObj.getString("productName"));
                    System.out.println("set productName DONE! PRODUCTNAME : " + ir.getProductName());
                } catch (JSONException ex) {
                    System.out.println("EXCEPTION ENCOUNTERED @ MainFXMLController");
                    Logger.getLogger(MainFXMLController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    System.out.println("EXCEPTION ENCOUNTERED @ MainFXMLController for facing "
                            + ir.getFacingShelfRelativeAddress() + " on shelf " + ir.getShelfID());

                    Logger.getLogger(MainFXMLController.class.getName()).log(Level.SEVERE, null, ex);
                    continue;
                }

                INSTALLABLE_TRACKS_C.add(ir);
                currentShelfID = ir.getShelfID();
                //loopcounter++;

            }

            Globals.INSTALLABLE_TRACKS.clear();
            for (InstallationRecord ir : INSTALLABLE_TRACKS_C) {
                Globals.INSTALLABLE_TRACKS.add(ir);
            }

            //restore old sort mode
            InstallationRecord.sortInstallationRecordList(INSTALLABLE_TRACKS, sortModeSave);
        }

        public ActivationChecker(ScheduledThreadPoolExecutor s) {
            mScheduledThreadPoolExecutor = s;

        }

    }

    class MainDisplayRefresher implements Runnable {
        // run is a abstract method that defines task performed at scheduled time.

        private final ScheduledThreadPoolExecutor mScheduledThreadPoolExecutor;

        public void run() {
            if (!Globals.MAIN_DISPLAY_TEXT.getText().equals(mainDisplay.getText())) {
                mainDisplay.setText(Globals.MAIN_DISPLAY_TEXT.getText());
                mainDisplay.positionCaret(mainDisplay.getText().length());

            }

        }

        public MainDisplayRefresher(ScheduledThreadPoolExecutor s) {
            mScheduledThreadPoolExecutor = s;

        }

    }

}
