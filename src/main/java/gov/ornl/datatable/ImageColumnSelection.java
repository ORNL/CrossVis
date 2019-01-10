package gov.ornl.datatable;

import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.scene.image.Image;
import javafx.util.Pair;

import java.io.File;
import java.util.Set;

public class ImageColumnSelection extends ColumnSelection {
    private SetProperty<Pair<File, Image>> selectedImagePairSet;

    public ImageColumnSelection(Column column, Set<Pair<File, Image>> imagePairs) {
        super(column);
        selectedImagePairSet = new SimpleSetProperty<>(FXCollections.observableSet(imagePairs));
    }

    public SetProperty<Pair<File,Image>> selectedImagePairSetProperty() { return selectedImagePairSet; }

    public void setSelectedImagePairs (Set<Pair<File, Image>> imagePairs) {
        selectedImagePairSet.set(FXCollections.observableSet(imagePairs));
    }

    public Set<Pair<File,Image>> getSelectedImagePairs() { return selectedImagePairSet.get(); }

    public void removeImagePair(Pair<File,Image> imagePair) { selectedImagePairSet.remove(imagePair); }

    public void addImagePair(Pair<File, Image> imagePair) { selectedImagePairSet.add(imagePair); }
}
