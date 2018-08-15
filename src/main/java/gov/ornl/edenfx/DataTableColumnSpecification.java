package gov.ornl.edenfx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.TreeMap;

public class DataTableColumnSpecification {
    private StringProperty name;
    private StringProperty type;
    private StringProperty dateTimeFormatterID;
    private BooleanProperty ignore;

    private TreeMap<String, DateTimeFormatter> dtFormatterMap;
    TreeMap<String, String> dtParsePatternsExamples;

    public DataTableColumnSpecification(String name, String type, String dateTimeFormatterID, boolean ignore) {
        setName(name);
        setType(type);
        setDateTimeFormatterID(dateTimeFormatterID);
        setIgnore(ignore);

        dtFormatterMap = new TreeMap<>();
        dtFormatterMap.put("BASIC_ISO_DATE", DateTimeFormatter.BASIC_ISO_DATE);
        dtFormatterMap.put("ISO_DATE", DateTimeFormatter.ISO_DATE);
        dtFormatterMap.put("ISO_DATE_TIME", DateTimeFormatter.ISO_DATE_TIME);
        dtFormatterMap.put("ISO_INSTANT", DateTimeFormatter.ISO_INSTANT);
        dtFormatterMap.put("ISO_LOCAL_DATE", DateTimeFormatter.ISO_LOCAL_DATE);
        dtFormatterMap.put("ISO_LOCAL_DATE_TIME", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        dtFormatterMap.put("ISO_LOCAL_TIME", DateTimeFormatter.ISO_LOCAL_TIME);
        dtFormatterMap.put("ISO_OFFSET_DATE", DateTimeFormatter.ISO_OFFSET_DATE);
        dtFormatterMap.put("ISO_OFFSET_DATE_TIME", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        dtFormatterMap.put("ISO_OFFSET_TIME", DateTimeFormatter.ISO_OFFSET_TIME);
        dtFormatterMap.put("ISO_ORDINAL_DATE", DateTimeFormatter.ISO_ORDINAL_DATE);
        dtFormatterMap.put("ISO_TIME", DateTimeFormatter.ISO_TIME);
        dtFormatterMap.put("ISO_WEEK_DATE", DateTimeFormatter.ISO_WEEK_DATE);
        dtFormatterMap.put("ISO_ZONED_DATE_TIME", DateTimeFormatter.ISO_ZONED_DATE_TIME);
        dtFormatterMap.put("RFC_1123_DATE_TIME", DateTimeFormatter.RFC_1123_DATE_TIME);

        dtParsePatternsExamples = new TreeMap<>();
        dtParsePatternsExamples.put("BASIC_ISO_DATE", "'20111203'");
        dtParsePatternsExamples.put("ISO_DATE", "'2011-12-03' or '2011-12-03+01:00'");
        dtParsePatternsExamples.put("ISO_DATE_TIME", "'2011-12-03T10:15:30', '2011-12-03T10:15:30+01:00' or '2011-12-03T10:15:30+01:00[Europe/Paris]'");
        dtParsePatternsExamples.put("ISO_INSTANT", "'2011-12-03T10:15:30Z'");
        dtParsePatternsExamples.put("ISO_LOCAL_DATE", "'2011-12-03'");
        dtParsePatternsExamples.put("ISO_LOCAL_DATE_TIME", "'2011-12-03T10:15:30'");
        dtParsePatternsExamples.put("ISO_LOCAL_TIME", "'10:15' or '10:15:30'");
        dtParsePatternsExamples.put("ISO_OFFSET_DATE", "'2011-12-03+01:00'");
        dtParsePatternsExamples.put("ISO_OFFSET_DATE_TIME", "'2011-12-03T10:15:30+01:00'");
        dtParsePatternsExamples.put("ISO_OFFSET_TIME", "'10:15+01:00' or '10:15:30+01:00'");
        dtParsePatternsExamples.put("ISO_ORDINAL_DATE", "'2012-337'");
        dtParsePatternsExamples.put("ISO_TIME", "'10:15', '10:15:30' or '10:15:30+01:00'");
        dtParsePatternsExamples.put("ISO_WEEK_DATE", "'2012-W48-6'");
        dtParsePatternsExamples.put("ISO_ZONED_DATE_TIME", "'2011-12-03T10:15:30+01:00[Europe/Paris]'");
        dtParsePatternsExamples.put("RFC_1123_DATE_TIME", "'Tue, 3 Jun 2008 11:05:30 GMT'");
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
            type = new SimpleStringProperty("Double");
        }
        return type;
    }

    public String getType() {
        return typeProperty().get();
    }

    public void setType(String type) {
        typeProperty().set(type);
    }

    public StringProperty dateTimeFormatterIDProperty() {
        if (dateTimeFormatterID == null) {
            dateTimeFormatterID = new SimpleStringProperty("");
        }
        return dateTimeFormatterID;
    }

    public Set<String> getDateTimeFormatterIDs() { return dtFormatterMap.keySet(); }

    public DateTimeFormatter getDateTimeFormatter() {
        if (!getDateTimeFormatterID().isEmpty()) {
            return dtFormatterMap.get(getDateTimeFormatterID());
        }
        return dtFormatterMap.get("ISO_INSTANT");
    }

    public String getDateTimeFormatterID() {
        return dateTimeFormatterIDProperty().get();
    }

    public void setDateTimeFormatterID(String ID) {
        dateTimeFormatterIDProperty().set(ID);
    }

    public String getDateTimeFormatterExample(String ID) {
        return dtParsePatternsExamples.get(ID);
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
