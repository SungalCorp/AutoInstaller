/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package General;

import static General.Globals.FACING_POSITIONS_MAP;
import autoinstaller.InstallationRecord;
import databaseIO.DataRetrieval;
import gen.SysVars;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import socketIO.SocketRetrieval;

/**
 *
 * @author danrothman
 */
public class TrackInstaller {
    public static void activateTrack(InstallationRecord ir){
        activateTrack(ir.getShelfIPAddress(),
                Integer.valueOf(ir.getPort()),
                ir.getRFIDTagID(),
                Integer.valueOf(ir.getRs485Address()),
                Integer.valueOf(ir.getnSensors()),
                Integer.valueOf(ir.getStartingSensor()),
                Integer.valueOf(ir.getFacingID()),
                ir.getManufactureDate(),
                ir.getDevicetype());
    }
    public static void deactivateFacing(int facingID){
        
                String APICall = SysVars.API_HOST_DOMAIN + "opDeactivateFacing?id=" + facingID;
                System.out.println("APICall = " + APICall);
                boolean insertSucessful = DataRetrieval.insertRecord(APICall);

                System.out.println((insertSucessful ? "did " : "did not ") + "deactivate facingID : " + facingID);
        
    }
    public static void activateTrack(String shelfIPAddress, int shelfPort, String trackID, int trackAddr, int nSensors,
            int startingSensor, int facingID, String mfDate, int deviceType) {
        JSONObject d = new JSONObject();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDateTime now = LocalDateTime.now();

        try {
            //this varies between physicalItems
            d.put("facingID", facingID);
            d.put("RS485Address", trackAddr + "");
            d.put("SN", trackID);
            d.put("mfdate", mfDate);
            d.put("activationDate", dtf.format(now));
            d.put("deviceTypeID", deviceType);
            d.put("reverseOrder", 0);

            System.out.println("d=" + d.toString());

            String initialVals = "";

            initialVals = SocketRetrieval.getPredefinedSensorReadings(startingSensor,nSensors,Globals.PRESET_INITIALVALUE);

            d.remove("reverseOrder");

            if (initialVals.equalsIgnoreCase("")) {
                System.out.println("NO CONNECTION UNABLE TO CONNECT TO TRACK @"
                        + "IP:" + shelfIPAddress
                        + "PORT:" + shelfPort
                        + "ADDRESS:" + trackAddr
                        + "ACTIVATION CANCELLED");
            } else {
                d.put("initialValues", initialVals);
                // adds fields to facing records for data related to a physical track such as serial number, RS485 address
                // track type etc.
       
                String APICall = SysVars.API_HOST_DOMAIN + "opActivate_facings?id=" + d.getInt("facingID") + "&fields="
                        + d.toString();
                System.out.println("APICall = " + APICall);
                boolean insertSucessful = DataRetrieval.insertRecord(APICall);

                System.out.println((insertSucessful ? "did " : "did not do") + "the activation");
            }
        } catch (JSONException ex) {
            Logger.getLogger(TrackInstaller.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static JSONObject getFacingForTrackFromTrackLocation(Integer shelfID, Double trackLocationMidpoint) {

        ArrayList<JSONObject> facings = FACING_POSITIONS_MAP.get(shelfID);

        //we have to find a facing whose width contains the midpoint 
        //for the track contained in trackLocationMidpoint
        JSONObject rVal = null;

        if (facings == null) {
            return rVal;
        }
        if (facings.size() > 0) {
            // facings are in order by relative address
            for (int i = 0; i < facings.size(); i++) {

                try {

                    JSONObject currentFacingObj = facings.get(i);

                    Double facingWidth;
                    if (currentFacingObj.get("productWidth") == null) {
                        facingWidth = currentFacingObj.getDouble("facingWidth");
                    } else {
                        facingWidth = currentFacingObj.getDouble("productWidth");
                    }

                    Double leftBoundDistance = currentFacingObj.getDouble("midpoint_inches_from_the_left") - facingWidth / 2.0;
                    Double rightBoundDistance = leftBoundDistance + facingWidth;

                    if (trackLocationMidpoint >= leftBoundDistance && trackLocationMidpoint <= rightBoundDistance) {
                        //return currentFacingObj.getInt("facingID");
                        return currentFacingObj;
                    }
                } catch (JSONException ex) {
                    Logger.getLogger(TrackInstaller.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

        }

        return rVal;
    }

    static boolean registerHardware(String RFIDTagID, Integer devicetype, Integer rs485Address) {
        try {
            JSONObject d = new JSONObject();
            // fill d with relevant fields
            // serialnumber,devicetype,address
            d.put("serialnumber", RFIDTagID);
            d.put("devicetype", devicetype);
            d.put("address", rs485Address);
            return insertHardwareIDRecord(d);
        } catch (JSONException ex) {
            return false;
        }
    }

   private static boolean insertHardwareIDRecord(JSONObject params) {
        //dbInsert?tablename=displayfixtures&fields={"storeID":1,"level":1,"displayfixtureIDForUser":"testertest","type":"gondola","location":"Detroit"}

        String mAPI = SysVars.API_HOST_DOMAIN + "dbInsert?tablename=hardwareids&fields=" + params.toString();

        return DataRetrieval.insertRecord(mAPI);
        //return true;
    }
}
