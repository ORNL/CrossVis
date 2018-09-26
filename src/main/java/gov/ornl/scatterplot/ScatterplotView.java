package gov.ornl.scatterplot;

import gov.ornl.datatable.Column;
import gov.ornl.datatable.DoubleColumn;
import javafx.beans.property.*;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;

public class ScatterplotView extends Region {
    public final static Color DEFAULT_TEXT_COLOR = Color.BLACK;
    public final static Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    public final static double DEFAULT_TEXT_SIZE = 10.;
    public final static double DEFAULT_MAX_AXIS_SIZE = 50.;
    private static final double DEFAULT_POINT_STROKE_OPACITY = 0.5;
    private static final Color DEFAULT_AXIS_STROKE_COLOR = Color.LIGHTGRAY;
    private static final Color DEFAULT_AXIS_TEXT_COLOR = Color.BLACK;
    private static final Color DEFAULT_SELECTED_POINT_COLOR = new Color(Color.STEELBLUE.getRed(),
            Color.STEELBLUE.getGreen(), Color.STEELBLUE.getBlue(), DEFAULT_POINT_STROKE_OPACITY);
    private static final Color DEFAULT_UNSELECTED_POINT_COLOR = new Color(Color.LIGHTGRAY.getRed(),
            Color.LIGHTGRAY.getGreen(), Color.LIGHTGRAY.getBlue(), DEFAULT_POINT_STROKE_OPACITY);

    private final static Logger log = Logger.getLogger(ScatterplotView.class.getName());

    private Pane pane;
    private Column xColumn;
    private Column yColumn;
    
    private Group graphicsGroup = new Group();
    
    private Text xAxisText;
    private Text yAxisText;
    
    private Canvas selectedCanvas;
    private Canvas unselectedCanvas;

    private ArrayList<double[]> points = new ArrayList<>();
    private HashSet<double[]> selectedPoints = new HashSet<>();
    private HashSet<double[]> unselectedPoints = new HashSet<>();

    private BoundingBox viewRegionBounds;
    private BoundingBox plotRegionBounds;
    private BoundingBox xAxisRegionBounds;
    private BoundingBox yAxisRegionBounds;
    
    private Rectangle viewRegionRectangle;
    private Rectangle plotRegionRectangle;
    private Rectangle xAxisRegionRectangle;
    private Rectangle yAxisRegionRectangle;
    
    private ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(DEFAULT_BACKGROUND_COLOR);
    private ObjectProperty<Color> textColor = new SimpleObjectProperty<>(DEFAULT_TEXT_COLOR);
    private ObjectProperty<Color> axisStrokeColor = new SimpleObjectProperty<>(DEFAULT_AXIS_STROKE_COLOR);
    private ObjectProperty<Color> axisTextColor = new SimpleObjectProperty<>(DEFAULT_AXIS_TEXT_COLOR);
    private ObjectProperty<Color> selectedPointStrokeColor = new SimpleObjectProperty<>(DEFAULT_SELECTED_POINT_COLOR);
    private ObjectProperty<Color> unselectedPointStrokeColor = new SimpleObjectProperty<>(DEFAULT_UNSELECTED_POINT_COLOR);
    
    private DoubleProperty maxAxisSize = new SimpleDoubleProperty(DEFAULT_MAX_AXIS_SIZE);

    private double axisSize = 12;
    private double pointSize = 4.;
    private double pointStrokeWidth = 1.2;
    private double pointStrokeOpacity = DEFAULT_POINT_STROKE_OPACITY;
    
    private BooleanProperty showSelectedPoints = new SimpleBooleanProperty(true);
    private BooleanProperty showUnselectedPoints = new SimpleBooleanProperty(false);
    
    public ScatterplotView(Column xColumn, Column yColumn) {
        this.xColumn = xColumn;
        this.yColumn = yColumn;
        initialize();
        registerListeners();
    }
//
//    public void setXColumn(Column xColumn) {
//        this.xColumn = xColumn;
//        initView();
//        resizeView();
//    }
//
//    public void setYColumn(Column yColumn) {
//        this.yColumn = yColumn;
//        initView();
//        resizeView();
//    }
//
//    public void setColumns(Column xColumn, Column yColumn) {
//        this.xColumn = xColumn;
//        this.yColumn = yColumn;
//        initView();
//        resizeView();
//    }

    private void initialize() {
        setBackground(new Background(new BackgroundFill(backgroundColor.get(), new CornerRadii(0), Insets.EMPTY)));

        selectedCanvas = new Canvas();
        unselectedCanvas = new Canvas();
        
        viewRegionRectangle = new Rectangle();
        viewRegionRectangle.setStroke(Color.DARKBLUE);
        viewRegionRectangle.setFill(Color.TRANSPARENT);
        viewRegionRectangle.setMouseTransparent(true);
        viewRegionRectangle.setStrokeWidth(2);

        plotRegionRectangle = new Rectangle();
        plotRegionRectangle.setStroke(Color.DARKBLUE);
        plotRegionRectangle.setFill(Color.TRANSPARENT);
        plotRegionRectangle.setMouseTransparent(true);
        plotRegionRectangle.setStrokeWidth(2);

        xAxisRegionRectangle = new Rectangle();
        xAxisRegionRectangle.setStroke(Color.DARKBLUE);
        xAxisRegionRectangle.setFill(Color.TRANSPARENT);
        xAxisRegionRectangle.setMouseTransparent(true);
        xAxisRegionRectangle.setStrokeWidth(2);

        yAxisRegionRectangle = new Rectangle();
        yAxisRegionRectangle.setStroke(Color.DARKBLUE);
        yAxisRegionRectangle.setFill(Color.TRANSPARENT);
        yAxisRegionRectangle.setMouseTransparent(true);
        yAxisRegionRectangle.setStrokeWidth(2);

        pane = new Pane();
        pane.getChildren().addAll(selectedCanvas, unselectedCanvas, viewRegionRectangle, plotRegionRectangle,
                xAxisRegionRectangle, yAxisRegionRectangle, xAxisText, yAxisText);

        getChildren().add(pane);
    }

    private void registerListeners() {

    }

    private void clearView() {

    }

    private void initView() {
        if (xColumn != null && yColumn != null && xColumn instanceof DoubleColumn && yColumn instanceof DoubleColumn) {
            
        }
    }

    private void resizeView() {
        viewRegionBounds = new BoundingBox(getInsets().getLeft(), getInsets().getTop(),
                getWidth() - (getInsets().getLeft() + getInsets().getRight()),
                getHeight() - (getInsets().getTop() + getInsets().getBottom()));
        viewRegionRectangle.setX(viewRegionBounds.getMinX());
        viewRegionRectangle.setY(viewRegionBounds.getMinY());
        viewRegionRectangle.setWidth(viewRegionBounds.getWidth());
        viewRegionRectangle.setHeight(viewRegionBounds.getHeight());

        
    }

    private void redrawView() {
        
    }

    public Color getBackgroundColor() {
        return backgroundColor.get();
    }

    public void setBackgroundColor(Color newBackgroundColor) {
        this.backgroundColor.set(newBackgroundColor);
        pane.setBackground(new Background(new BackgroundFill(backgroundColor.get(), new CornerRadii(0), Insets.EMPTY)));
    }

    public ObjectProperty<Color> backgroundColorProperty() {
        return backgroundColor;
    }

    public Color getLabelsColor() {
        return textColor.get();
    }

    public void setLabelsColor(Color labelsColor) {
        this.textColor.set(labelsColor);
    }

    public ObjectProperty<Color> labelsColorProperty() {
        return textColor;
    }
}
