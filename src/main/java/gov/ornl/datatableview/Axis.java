package gov.ornl.datatableview;

import gov.ornl.datatable.Column;
import gov.ornl.datatable.ColumnSelection;
import gov.ornl.datatable.DataTable;
import gov.ornl.pcpview.PCPAxisSelection;
import javafx.beans.property.*;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.logging.Logger;

public abstract class Axis {
    private final static Logger log = Logger.getLogger(Axis.class.getName());

    public final static double DEFAULT_TITLE_TEXT_SIZE = 12d;
    public final static double DEFAULT_TEXT_SIZE = 10d;
    public final static double DEFAULT_STROKE_WIDTH = 1.5;
    public final static Color DEFAULT_TEXT_COLOR = Color.BLACK;
    public final static Color DEFAULT_HISTOGRAM_FILL = Color.SILVER.deriveColor(1, 1, 1, .8);
    public final static Color DEFAULT_QUERY_HISTOGRAM_FILL = Color.SLATEGRAY.deriveColor(1., 1., 1., 0.8);
    public final static Color DEFAULT_HISTOGRAM_STROKE = Color.DARKGRAY;
    public final static int DEFAULT_MAX_HISTOGRAM_BIN_WIDTH = 30;

    private DataTableView dataTableView;

    protected double maxHistogramBinWidth = DEFAULT_MAX_HISTOGRAM_BIN_WIDTH;

    private Text titleText;
    private Rectangle titleTextRectangle;

    private Bounds bounds;
    private double centerX = 0d;
    private double centerY = 0d;

    private ObjectProperty<Color> textColor = new SimpleObjectProperty<>(DEFAULT_TEXT_COLOR);
    private BooleanProperty highlighted = new SimpleBooleanProperty(false);

    private Group graphicsGroup = new Group();

    protected Group axisSelectionGraphicsGroup = new Group();

    private ArrayList<AxisSelection> axisSelectionList = new ArrayList<>();

    // dragging variables
    protected Group axisDraggingGraphicsGroup;
    protected Text axisDraggingMessageText;

    protected Point2D dragStartPoint;
    protected Point2D dragEndPoint;
    protected boolean dragging = false;

    protected Color histogramFill = DEFAULT_HISTOGRAM_FILL;
//    protected Color histogramStroke = DEFAULT_HISTOGRAM_STROKE;
    protected Color queryHistogramFill = DEFAULT_QUERY_HISTOGRAM_FILL;

    private Column column;

    public Axis(DataTableView dataTableView, Column column) {
        this.column = column;

        this.dataTableView = dataTableView;

        titleText = new Text(column.getName());
        Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bindBidirectional(column.nameProperty());
        Tooltip.install(titleText, tooltip);
        titleText.setFont(new Font(DEFAULT_TITLE_TEXT_SIZE));
        titleText.setSmooth(true);
        titleText.setFill(DEFAULT_TEXT_COLOR);
        titleText.setMouseTransparent(true);

        axisDraggingMessageText = new Text();
        axisDraggingMessageText.setFont(new Font(DEFAULT_TEXT_SIZE));
        axisDraggingMessageText.setFill(DEFAULT_TEXT_COLOR);
        axisDraggingMessageText.setMouseTransparent(true);

        titleTextRectangle = new Rectangle();
        titleTextRectangle.setStrokeWidth(3.);
        titleTextRectangle.setStroke(Color.TRANSPARENT);
        titleTextRectangle.setFill(Color.TRANSPARENT);
        titleTextRectangle.setArcWidth(6.);
        titleTextRectangle.setArcHeight(6.);

        graphicsGroup.getChildren().addAll(titleTextRectangle, titleText, axisSelectionGraphicsGroup);

        registerListeners();
    }

    protected boolean removeAxisSelection(ColumnSelection columnSelection) {
        // find the axis selection for the given column selection
        AxisSelection axisSelection = getAxisSelection(columnSelection);
        if (axisSelection != null) {
            // remove the axis selection's graphics
            axisSelectionGraphicsGroup.getChildren().remove(axisSelection.getGraphicsGroup());
            return getAxisSelectionList().remove(axisSelection);
        }

        return false;
    }

    protected  void removeAllAxisSelections() {
        axisSelectionGraphicsGroup.getChildren().clear();
        axisSelectionList.clear();
    }

    protected AxisSelection getAxisSelection(ColumnSelection columnSelection) {
        for (AxisSelection axisSelection : getAxisSelectionList()) {
            if (axisSelection.getColumnSelection() == columnSelection) {
                return axisSelection;
            }
        }

        return null;
    }

    public ArrayList<AxisSelection> getAxisSelectionList() { return axisSelectionList; }

    public Column getColumn() { return column; }

    public boolean isHighlighted() { return highlighted.get(); }

    protected abstract AxisSelection addAxisSelection(ColumnSelection columnSelection);

    public void setHighlighted(boolean highlighted) {
        if (isHighlighted() != highlighted) {
            this.highlighted.set(highlighted);
        }
    }

    public BooleanProperty highlightedProperty() { return highlighted; }

    public Color getTextColor() { return textColor.get(); }

    public void setTextColor(Color c) { textColor.set(c); }

    public ObjectProperty<Color> textColorProperty() { return textColor; }

    protected Bounds getBounds() { return bounds; }

    protected double getCenterX() { return centerX; }

    protected double getCenterY() { return centerY; }

    private void registerListeners() {
        titleText.textProperty().addListener((observable, oldValue, newValue) -> {
            titleText.setX(bounds.getMinX() + ((bounds.getWidth() - titleText.getLayoutBounds().getWidth()) / 2.));
            titleText.setY(bounds.getMinY() + titleText.getLayoutBounds().getHeight());
        });

        titleTextRectangle.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                if (getDataTable().getHighlightedColumn() == getColumn()) {
                    getDataTable().setHighlightedColumn(null);
//                    dataTableView.setHighlightedAxis(this);
                } else {
                    getDataTable().setHighlightedColumn(getColumn());
//                    dataTableView.setHighlightedAxis(null);
                }
            }
        });

        titleTextRectangle.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                final ContextMenu contextMenu = new ContextMenu();
                MenuItem hideMenuItem = new MenuItem("Remove Axis");
                hideMenuItem.setOnAction(removeEvent -> {
                    // remove this axis from the data table view
//                    dataTableView.removeAxis(this);
                    getDataTable().disableColumn(column);
                });

                MenuItem closeMenuItem = new MenuItem("Close Menu");
                closeMenuItem.setOnAction(closeEvent -> contextMenu.hide());

                contextMenu.getItems().addAll(hideMenuItem, closeMenuItem);

                if (this instanceof CategoricalAxis) {
                    CheckMenuItem showCategoryLabels = new CheckMenuItem("Show Category Labels");
                    showCategoryLabels.selectedProperty().bindBidirectional(((CategoricalAxis)this).showCategoryLabelsProperty());
                    contextMenu.getItems().add(0, showCategoryLabels);

                    CheckMenuItem categoryHeightProportionalToCountCheck = new CheckMenuItem("Category Height Proportional To Count");
                    categoryHeightProportionalToCountCheck.selectedProperty().bindBidirectional(((CategoricalAxis)this).categoryHeightProportionalToCountProperty());
                    contextMenu.getItems().add(1, categoryHeightProportionalToCountCheck);
                }
                contextMenu.show(dataTableView, event.getScreenX(), event.getScreenY());
            }
        });

        titleTextRectangle.setOnMouseDragged(event -> {
            if (!dragging) {
                dragging = true;
                makeAxisDraggingGraphics();
                axisDraggingMessageText.setX(getCenterX());
                axisDraggingMessageText.setY(getCenterY());
                axisDraggingGraphicsGroup.getChildren().add(axisDraggingMessageText);
                axisDraggingGraphicsGroup.setEffect(new DropShadow());
                dataTableView.getPane().getChildren().add(axisDraggingGraphicsGroup);
                dragStartPoint = new Point2D(event.getX(), event.getY());
            }

            dragEndPoint = new Point2D(event.getX(), event.getY());
            axisDraggingGraphicsGroup.setTranslateX(event.getX() - dragStartPoint.getX());

            axisDraggingMessageText.setText("Move " + this.getColumn().getName() + " Axis");
            if (this instanceof UnivariateAxis) {
                for (int i = 0; i < dataTableView.getAxisCount(); i++) {
                    Axis axis = dataTableView.getAxis(i);
                    if (axis instanceof UnivariateAxis) {
                        if (dragEndPoint.getX() >= ((UnivariateAxis)axis).getBarLeftX() &&
                                dragEndPoint.getX() <= ((UnivariateAxis)axis).getBarRightX()) {
                            axisDraggingMessageText.setText("Create BiVariate Axis (" + this.getColumn().getName() + " vs. " + axis.getColumn().getName());
                        }
                    }
                }
            }

            axisDraggingMessageText.setX(getCenterX() - axisDraggingMessageText.getLayoutBounds().getWidth() / 2.);
        });

        titleTextRectangle.setOnMouseReleased(event -> {
            if (dragging) {
                dataTableView.getPane().getChildren().remove(axisDraggingGraphicsGroup);
                dragging = false;

                if (this instanceof UnivariateAxis) {
                    for (int i = 0; i < dataTableView.getAxisCount(); i++) {
                        Axis axis = dataTableView.getAxis(i);
                        if (axis instanceof UnivariateAxis) {
                            if (dragEndPoint.getX() >= ((UnivariateAxis)axis).getBarLeftX() &&
                                dragEndPoint.getX() <= ((UnivariateAxis)axis).getBarRightX()) {
                                getDataTable().addBivariateColumn(axis.getColumn(), this.getColumn(), i);
                                return;
                            }
                        }
                    }
                }

                int newColumnIndex = -1;

                for (int i = 0; i < dataTableView.getAxisCount(); i++) {
                    Axis axis = dataTableView.getAxis(i);
//
//                    if (this instanceof UnivariateAxis && axis instanceof UnivariateAxis) {
//                        log.info("dragEndPoint.getX(): " + dragEndPoint.getX() + " event.getX(): " + event.getX() + " bar: [" + ((UnivariateAxis)axis).getBarLeftX() +
//                                "," + ((UnivariateAxis)axis).getBarRightX());
//                        if (dragEndPoint.getX() >= ((UnivariateAxis)axis).getBarLeftX() && dragEndPoint.getX() <= ((UnivariateAxis)axis).getBarRightX()) {
//                            dataTableView.getDataTable().addBivariateColumn(axis.getColumn(), this.getColumn(), i);
//                            break;
//                        }
//                    }

                    if ( (i == 0) && (dragEndPoint.getX() < axis.getCenterX()) ) {
                        newColumnIndex = 0;
                        break;
                    }

                    if (dragEndPoint.getX() > axis.getCenterX()) {
                        if ((i + 1) < dataTableView.getAxisCount()) {
                            Axis nextAxis = dataTableView.getAxis(i + 1);
                            if (dragEndPoint.getX() >= axis.getCenterX() && dragEndPoint.getX() < nextAxis.getCenterX()) {
                                newColumnIndex = i + 1;
                                break;
                            }
                        } else {
                            newColumnIndex = dataTableView.getAxisCount();
                        }
                    }
                }

                if (newColumnIndex != -1) {
                    getDataTable().changeColumnOrder(getColumn(), newColumnIndex);
                }

//                getDataTable().changeColumnOrder(getColumn(), getDataTable().getColumnCount());
//                int currentAxisPosition = getDataTable().getColumnIndex(getColumn());
//                int newAxisPosition = (int)Math.floor(((dragEndPoint.getX() - (dataTableView.getAxisSpacing() / 2.)) / dataTableView.getAxisSpacing()));
//
//                if (newAxisPosition < 0) {
//                    newAxisPosition = 0;
//                } else if (currentAxisPosition > newAxisPosition) {
//                    newAxisPosition++;
//                }
//
//                if (this instanceof UnivariateAxis) {
//                    Axis currentAxisAtNewLocation = getDataTableView().getAxis(newAxisPosition);
//                    if (currentAxisAtNewLocation instanceof UnivariateAxis) {
//                        Rectangle axisBar = ((UnivariateAxis)currentAxisAtNewLocation).getAxisBar();
//                        if (event.getX() >= axisBar.getX() && (event.getX() <= (axisBar.getX() + axisBar.getWidth()))) {
//                            dataTableView.getDataTable().addBivariateColumn(currentAxisAtNewLocation.getColumn(),
//                                    this.getColumn(), newAxisPosition);
//                            return;
//                        }
//                    }
//                }
//
//                if (newAxisPosition != currentAxisPosition) {
//                    getDataTable().changeColumnOrder(getColumn(), newAxisPosition);
//                }

//                int newAxisPosition = (int)dragEndPoint.getX() / dataTableView.getAxisSpacing();
//
//                if (!(newAxisPosition == getDataTable().getColumnIndex(column))) {
//                    if (this instanceof UnivariateAxis) {
//                        Axis nearestAxis = getDataTableView().getAxis(newAxisPosition);
//                        if (nearestAxis instanceof UnivariateAxis) {
//                            Rectangle axisBar = ((UnivariateAxis) nearestAxis).getAxisBar();
//                            if ((event.getX() >= axisBar.getX()) && (event.getX() <= (axisBar.getX() + axisBar.getWidth()))) {
//                                // dropped dragging univariate axis on another univariate axis
//                                // create a new bivariate axis and add at the newAxisPosition
//                                dataTableView.getDataTable().addBivariateColumn(this.getColumn(), nearestAxis.getColumn(), newAxisPosition);
//                                return;
//                            }
//                        }
//                    }
//
//                    getDataTable().changeColumnOrder(getColumn(), newAxisPosition);
//                }
//                dataTableView.setAxisPosition(this, newAxisPosition);
            }
        });

        highlighted.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                titleTextRectangle.setFill(Color.web("#ffc14d"));
            } else {
                titleTextRectangle.setFill(Color.TRANSPARENT);
            }
        });
    }

    protected void makeAxisDraggingGraphics() {
        Text draggingNameText = new Text(titleText.getText());
        draggingNameText.setX(titleText.getX());
        draggingNameText.setY(titleText.getY());
        draggingNameText.setFont(titleText.getFont());

        axisDraggingGraphicsGroup = new Group(draggingNameText);
        axisDraggingGraphicsGroup.setTranslateY(5);

        if (this instanceof UnivariateAxis) {
            UnivariateAxis univariateAxis = (UnivariateAxis)this;
            Rectangle dragAxisBar = new Rectangle(univariateAxis.getAxisBar().getX(),
                    univariateAxis.getAxisBar().getY(), univariateAxis.getAxisBar().getWidth(),
                    univariateAxis.getAxisBar().getHeight());
            dragAxisBar.setStroke(univariateAxis.getAxisBar().getStroke());
            dragAxisBar.setFill(univariateAxis.getAxisBar().getFill());

            Rectangle dragUpperContextBar = new Rectangle(univariateAxis.getUpperContextBar().getX(),
                    univariateAxis.getUpperContextBar().getY(),
                    univariateAxis.getUpperContextBar().getWidth(),
                    univariateAxis.getUpperContextBar().getHeight());
            dragUpperContextBar.setStroke(univariateAxis.getUpperContextBar().getStroke());
            dragUpperContextBar.setFill(univariateAxis.getUpperContextBar().getFill());

            Rectangle dragLowerContextBar = new Rectangle(univariateAxis.getLowerContextBar().getX(),
                    univariateAxis.getLowerContextBar().getY(),
                    univariateAxis.getLowerContextBar().getWidth(), univariateAxis.getLowerContextBar().getHeight());
            dragLowerContextBar.setStroke(univariateAxis.getLowerContextBar().getStroke());
            dragLowerContextBar.setFill(univariateAxis.getLowerContextBar().getFill());

            axisDraggingGraphicsGroup.getChildren().addAll(dragUpperContextBar, dragLowerContextBar, dragAxisBar);
        } else if (this instanceof BivariateAxis) {
            BivariateAxis biAxis = (BivariateAxis)this;

            Rectangle scatterplotRectangle = new Rectangle(biAxis.getScatterplotRectangle().getX(),
                    biAxis.getScatterplotRectangle().getY(), biAxis.getScatterplotRectangle().getWidth(),
                    biAxis.getScatterplotRectangle().getHeight());
            scatterplotRectangle.setFill(dataTableView.getBackgroundColor());
            scatterplotRectangle.setStroke(biAxis.getScatterplotRectangle().getStroke());

            axisDraggingGraphicsGroup.getChildren().add(scatterplotRectangle);
        }
    }

    public Group getGraphicsGroup() { return graphicsGroup; }

    protected DataTableView getDataTableView() { return dataTableView; }

    protected DataTable getDataTable() { return dataTableView.getDataTable(); }

    protected Text getTitleText() { return titleText; }

    public void resize (double left, double top, double width, double height) {
        bounds = new BoundingBox(left, top, width, height);
        centerX = left + (width / 2.);
        centerY = top + (height / 2.);

        titleText.setText(column.getName());
        if (titleText.getLayoutBounds().getWidth() > bounds.getWidth()) {
            // truncate the column name to fit axis bounds
            while (titleText.getLayoutBounds().getWidth() > bounds.getWidth()) {
                titleText.setText(titleText.getText().substring(0, titleText.getText().length() - 1));
            }
        }
        titleText.setX(bounds.getMinX() + ((width - titleText.getLayoutBounds().getWidth()) / 2.));
        titleText.setY(bounds.getMinY() + titleText.getLayoutBounds().getHeight());

        titleTextRectangle.setX(titleText.getX() - 4.);
        titleTextRectangle.setY(titleText.getY() - titleText.getLayoutBounds().getHeight());
        titleTextRectangle.setWidth(titleText.getLayoutBounds().getWidth() + 8.);
        titleTextRectangle.setHeight(titleText.getLayoutBounds().getHeight() + 4.);
    }
}
