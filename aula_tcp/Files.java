package aula_tcp;

public class Files {
    public String name;
    public String ext;

    Files(String name) {
        String[] nameSplited = this.createFile(name);
        this.name = nameSplited[0];
        this.ext = nameSplited[1];
    }

    protected String[] createFile(String name) {
        return name.split("\\.");
    }

    public String getName() {
        return this.name + "." + this.ext;
    }
}
