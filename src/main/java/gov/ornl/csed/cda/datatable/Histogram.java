package gov.ornl.csed.cda.datatable;

public abstract class Histogram {
    private String name;
    private int binCounts[];

    public Histogram(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
