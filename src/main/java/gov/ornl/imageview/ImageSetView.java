package gov.ornl.imageview;


import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.BoundingBox;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ImageSetView extends Region {
    public final static Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;

    private final static Logger log = Logger.getLogger(ImageSetView.class.getName());

    private Pane pane;

    private ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(DEFAULT_BACKGROUND_COLOR);
    private BoundingBox viewRegionBounds;
    private Rectangle viewRegionRectangle;

    private ObjectProperty<Orientation> orientation = new SimpleObjectProperty<>(Orientation.HORIZONTAL);

    private ArrayList<Image> imageList = new ArrayList<>();

    private double maxImageHeight = 0.;
    private double maxImageWidth = 0;

    public ImageSetView () {
        setMinSize(200, 200);
        initialize();
        registerListeners();
    }

    public void setImages(List<Image> images) {
        imageList.addAll(images);
        resizeView();
    }

    private void initialize() {
        viewRegionRectangle = new Rectangle();
        viewRegionRectangle.setStroke(Color.BLUE);
        viewRegionRectangle.setFill(Color.TRANSPARENT);

        pane = new Pane();
        this.setBackground(new Background(new BackgroundFill(backgroundColor.get(), new CornerRadii(0), Insets.EMPTY)));

        pane.getChildren().add(viewRegionRectangle);

        getChildren().add(pane);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resizeView());
        heightProperty().addListener(o -> resizeView());

        backgroundColor.addListener((observable, oldValue, newValue) -> {
            this.setBackground(new Background(new BackgroundFill(backgroundColor.get(), new CornerRadii(0), Insets.EMPTY)));
        });
    }

    public Orientation getOrientation() { return orientation.get(); }

    public void setOrientation(Orientation orientation) { this.orientation.set(orientation); }

    public ObjectProperty<Orientation> orientationProperty() { return orientation; }

    private void resizeView() {
//        viewRegionBounds = new BoundingBox(getInsets().getLeft(), getInsets().getTop(),
//                getWidth() - (getInsets().getLeft() + getInsets().getRight()),
//                getHeight() - (getInsets().getTop() + getInsets().getBottom()));
//        viewRegionRectangle.setX(viewRegionBounds.getMinX());
//        viewRegionRectangle.setY(viewRegionBounds.getMinY());
//        viewRegionRectangle.setWidth(viewRegionBounds.getWidth());
//        viewRegionRectangle.setHeight(viewRegionBounds.getHeight());

        if (!imageList.isEmpty()) {
            if (getOrientation() == Orientation.HORIZONTAL) {
                maxImageHeight = viewRegionBounds.getHeight();
                maxImageWidth = viewRegionBounds.getHeight();


            } else {
            }
        }
    }
}
