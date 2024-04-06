package aula_tcp;

public class Directory {
    String name;

    Directory[] directories;
    Directory parentDirectory;

    Files[] files;
    Integer amountFiles;
    Integer amountDirectories;

    Directory(Directory parent, String name) {
        this.name = name;
        this.parentDirectory = parent;

        this.files = new Files[10];
        this.directories = new Directory[10];
        this.amountFiles = 0;
        this.amountDirectories = 0;
    }

    public String pwd() {
        return this.name;
    }

    public Directory chdir(String name) {
        for (int i = 0; i < 10; i++) {
            if (directories[i].name.equals(name)) {
                return this.directories[i];
            }
        }

        return null;

    }

    public Directory[] getDirs() {
        return this.directories;
    }

    public Files[] getFiles() {
        return this.files;
    }

    public void mkdir() {

    }
}
