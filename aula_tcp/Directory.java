package aula_tcp;

import java.io.IOException;

public class Directory {
    String name;

    Files[] files;
    Integer amountFiles;

    Directory[] directories;
    Directory parentDirectory;
    Integer amountDirectories;

    Directory(Directory parent, String name) {
        this.name = this.formatDirName(parent, name);
        this.parentDirectory = parent;

        this.amountFiles = 0;
        this.amountDirectories = 0;

        this.files = new Files[10];
        this.directories = new Directory[10];
    }

    public String pwd() {
        return this.name;
    }

    public Directory chdir(String name) throws IOException {
        this.checkIfNameIsValid(name);
        String dirname = this.extractDirName(name);

        return this.findDir(dirname);
    }

    public String getDirs() {
        return this.directories == null ? null : this.generateDirNames();
    }

    public String getFiles() {
        return this.files == null ? null : this.generateFileNames();
    }

    public Files touch(String name) throws IOException {
        this.checkIfNameIsValid(name);
        return this.createFile(name);
    }

    protected void checkIfNameIsValid(String name) throws IOException {
        if (name == null) {
            throw new IOException("Name not be null");
        }
    }

    public Directory mkdir(String name) throws IOException {
        if (name == null) {
            throw new IOException("Mkdir need a name");
        }

        return this.createDir(name);
    }

    protected Directory createDir(String name) {
        Directory dir = new Directory(this, name);
        this.directories[this.amountDirectories++] = dir;
        return dir;
    }

    protected String extractDirName(String literalName) {
        String[] name = literalName.split("/");
        return name[name.length - 1];
    }

    protected String generateDirNames() {
        int current = 0;
        String response = "";
        Directory d = this.directories[current++];

        while (d != null) {
            response += extractDirName(d.name) + "\n";
            d = this.directories[current++];
        }

        return response;
    }

    protected String generateFileNames() {
        String response = "";
        Integer current = 0;

        Files file = this.files[current++];

        while (file != null) {
            response += file.getName();
            file = this.files[current++];
        }

        return response;

    }

    protected Directory findDir(String currentDirName) {
        if (currentDirName.equals("..")) {
            return this.parentDirectory;
        }

        for (int i = 0; i < this.amountDirectories; i++) {
            String dirName = extractDirName(this.directories[i].name);

            if (dirName.equals(currentDirName)) {
                return this.directories[i];
            }
        }
        return null;
    }

    protected String formatDirName(Directory dirParent, String name) {
        if (dirParent != null) {
            return dirParent.name + "/" + name;
        }

        return name;
    }

    protected Files createFile(String name) {
        Files file = new Files(name);
        this.files[this.amountFiles++] = file;

        return file;
    }
}
