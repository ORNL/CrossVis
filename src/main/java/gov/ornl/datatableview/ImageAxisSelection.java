package gov.ornl.datatableview;

import gov.ornl.datatable.ImageColumnSelection;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;

import java.awt.*;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class ImageAxisSelection extends UnivariateAxisSelection {

    public ImageAxisSelection(ImageAxis imageAxis, ImageColumnSelection columnSelection, double minValueY,
                              double maxValueY) {
        super(imageAxis, columnSelection, minValueY, maxValueY);
        registerListeners();
    }

    private void registerListeners() {
        imageColumnSelection().selectedImagePairSetProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                resize();
            }
        });
    }

    private ImageColumnSelection imageColumnSelection() {
        return (ImageColumnSelection)getColumnSelection();
    }

    private ImageAxis imageAxis() { return (ImageAxis)getAxis(); }

    protected void update(Set<Pair<File, Image>> selectedImagePairs, double minValueY, double maxValueY) {
        imageColumnSelection().setSelectedImagePairs(selectedImagePairs);
        layoutGraphics(minValueY, maxValueY);
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
    public void resize() {

    }
}
