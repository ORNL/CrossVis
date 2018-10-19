package gov.ornl.datatableview;

import gov.ornl.datatable.CategoricalColumnSelection;
import javafx.scene.input.MouseEvent;

public class CategoricalAxisSelection extends UnivariateAxisSelection {

    public CategoricalAxisSelection(CategoricalAxis categoricalAxis, CategoricalColumnSelection selection) {
        super(categoricalAxis, selection);
        registerListeners();
    }

    private void registerListeners() {
        categoricalColumnSelection().selectedCategoriesProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                relayout();
            }
        }));
    }

    private CategoricalColumnSelection categoricalColumnSelection() {
        return (CategoricalColumnSelection)getColumnSelection();
    }

    private CategoricalAxis categoricalAxis() {
        return (CategoricalAxis)getAxis();
    }

    @Override
    protected void handleRectangleMouseEntered() {

    }

    @Override
    protected void handleRectangleMouseExited() {

    }

    @Override
    protected void handleRectangleMouseDragged(MouseEvent event) {

    }

    @Override
    protected void handleRectangleMousePressed(MouseEvent event) {

    }

    @Override
    protected void handleRectangleMouseReleased() {

    }

    @Override
    protected void handleBottomCrossbarMouseEntered() {

    }

    @Override
    protected void handleBottomCrossbarMouseExited() {

    }

    @Override
    protected void handleBottomCrossbarMouseDragged(MouseEvent event) {

    }

    @Override
    protected void handleBottomCrossbarMousePressed() {

    }

    @Override
    protected void handleBottomCrossbarMouseReleased() {

    }

    @Override
    protected void handleTopCrossbarMouseEntered() {

    }

    @Override
    protected void handleTopCrossbarMouseExited() {

    }

    @Override
    protected void handleTopCrossbarMouseDragged(MouseEvent event) {

    }

    @Override
    protected void handleTopCrossbarMousePressed() {

    }

    @Override
    protected void handleTopCrossbarMouseReleased() {

    }

    @Override
    public void relayout() {

    }
}
