package aula_tcp;

public class Directory {
    String name;

    Directory[] directories;
    Directory parentDirectory;

    Files[] files;
    Integer amountFiles;
    Integer amountDirectories;

    Directory(Directory parent, String name) {
        if (parent != null) {
            this.name = parent.name + "/" + name;
        } else {
            this.name = name;
        }
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
        if (name == null)
            return null;

        String[] splited = name.split("/");
        System.out.println(splited[splited.length - 1]);

        if (splited[splited.length - 1].equals("..")) {
            System.out.println("BAck directory");
            return this.parentDirectory;
        }

        for (int i = 0; i < this.amountDirectories; i++) {
            String[] nameS = directories[i].name.split("/");
            if (nameS[nameS.length - 1].equals(splited[splited.length - 1])) {
                return this.directories[i];
            }
        }

        return null;
    }

    public String getDirs() {

        if (this.directories != null) {

            String response = "";
            int current = 0;
            Directory d = this.directories[current++];

            while (d != null) {
                String[] dName = d.name.split("/");
                response += dName[dName.length - 1] + "\n";
                d = directories[current++];
            }

            return response;
        }
        return "";
    }

    public String getFileNames() {
        if (this.files != null) {
            String response = "";
            Integer current = 0;

            Files file = files[current++];

            while (file != null) {
                response += file.name + "." + file.ext + "\n";
                file = files[current++];
            }

            return response;
        } else {
            return null;
        }
    }

    public String touch(String name) {
        if (name == null) {
            return "Name not be null";
        }

        String[] nameSplited = name.split("\\.");
        Integer free = this.amountFiles;

        this.files[free] = new Files(nameSplited[0], nameSplited[1]);
        this.amountFiles++;

        return "File " + name + " created";
    }

    public String mkdir(String name) {
        if (name == null) {
            return "Mkdir need a name";
        }
        Integer free = this.amountDirectories;

        this.directories[free] = new Directory(this, name);
        this.amountDirectories++;

        return "Diretorio " + name + " created";
    }
}
