package src;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class Directory {

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

    public File mkdir(String parent, String dirName) {
        File newDir = new File(parent, dirName);
        if (!newDir.exists()) {
            boolean created = newDir.mkdirs();
            if (created) {
                System.out.println("Directory created successfully.");
            } else {
                System.out.println("Failed to create directory.");
                return null;
            }
        }
        return newDir;
    }

    public String getDirs(File[] files) {
        if (files == null)
            return "";

        String response = "";

        for (int i = 0; i < files.length; i++) {

            if (files[i].isDirectory()) {
                String name = files[i].getName();
                response += name + "\n";
            }
        }

        return response;
    }

    public String getFiles(File[] files) {
        if (files == null)
            return "";

        String response = "";

        for (int i = 0; i < files.length; i++) {

            if (files[i].isFile()) {
                String name = files[i].getName();
                response += name + "\n";
            }
        }

        return response;
    }

    private Path backDirectory(Path currentPath, String destinationPath, Path limitPath) {
        if (limitPath.equals(currentPath)) {
            return currentPath;
        }

        return currentPath.getParent();
    }

    private Path entryOnDirectory(Path currentPath, String destinationPath) {
        Path nextDir = currentPath.resolve(destinationPath);
        File homeFile = nextDir.toFile();

        if (homeFile.exists()) {
            currentPath = nextDir;
            return currentPath;
        } else {
            return null;
        }
    }

    public Path chdir(Path currentPath, String destinationPath, Path limitPath) {
        if (destinationPath.equals("..")) {
            return this.backDirectory(currentPath, destinationPath, limitPath);
        } else {
            return this.entryOnDirectory(currentPath, destinationPath);
        }
    }
}
