/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package General;

import static General.Globals.MAIN_LOOP_DELAY;
//import static General.Globals.MAIN_OUTPUT_ARRAY;
import static General.Globals.VERBOSE_MODE;
//import static General.TrackInstaller.activateTrack;
import static MFUtils.CodeConverter.encodeDec;
import RFIDClasses.GlobalRFIDScannerMap;
import RFIDClasses.TagLocation;
import autoinstaller.InstallationRecord;
//import databaseIO.DataRetrieval;
//import gen.SysVars;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author danrothman
 */
public class RFIDScannerReader implements Runnable {

    // ***********************************************************
    // this class processes the RFID scanner array of one shelf
    // ***********************************************************
    private String displayfixtureIDForUser;
    private String shelfIPAddress;           //contains motherboard IP Address
    private int port;
    private Integer shelfID;
    private String shelfIDForUser;
    private Integer startingRFIDScannerAddress;
    private Integer numberOfRFIDScanners;
    private Double RFIDScannerWidth;
    private Double RFIDScannerFirstPosition;
    private int timeOut = Globals.SOCKET_REQUEST_TIMEOUT;

    private GlobalRFIDScannerMap RFIDScannerMap = new GlobalRFIDScannerMap();  //new GlobalRFIDScannerMap();

    public void run() {
        try {
            //main(shelfIPAddress, port, startingScannerAddress, numberOfScannerAddresses, timeOut);
            main();

        } catch (Exception e) {
            System.out.println("An Exception Has Been Thrown");
            Logger.getLogger(RFIDScannerReader.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public RFIDScannerReader(JSONObject shelfObj) {
        // class constructor, has a shelf object as a parameter, shelf object includes ipaddress,
        // address of first scanner, and number of scanners
        //

        try {
            this.displayfixtureIDForUser = shelfObj.getString("displayfixtureIDForUser");
            this.shelfID = shelfObj.getInt("shelfID");
            this.shelfIPAddress = shelfObj.getString("IPAddress");
            this.shelfIDForUser = shelfObj.getString("shelfIDForUser");
            this.shelfID = shelfObj.getInt("shelfID");
            this.port = shelfObj.getInt("port");
            this.RFIDScannerWidth = shelfObj.getDouble("RFIDScannerWidth");
            this.startingRFIDScannerAddress = shelfObj.getInt("startingRFIDScannerAddress");
            this.numberOfRFIDScanners = shelfObj.getInt("numberOfRFIDScanners");
            this.RFIDScannerMap = Globals.ALL_SCANNERS_MAP.get(shelfID);

        } catch (JSONException ex) {
            Logger.getLogger(RFIDScannerReader.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private String processRFIDScanner(String shelfIPAddress, int port, int RFIDScannerAddress, int timeOut) throws JSONException {
        String RFIDTagSector = "";

        RFIDTagSector = socketIO.SocketRetrieval.
                getRFIDScannerSector(shelfIPAddress, port, RFIDScannerAddress, timeOut);

        return RFIDTagSector;

    }

    private void main() throws JSONException {

        Integer processNumber = ++Globals.process_number;
        Integer endAddress = startingRFIDScannerAddress + numberOfRFIDScanners;
        ArrayList<String> RFIDTagIDs = new ArrayList<>();

        //build the global hashmap for scanner data
        //
        // effectively this is the main loop of the app, run in parallel threads for each shelf
        this.RFIDScannerMap = Globals.ALL_SCANNERS_MAP.get(shelfID);
        //System.out.println("------- in main loop ------");
        for (int i = startingRFIDScannerAddress; i < endAddress; i++) {
            if (Globals.KILL_EXECUTION) {
                break;
            }
            int RFIDScannerAddr = i;
            String sectorData = "";
            sectorData = processRFIDScanner(shelfIPAddress, port, RFIDScannerAddr, timeOut);
            // if the first 2 bytes of the sector don't equal scanner address then throw this out
            //
            if (!encodeDec(RFIDScannerAddr, 16, 2).equalsIgnoreCase(sectorData.substring(0, 2))) {
                System.out.println("Error Reading Sector, expected scanner address " + RFIDScannerAddr);
                System.out.println("Processing aborted");
                //RFIDScannerAddr = decodeBaseN(sectorData.substring(0,2), 16);
                continue;
            }

            String RFIDTagID = RFIDScannerMap.getRFIDTagID(sectorData);
            if (RFIDTagID.equalsIgnoreCase("NONE") || RFIDTagID.equals("0000000000000000")) {
                if (VERBOSE_MODE) {
                    Globals.writeToMainDisplay("*** RFID SCANNER ON SHELF " + this.shelfID + " AT ADDRESS " + RFIDScannerAddr + " HAS NOT BEEN ACTIVED  - NO TRACK WAS DETECTED");
                }
                continue;
            }
            // If we're down to this point it means we have a good RFIDTag
            // add it to the hashmap, add the RFIDTagID to our array of RFIDTagIDs
            System.out.println("RFIDTagID " + RFIDTagID + " found at address " + RFIDScannerAddr);
            if (RFIDScannerMap.getSectorData(RFIDTagID) == null) {
                RFIDTagIDs.add(RFIDTagID);
            }

            // put an element onto the Global ScannerMap
            RFIDScannerMap.put(RFIDTagID, RFIDScannerAddr, sectorData);

            // delay for some time interval
            try {
                Thread.sleep(MAIN_LOOP_DELAY);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }

        }
        // process all discovered RFIDTagIDs
        for (int i = 0; i < RFIDTagIDs.size(); i++) {
            if (Globals.KILL_EXECUTION) {
                break;
            }

            String sectorData = RFIDScannerMap.getSectorData(RFIDTagIDs.get(i));
            String RFIDTagID = RFIDTagIDs.get(i);

            ////////////////
            TagLocation tl = RFIDScannerMap.getRFIDTagLocation(RFIDTagID,
                    startingRFIDScannerAddress, RFIDScannerWidth,
                    RFIDScannerFirstPosition);
            tl.setGondola(displayfixtureIDForUser);
            tl.setShelf(shelfIDForUser);
            Double locationInInches = Double.valueOf(tl.getInchesFromLeft());

            Integer rs485Address = RFIDScannerMap.getrs485Address(sectorData);
            Integer devicetype = RFIDScannerMap.getDeviceType(sectorData);

            Globals.TAG_LOCATIONS.add(tl);
            Globals.writeToMainDisplay("Track ID : " + RFIDTagID + " Detected by Scanner @ IPADDR:" + shelfIPAddress); // + " :: Facing #" + facingShelfRelativeAddress + "@" + tl.getInchesFromLeft() + " inches from 'left'");

            // first try to register the track into the hardwareID table of the cloud database
            String outMess = "TrackID: " + RFIDTagID; // + " facingID : " + facingShelfRelativeAddress;
            if (TrackInstaller.registerHardware(RFIDTagID, devicetype, rs485Address)) {
                Globals.writeToMainDisplay(outMess + " was added to Hardware Table");
                Globals.refreshData();

            } else {
                // error in inserting record for track into the hardwareID table of the cloud database
                Globals.writeToMainDisplay(outMess + " Not Installed - could not register hardware to the database");
                continue;  // don't do the rest of the processing bc the installation will be missing key elements
            }

            // now activate the track by adding serial number (trackID), RS485 address initial values and other fields to the 
            // facing record in the facings table.
            JSONObject deviceTypeObject = Globals.DEVICETYPE_MAP.get(devicetype);

            try {
                int nSensors = deviceTypeObject.getInt("numberOfSensors");
                int startingSensor = deviceTypeObject.getInt("startingSensor");
                System.out.println("");
                System.out.println("************nSensors=" + nSensors + "  :  " + " startingSensor=" + startingSensor);

                /* TrackInstaller.activateTrack(shelfIPAddress, port, RFIDTagID, rs485Address, nSensors,
                            startingSensor, facingID, "00-00-00", devicetype);*/
                // String displayfixtureIDForuser = "dfidfuxldie43";
                String manufactureDate = "00-00-00";
                Globals.INSTALLABLE_TRACKS.add(new InstallationRecord(displayfixtureIDForUser,shelfID,
                        shelfIDForUser,
                        shelfIPAddress,
                        //facingID,
                        0,
                        //facingShelfRelativeAddress,
                        0,
                        port,
                        RFIDTagID,
                        rs485Address,
                        nSensors,
                        startingSensor,
                        manufactureDate,
                        devicetype,
                        //productName,
                        "prodname",
                        locationInInches,
                        "New Installation"));

            } catch (Exception e) {
                System.out.println("Error at " + e.getMessage());
            }

        }

        //now set things back to NATURAL sort
        InstallationRecord.sortInstallationRecordList(Globals.INSTALLABLE_TRACKS, "NATURAL");
    }

}
