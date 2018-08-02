package gov.ornl.pcpview;

import gov.ornl.datatable.CategoricalColumnSelection;
import gov.ornl.datatable.DataTable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class PCPCategoricalAxisSelection extends PCPAxisSelection {

    public PCPCategoricalAxisSelection(PCPAxis pcpAxis, CategoricalColumnSelection selection, Pane pane, DataTable dataModel) {
        super(pcpAxis, selection, pane, dataModel);
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
        return (CategoricalColumnSelection)getColumnSelectionRange();
    }

    private PCPCategoricalAxis categoricalAxis() {
        return (PCPCategoricalAxis)getPCPAxis();
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
