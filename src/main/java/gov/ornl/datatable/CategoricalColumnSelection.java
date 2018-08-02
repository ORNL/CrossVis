package gov.ornl.datatable;

import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;

import java.util.Set;

public class CategoricalColumnSelection extends ColumnSelection {
    private SetProperty<String> selectedCategories;

    public CategoricalColumnSelection(CategoricalColumn column, Set<String> categories) {
        super(column);
        selectedCategories = new SimpleSetProperty<>(FXCollections.observableSet(categories));
    }

    public SetProperty<String> selectedCategoriesProperty() {
        return selectedCategories;
    }

    public void setSelectedCategories (Set<String> categories) {
        selectedCategories.set(FXCollections.observableSet(categories));
    }

    public Set<String> getSelectedCategories() {
        return selectedCategories.get();
    }

    public void removeCategory(String category) {
        selectedCategories.remove(category);
    }

    public void addCategory(String category) {
        selectedCategories.add(category);
    }
}
