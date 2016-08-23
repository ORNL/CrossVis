package gov.ornl.csed.cda.mustang;

/**
 * Created by csg on 8/12/16.
 */
public abstract class Column {
    private String name;
    private boolean enabled = true;

    public Column (String name) {
        this.name = name;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
