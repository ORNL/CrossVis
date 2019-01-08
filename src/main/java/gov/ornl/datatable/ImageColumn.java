package gov.ornl.datatable;

import javafx.scene.image.Image;
import javafx.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ImageColumn extends Column {

    public ImageColumn(String name) {
        super(name);
    }

    @Override
    public boolean setFocusContext(Tuple tuple, int elementIdx) {
        getFocusTuples().add(tuple);
        return true;
    }

    @Override
    public void calculateStatistics() {

    }

    @Override
    public ColumnSummaryStats getStatistics() {
        return null;
    }

    public Pair<File, Image>[] getValues() {
        int columnIndex = getDataTable().getColumnIndex(this);

        Pair<File, Image>[] values = new Pair[getDataTable().getTupleCount()];
//        Image values[] = new Image[getDataTable().getTupleCount()];
        for (int i = 0; i < getDataTable().getTupleCount(); i++) {
            values[i] = (Pair<File, Image>)getDataTable().getTuple(i).getElement(columnIndex);
//            values[i] = (Image) getDataTable().getTuple(i).getElement(columnIndex);
        }

        return values;
    }

    public List<Pair<File, Image>> getValuesAsList() {
        int columnIndex = getDataTable().getColumnIndex(this);
        ArrayList<Pair<File,Image>> valuesList = new ArrayList<>();
        for (int i = 0; i < getDataTable().getTupleCount(); i++) {
            valuesList.add((Pair<File, Image>)getDataTable().getTuple(i).getElement(columnIndex));
//            valuesList.add((Image) getDataTable().getTuple(i).getElement(columnIndex));
        }
        return valuesList;
    }

    public Pair<File,Image>[] getQueriedValues() {
        int columnIndex = getDataTable().getColumnIndex(this);

        Set<Tuple> queriedTuples = getDataTable().getActiveQuery().getQueriedTuples();
        Pair<File,Image> values[] = new Pair[queriedTuples.size()];
//        Image values[] = new Image[queriedTuples.size()];

        int counter = 0;
        for (Tuple tuple : queriedTuples) {
            values[counter++] = (Pair<File, Image>)tuple.getElement(columnIndex);
//            values[counter++] = (Image)tuple.getElement(columnIndex);
        }

        return values;
    }

    public Pair<File,Image>[] getNonqueriedValues() {
        int columnIndex = getDataTable().getColumnIndex(this);

        Set<Tuple> nonqueriedTuples = getDataTable().getActiveQuery().getNonQueriedTuples();
        Pair<File,Image> values[] = new Pair[nonqueriedTuples.size()];

        int counter = 0;
        for (Tuple tuple : nonqueriedTuples) {
            values[counter++] = (Pair<File, Image>)tuple.getElement(columnIndex);
//            values[counter++] = (Image)tuple.getElement(columnIndex);
        }

        return values;
    }
}
