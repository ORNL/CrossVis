package gov.ornl.pcpview;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ColumnSpecification {
    private StringProperty name;
    private StringProperty type;
    private StringProperty parsePattern;
    private BooleanProperty ignore;

    public ColumnSpecification (String name, String type, String timeParsePattern, boolean ignore) {
        setName(name);
        setType(type);
        setParsePattern(timeParsePattern);
        setIgnore(ignore);
    }

    public StringProperty nameProperty() {
        if (name == null) {
            name = new SimpleStringProperty();
        }
        return name;
    }

    public String getName() {
        return nameProperty().get();
    }

    public void setName(String name) {
        nameProperty().set(name);
    }

    public StringProperty typeProperty() {
        if (type == null) {
            type = new SimpleStringProperty("Number");
        }
        return type;
    }

    public String getType() {
        return typeProperty().get();
    }

    public void setType(String type) {
        typeProperty().set(type);
    }

    public StringProperty parsePatternProperty() {
        if (parsePattern == null) {
            parsePattern = new SimpleStringProperty("");
        }
        return parsePattern;
    }

    public String getParsePattern() {
        return parsePatternProperty().get();
    }

    public void setParsePattern(String parsePattern) {
        parsePatternProperty().set(parsePattern);
    }

    public BooleanProperty ignoreProperty() {
        if (ignore == null) {
            ignore = new SimpleBooleanProperty(false);
        }
        return ignore;
    }

    public boolean getIgnore() {
        return ignoreProperty().get();
    }

    public void setIgnore(boolean ignore) {
        ignoreProperty().set(ignore);
    }
}
