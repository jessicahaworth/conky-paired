package com.catdice.code;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class Pair {

    String name;
    String conkyLoc;
    String picLoc;

    public Pair(String n, String cloc, String ploc) {
        name = n;
        conkyLoc = cloc;
        picLoc = ploc;
    }

    // saves the conky file to the conky dir
    // saves the pic file to the pic dir
    // saves them as conky_name and name.jpg
    public boolean save() {
        String newConkyLoc = "scripts/conky_" + name;
        String newPicLoc = "pics/" + name + ".jpg";
        File origConkyFile = new File(conkyLoc);
        File origPicFile = new File(picLoc);
        File newConkyFile = new File(newConkyLoc);
        File newPicFile = new File(newPicLoc);

        try {
            FileUtils.copyFile(origConkyFile, newConkyFile);
            FileUtils.copyFile(origPicFile, newPicFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public String[] getEntries() {
        String[] entries = new String[3];
        entries[0] = name;
        entries[1] = conkyLoc;
        entries[2] = picLoc;
        return entries;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConkyLoc() {
        return conkyLoc;
    }

    public void setConkyLoc(String conkyLoc) {
        this.conkyLoc = conkyLoc;
    }

    public String getPicLoc() {
        return picLoc;
    }

    public void setPicLoc(String picLoc) {
        this.picLoc = picLoc;
    }

}
