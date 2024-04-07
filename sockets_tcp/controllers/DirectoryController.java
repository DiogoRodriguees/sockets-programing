package sockets_tcp.controllers;

import java.io.File;
import java.io.IOException;

public class DirectoryController {

    public DirectoryController() {
    }

    public String touch(String dirName, String fileName) throws IOException {

        File file = new File(dirName, fileName);
        String errMsg = "File already exists.";
        String successMsg = "File created successfully.";

        try {

            return file.createNewFile() ? successMsg : errMsg;

        } catch (IOException e) {
            System.out.println("An error occurred while creating the file.");
            e.printStackTrace();

            return null;
        }
    }

    public void mkdir(String parent, String dirName) {
        File newDir = new File(parent, dirName);

        if (!newDir.exists()) {
            boolean created = newDir.mkdirs();
            if (created) {
                System.out.println("Directory created successfully.");
            } else {
                System.out.println("Failed to create directory.");
                return;
            }
        }

    }
    // public void mkdir(String parent, String dirName) {
    // File newDir = new File(parent, dirName);

    // if (!newDir.exists()) {
    // boolean created = newDir.mkdirs();
    // if (created) {
    // System.out.println("Directory created successfully.");
    // } else {
    // System.out.println("Failed to create directory.");
    // return;
    // }
    // }

    // }

}
