/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autoinstaller;

import MFUtils.CodeConverter;
import java.util.Collections;
import java.util.List;
import javafx.scene.control.CheckBox;

/**
 *
 * @author danrothman
 *
 * This class represents a record that contains all values necessary to run a
 * hardware installation for a track.
 *
 *
 * Hardware installation includes: 1. Registering the track in the hardwareID
 * table in the central database 2. filling the facing record in the facing
 * table of the central database with data such as - Track Serial Number
 * (RFIDTagID) - comm. port (port) - RS485 Address - device type (model
 * determining number of sensors, spacing of sensors, length of track and other
 * datum) - initial sensor values upon installation (uncovered photo-resistor
 * calibration)
 *
 */
public class InstallationRecord implements Comparable<InstallationRecord> {

    private String displayfixtureIDForUser = "Gondola";
    private Integer shelfID = 0;
    private String shelfIDForUser = "Shelf";
    private String shelfIPAddress = "";
    private String facingID = "0";
    private String facingShelfRelativeAddress = "0";
    private String currentFacingID = "0";
    private String currentFacingShelfRelativeAddress = "0";
    private String port = "0";
    private String RFIDTagID = "RFIDTagID";
    private String rs485Address = "0";
    private String nSensors = "0";
    private String startingSensor = "0";
    private String manufactureDate = "00-00-00";
    private Integer devicetype = 0;
    private String productName = "productName";
    private String positionFromLeft = "0";

    private CheckBox doInstall = new CheckBox();
    private String notes = "Notes Go Here";

    private String sortMode = "NATURAL";

    public InstallationRecord() {
        //null constructor leave all properties at default values

    }

    public InstallationRecord(
            String displayfixtureIDForUser,
            Integer shelfID,
            String shelfIDForUser,
            String shelfIPAddress,
            Integer facingID,
            Integer facingShelfRelativeAddress,
            Integer port,
            String RFIDTagID,
            Integer rs485Address,
            Integer nSensors,
            Integer startingSensor,
            String manufactureDate,
            Integer devicetype,
            String productName,
            double positionFromLeft,
            String notes) {

        this.displayfixtureIDForUser = displayfixtureIDForUser;
        this.shelfID = shelfID;
        this.shelfIDForUser = shelfIDForUser;
        this.shelfIPAddress = shelfIPAddress;
        this.facingID = facingID + "";
        this.facingShelfRelativeAddress = facingShelfRelativeAddress + "";
        this.port = port + "";
        this.RFIDTagID = RFIDTagID;
        this.rs485Address = rs485Address + "";
        this.nSensors = nSensors + "";
        this.startingSensor = startingSensor + "";
        this.manufactureDate = manufactureDate;
        this.productName = productName;
        this.devicetype = devicetype;
        this.positionFromLeft = positionFromLeft + "";
        this.notes = notes;

        this.doInstall.setSelected(true);
    }

    //
    // getters
    //
    public String getSortMode() {
        return this.sortMode;
    }

    public void setSortMode(String sortMode) {
        this.sortMode = sortMode;
    }

    public String getDisplayfixtureIDForUser() {
        return this.displayfixtureIDForUser;
    }

    public String getShelfIDForUser() {
        return this.shelfIDForUser;
    }
    
    public Integer getShelfID(){
        return this.shelfID;
    }
    public String getFacingID() {
        return facingID;
    }

    public String getFacingShelfRelativeAddress() {
        String rVal = CodeConverter.addPadding(this.facingShelfRelativeAddress, 3, "0", "R");
        return rVal;
    }

    public String getCurrentFacingID() {
        return currentFacingID;
    }

    public String getCurrentFacingShelfRelativeAddress() {
        return CodeConverter.addPadding(this.currentFacingShelfRelativeAddress, 3, "0", "R");
    }

    public String getProductName() {
        return this.productName;
    }

    public String getRFIDTagID() {
        return this.RFIDTagID;
    }

    public String getShelfIPAddress() {
        return shelfIPAddress;
    }

    public String getPort() {
        return port;
    }

    public String getRs485Address() {
        return rs485Address;
    }

    public String getnSensors() {
        return nSensors;
    }

    public String getStartingSensor() {
        return startingSensor;
    }

    public String getManufactureDate() {
        return manufactureDate;
    }

    public Integer getDevicetype() {
        return devicetype;
    }

    public String getPositionFromLeft() {
        return this.positionFromLeft + "";
    }

    public String getNotes() {
        return notes;
    }

    public CheckBox getDoInstall() {
        return doInstall;
    }

    //
    //     setters
    //
    public void setDoInstall(CheckBox doInstall) {
        this.doInstall.setSelected(doInstall.isSelected());
    }

    public void setDisplayfixtureIDForUser(String displayfixtureIDForUser) {
        this.displayfixtureIDForUser = displayfixtureIDForUser;
    }

    public void setShelfIDForUser(String shelfIDForUser) {
        this.shelfIDForUser = shelfIDForUser;
    }
    
    public void setShelfID(Integer shelfID){
        this.shelfID = shelfID;
    }
    public void setShelfIPAddress(String shelfIPAddress) {
        this.shelfIPAddress = shelfIPAddress;
    }

    public void setFacingID(String facingID) {
        this.facingID = facingID;
    }

    public void setFacingShelfRelativeAddress(String facingShelfRelativeAddress) {
        this.facingShelfRelativeAddress = facingShelfRelativeAddress;
    }

    public void setCurrentFacingID(String currentFacingID) {
        this.currentFacingID = currentFacingID;
    }

    public void setCurrentFacingShelfRelativeAddress(String currentFacingShelfRelativeAddress) {
        this.currentFacingShelfRelativeAddress = currentFacingShelfRelativeAddress;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setRFIDTagID(String RFIDTagID) {
        this.RFIDTagID = RFIDTagID;
    }

    public void setRs485Address(String rs485Address) {
        this.rs485Address = rs485Address;
    }

    public void setnSensors(String nSensors) {
        this.nSensors = nSensors;
    }

    public void setStartingSensor(String startingSensor) {
        this.startingSensor = startingSensor;
    }

    public void setManufactureDate(String manufactureDate) {
        this.manufactureDate = manufactureDate;
    }

    public void setDevicetype(Integer devicetype) {
        this.devicetype = devicetype;
    }

    public void setPositionFromLeft(String positionFromLeft) {
        this.positionFromLeft = positionFromLeft;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public static void sortInstallationRecordList(List<InstallationRecord> oColl, String mode) {
        for (InstallationRecord ir : oColl) {
            ir.setSortMode(mode);
        }
        Collections.sort(oColl);
    }

    @Override
    public int compareTo(InstallationRecord o) {
        int rVal = 0;
        //sortMode determines how the compareTo sort is implemented
        if (this.sortMode.equalsIgnoreCase("NATURAL")) {
            //the "natural" sort order is Gondola + Shelf + facing (for users)
            String thisSortKey = createNaturalCompString(getDisplayfixtureIDForUser(), getShelfIDForUser(), getFacingShelfRelativeAddress());
            String thatSortKey = createNaturalCompString(o.getDisplayfixtureIDForUser(), o.getShelfIDForUser(), o.getFacingShelfRelativeAddress());
            return thisSortKey.compareTo(thatSortKey);
        }

        if (this.sortMode.equalsIgnoreCase("POSITION")) {
            //sort by facingID and position - 
            Double thisSortKey = Double.valueOf(this.facingID) * 1000 + Double.valueOf(this.positionFromLeft);
            Double thatSortKey = Double.valueOf(o.getFacingID()) * 1000 + Double.valueOf(o.getPositionFromLeft());
            return (int) (thisSortKey - thatSortKey);
        }

        if (this.sortMode.equalsIgnoreCase("SHELFANDPOSITION")) {
             Double thisSortKey = shelfID * 1000000 + Double.valueOf(this.positionFromLeft);
             Double thatSortKey = o.getShelfID() * 1000000 + Double.valueOf(o.getPositionFromLeft());
             return (int) (thisSortKey - thatSortKey);
        }

        return rVal;
    }

    private String createNaturalCompString(String displayFixture, String shelf, String facingRelativeAddress) {

        String displayFixture_1 = CodeConverter.addPadding(displayFixture, 50, " ");         //Utils.addPads(displayFixture, 50);
        String shelf_1 = CodeConverter.addPadding(shelf, 50, " ");
        String facingRelativeAddress_1 = CodeConverter.addPadding(facingRelativeAddress, 3, "0", "R");
        return displayFixture_1 + shelf_1 + facingRelativeAddress_1;
    }

    public String getSortKey() {
        return createNaturalCompString(getDisplayfixtureIDForUser(), getShelfIDForUser(), getFacingShelfRelativeAddress());
    }
}
