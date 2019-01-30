package gov.ornl.datatableview;

import gov.ornl.datatable.CategoricalColumn;
import gov.ornl.datatable.Column;
import gov.ornl.datatable.ColumnSelection;
import gov.ornl.datatable.DataTable;
import javafx.beans.property.*;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.logging.Logger;

public abstract class Axis {
    protected final static Logger log = Logger.getLogger(Axis.class.getName());

    public final static double DEFAULT_TITLE_TEXT_SIZE = 12d;
    public final static double DEFAULT_TEXT_SIZE = 10d;
    public final static double DEFAULT_STROKE_WIDTH = 1.5;
    public final static Color DEFAULT_TEXT_COLOR = Color.BLACK;
    public final static Color DEFAULT_HISTOGRAM_FILL = new Color(Color.SILVER.getRed(), Color.SILVER.getGreen(),
            Color.SILVER.getBlue(), 0.6);
    public final static Color DEFAULT_HISTOGRAM_STROKE = Color.SILVER.darker();
    public final static int DEFAULT_MAX_HISTOGRAM_BIN_WIDTH = 30;
    public final static Color DEFAULT_SELECTION_FILL_COLOR = new Color(Color.ORANGE.getRed(), Color.ORANGE.getGreen(),
            Color.ORANGE.getBlue(), 0.35);
    public final static Color DEFAULT_AXIS_BAR_FILL_COLOR = Color.gray(0.95);
    public final static Color DEFAULT_AXIS_BAR_CONTEXT_FILL_COLOR = Color.gray(0.9);
    public final static Color DEFAULT_AXIS_BAR_CONTEXT_STROKE_COLOR = Color.gray(0.8);
    public final static Color DEFAULT_AXIS_BAR_STROKE_COLOR = Color.gray(0.6);
    public final static Color DEFAULT_CONTEXT_LIMIT_HANDLE_COLOR = Color.gray(0.5);

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

    // properties
    protected SimpleObjectProperty<Paint> overallHistogramFill = new SimpleObjectProperty<>(DEFAULT_HISTOGRAM_FILL);
    protected SimpleObjectProperty<Paint> overallHistogramStroke = new SimpleObjectProperty<>(DEFAULT_HISTOGRAM_STROKE);
    protected SimpleObjectProperty<Paint> queryHistogramFill;
    protected SimpleObjectProperty<Paint> queryHistogramStroke;

    private Column column;

    public Axis(DataTableView dataTableView, Column column) {
        this.column = column;

        this.dataTableView = dataTableView;

        queryHistogramFill = new SimpleObjectProperty<>(new Color(dataTableView.getSelectedItemsColor().getRed(),
                dataTableView.getSelectedItemsColor().getGreen(),
                dataTableView.getSelectedItemsColor().getBlue(), ((Color)overallHistogramFill.get()).getOpacity()));
        queryHistogramStroke = new SimpleObjectProperty<>(((Color)queryHistogramFill.get()).darker());

        titleText = new Text(column.getName());
        titleText.setFont(new Font(DEFAULT_TITLE_TEXT_SIZE));
        titleText.setSmooth(true);
        titleText.setFill(DEFAULT_TEXT_COLOR);
        titleText.setMouseTransparent(true);

        axisDraggingMessageText = new Text();
        axisDraggingMessageText.setFont(new Font(DEFAULT_TEXT_SIZE));
        axisDraggingMessageText.setFill(DEFAULT_TEXT_COLOR);
        axisDraggingMessageText.setMouseTransparent(true);

        titleTextRectangle = new Rectangle();
        Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bind(column.nameProperty());
        Tooltip.install(titleTextRectangle, tooltip);
        titleTextRectangle.setStrokeWidth(3.);
        titleTextRectangle.setStroke(Color.TRANSPARENT);
        titleTextRectangle.setFill(Color.TRANSPARENT);
        titleTextRectangle.setArcWidth(6.);
        titleTextRectangle.setArcHeight(6.);
        titleTextRectangle.setCursor(Cursor.HAND);

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

    public int getColumnIndex() { return getDataTable().getColumnIndex(getColumn()); }

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
        column.nameProperty().addListener(observable -> {
            resizeTitleText();
        });

        dataTableView.selectedItemsColorProperty().addListener(observable -> {
            queryHistogramFill.set(new Color(dataTableView.getSelectedItemsColor().getRed(),
                    dataTableView.getSelectedItemsColor().getGreen(),
                    dataTableView.getSelectedItemsColor().getBlue(), ((Color)overallHistogramFill.get()).getOpacity()));
            queryHistogramStroke = new SimpleObjectProperty<>(((Color)queryHistogramFill.get()).darker());
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

                    CheckMenuItem categoryHeightProportionalToCountCheck = new CheckMenuItem("Set Category Height Proportional To Count");
                    categoryHeightProportionalToCountCheck.selectedProperty().bindBidirectional(((CategoricalAxis)this).categoryHeightProportionalToCountProperty());
                    contextMenu.getItems().add(1, categoryHeightProportionalToCountCheck);

                    MenuItem addSelection = new MenuItem("Select Categories");
                    addSelection.setOnAction(event1 -> {
                        TextInputDialog dialog = new TextInputDialog("");
                        dialog.setTitle("Add Categorical Axis Selection");
                        dialog.setHeaderText("Enter " + this.getColumn().getName() + " axis categories to select");
                        dialog.setContentText("Categories (category1,category2,...):");

                        Optional<String> result = dialog.showAndWait();
                        result.ifPresent(categoriesString -> {
                            System.out.println("Selecting " + categoriesString);
                            String categories[] = categoriesString.split(",");
                            HashSet<String> categorySet = new HashSet<>();
                            for (String category : categories) {
                                category = category.trim();
                                if (((CategoricalColumn)getColumn()).getCategories().contains(category)) {
                                    categorySet.add(category);
                                }
                            }
                            if (!categorySet.isEmpty()) {
                                getDataTableView().addCategoricalSelection((CategoricalColumn) getColumn(), categorySet);
                            }
                        });

                    });
                    contextMenu.getItems().add(2, addSelection);
                }

                if (this instanceof BivariateAxis) {
                    CheckMenuItem showXAxisMarginValuesCheck = new CheckMenuItem("Show X Axis Margin Values");
                    showXAxisMarginValuesCheck.selectedProperty().bindBidirectional(((BivariateAxis)this).getScatterplot().showXAxisMarginValuesProperty());
                    showXAxisMarginValuesCheck.setOnAction(event1 -> {
                        getDataTableView().resizeView();
                    });
                    CheckMenuItem showYAxisMarginValuesCheck = new CheckMenuItem("Show Y Axis Margin Values");
                    showYAxisMarginValuesCheck.setOnAction(event1 -> {
                        getDataTableView().resizeView();
                    });
                    showYAxisMarginValuesCheck.selectedProperty().bindBidirectional(((BivariateAxis)this).getScatterplot().showYAxisMarginValuesProperty());
                    MenuItem swapAxesMenuItem = new MenuItem("Swap X and Y Axes");
                    swapAxesMenuItem.setOnAction(event1 -> {
                        ((BivariateAxis)this).swapColumnAxes();
//                        getDataTableView().resizeView();
                    });
                    contextMenu.getItems().add(0, showXAxisMarginValuesCheck);
                    contextMenu.getItems().add(1, showYAxisMarginValuesCheck);
                    contextMenu.getItems().add(2, swapAxesMenuItem);
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
                                dragEndPoint.getX() <= ((UnivariateAxis)axis).getBarRightX() &&
                                (this != axis)) {
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
                                if (this != axis) {
                                    getDataTable().addBivariateColumn(axis.getColumn(), this.getColumn(), i);
                                    return;
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                }

                int newColumnIndex = -1;

                for (int i = 0; i < dataTableView.getAxisCount(); i++) {
                    Axis axis = dataTableView.getAxis(i);

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
            }
        });

        highlighted.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                titleTextRectangle.setFill(DEFAULT_SELECTION_FILL_COLOR);
//                titleTextRectangle.setStroke(DEFAULT_HISTOGRAM_STROKE);
//                titleTextRectangle.setFill(Color.web("#ffc14d"));
            } else {
                titleTextRectangle.setFill(Color.TRANSPARENT);
//                titleTextRectangle.setStroke(Color.TRANSPARENT);
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

    private void resizeTitleText() {
        titleText.setText(column.getName());
        if (titleText.getLayoutBounds().getWidth() > (bounds.getWidth() - 8.)) {
            // truncate the column name to fit axis bounds
            while (titleText.getLayoutBounds().getWidth() > (bounds.getWidth() - 8.)) {
                titleText.setText(titleText.getText().substring(0, titleText.getText().length() - 1));
            }
        }
        titleText.setX(bounds.getMinX() + ((bounds.getWidth() - titleText.getLayoutBounds().getWidth()) / 2.));
        titleText.setY(bounds.getMinY() + titleText.getLayoutBounds().getHeight());
    }

    public void resize (double left, double top, double width, double height) {
        bounds = new BoundingBox(left, top, width, height);
        centerX = left + (width / 2.);
        centerY = top + (height / 2.);

        resizeTitleText();
//        titleText.setText(column.getName());
//        if (titleText.getLayoutBounds().getWidth() > bounds.getWidth()) {
//            // truncate the column name to fit axis bounds
//            while (titleText.getLayoutBounds().getWidth() > bounds.getWidth()) {
//                titleText.setText(titleText.getText().substring(0, titleText.getText().length() - 1));
//            }
//        }
//        titleText.setX(bounds.getMinX() + ((width - titleText.getLayoutBounds().getWidth()) / 2.));
//        titleText.setY(bounds.getMinY() + titleText.getLayoutBounds().getHeight());

        titleTextRectangle.setX(titleText.getX() - 4.);
        titleTextRectangle.setY(titleText.getY() - titleText.getLayoutBounds().getHeight());
        titleTextRectangle.setWidth(titleText.getLayoutBounds().getWidth() + 8.);
        titleTextRectangle.setHeight(titleText.getLayoutBounds().getHeight() + 4.);
    }
}
