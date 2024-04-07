package aula_tcp.classes;

public class Files {
    public String name;
    public String ext;

    public Files(String name) {
        String[] nameSplited = this.splitName(name);
        this.name = nameSplited[0];
        this.ext = nameSplited[1];
    }

    protected String[] splitName(String name) {
        return name.split("\\.");
    }

    public String getName() {
        return this.name + "." + this.ext;
    }
}
