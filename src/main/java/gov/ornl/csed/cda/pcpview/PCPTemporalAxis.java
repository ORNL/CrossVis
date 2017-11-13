package gov.ornl.csed.cda.pcpview;

import gov.ornl.csed.cda.datatable.DataModel;
import gov.ornl.csed.cda.datatable.TemporalColumn;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.converter.LocalDateTimeStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PCPTemporalAxis extends PCPAxis {
    public final static Logger log = LoggerFactory.getLogger(PCPTemporalAxis.class);

    public final static Color DEFAULT_LABEL_COLOR = Color.BLACK;
    public final static double DEFAULT_NAME_LABEL_HEIGHT = 30d;
    public final static double DEFAULT_NAME_TEXT_SIZE = 12d;
    public final static double DEFAULT_CONTEXT_HEIGHT = 20d;
    public final static double DEFAULT_BAR_WIDTH = 10d;
    public final static double DEFAULT_TEXT_SIZE = 10d;
    public final static double DEFAULT_STROKE_WIDTH = 1.5;
    
    public final static double DEFAULT_OVERVIEW_QUERY_BAR_SPACING = 30d;

//    private TemporalColumn column;

    private double barTopY;
    private double barBottomY;

    private Rectangle overviewAxisBar;
    private Line overViewTopCrossBarLine;
    private Line overviewBottomCrossBarLine;

    private Text overviewStartInstantText;
    private Text overviewEndInstantText;

    private Rectangle detailAxisBar;
    private Line detailTopCrossBarLine;
    private Line detailBottomCrossBarLine;

    private Text detailStartInstantText;
    private Text detailEndInstantText;

    public PCPTemporalAxis(PCPView pcpView, TemporalColumn column, DataModel dataModel, Pane pane) {
        super(pcpView, column, dataModel, pane);

        this.pcpView = pcpView;
        this.column = column;
        this.dataModel = dataModel;
        this.pane = pane;

        barTopY = 0d;
        barBottomY = 0d;

        overviewStartInstantText = new Text();
        overviewStartInstantText.textProperty().bindBidirectional(temporalColumn().startLocalDateTimeProperty(), new LocalDateTimeStringConverter());
        overviewStartInstantText.setFont(new Font(DEFAULT_TEXT_SIZE));
        overviewStartInstantText.setSmooth(true);

        overviewEndInstantText = new Text();
        overviewEndInstantText.textProperty().bindBidirectional(column.endLocalDateTimeProperty(), new LocalDateTimeStringConverter());
        overviewEndInstantText.setFont(new Font(DEFAULT_TEXT_SIZE));
        overviewEndInstantText.setSmooth(true);

        detailStartInstantText = new Text();
        detailStartInstantText.textProperty().bindBidirectional(column.queryStartLocalDateTimeProperty(), new LocalDateTimeStringConverter());
        detailStartInstantText.setFont(new Font(DEFAULT_TEXT_SIZE));
        detailStartInstantText.setSmooth(true);

        detailEndInstantText = new Text();
        detailEndInstantText.textProperty().bindBidirectional(column.queryEndLocalDateTimeProperty(), new LocalDateTimeStringConverter());
        detailEndInstantText.setFont(new Font(DEFAULT_TEXT_SIZE));
        detailEndInstantText.setSmooth(true);

        overviewAxisBar = new Rectangle();
        overviewAxisBar.setStroke(Color.DARKGRAY);
        overviewAxisBar.setFill(Color.WHITESMOKE);
        overviewAxisBar.setSmooth(true);
        overviewAxisBar.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        detailAxisBar = new Rectangle();
        detailAxisBar.setStroke(Color.DARKGRAY);
        detailAxisBar.setFill(Color.WHITESMOKE);
        detailAxisBar.setSmooth(true);
        detailAxisBar.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        overViewTopCrossBarLine = makeLine();
        overviewBottomCrossBarLine = makeLine();

        detailTopCrossBarLine = makeLine();
        detailBottomCrossBarLine = makeLine();

        graphicsGroup.getChildren().addAll(overviewAxisBar, detailAxisBar, overViewTopCrossBarLine,
                overviewBottomCrossBarLine, detailTopCrossBarLine, detailBottomCrossBarLine,
                overviewStartInstantText, overviewEndInstantText, detailStartInstantText,
                detailEndInstantText);

        registerListeners();
    }

    private TemporalColumn temporalColumn() {
        return (TemporalColumn)column;
    }

    private void registerListeners() {

    }

    @Override
    public void layout(double centerX, double topY, double width, double height) {
        this.centerX = centerX;
        double left = centerX - (width / 2d);
        bounds = new Rectangle(left, topY, width, height);
        barTopY = topY + DEFAULT_NAME_LABEL_HEIGHT;
        barBottomY = bounds.getY() + bounds.getHeight() - overviewEndInstantText.getLayoutBounds().getHeight();

        double overviewBarCenterX = centerX - (DEFAULT_OVERVIEW_QUERY_BAR_SPACING / 2d);
        double detailBarCenterX = overviewBarCenterX + DEFAULT_OVERVIEW_QUERY_BAR_SPACING;
        
        overviewAxisBar.setX(overviewBarCenterX - (DEFAULT_BAR_WIDTH / 2d));
        overviewAxisBar.setY(barTopY);
        overviewAxisBar.setWidth(DEFAULT_BAR_WIDTH);
        overviewAxisBar.setHeight(barBottomY - barTopY);

        overViewTopCrossBarLine.setStartY(barTopY);
        overViewTopCrossBarLine.setEndY(barTopY);
        overViewTopCrossBarLine.setStartX(overviewBarCenterX - (DEFAULT_BAR_WIDTH / 2.));
        overViewTopCrossBarLine.setEndX(overviewBarCenterX + (DEFAULT_BAR_WIDTH / 2.));

        overviewBottomCrossBarLine.setStartY(barBottomY);
        overviewBottomCrossBarLine.setEndY(barBottomY);
        overviewBottomCrossBarLine.setStartX(overviewBarCenterX - (DEFAULT_BAR_WIDTH / 2.));
        overviewBottomCrossBarLine.setEndX(overviewBarCenterX + (DEFAULT_BAR_WIDTH / 2.));

        detailAxisBar.setX(detailBarCenterX - (DEFAULT_BAR_WIDTH / 2d));
        detailAxisBar.setY(barTopY);
        detailAxisBar.setWidth(DEFAULT_BAR_WIDTH);
        detailAxisBar.setHeight(barBottomY - barTopY);

        detailTopCrossBarLine.setStartY(barTopY);
        detailTopCrossBarLine.setEndY(barTopY);
        detailTopCrossBarLine.setStartX(detailBarCenterX - (DEFAULT_BAR_WIDTH / 2.));
        detailTopCrossBarLine.setEndX(detailBarCenterX + (DEFAULT_BAR_WIDTH / 2.));

        detailBottomCrossBarLine.setStartY(barBottomY);
        detailBottomCrossBarLine.setEndY(barBottomY);
        detailBottomCrossBarLine.setStartX(detailBarCenterX - (DEFAULT_BAR_WIDTH / 2.));
        detailBottomCrossBarLine.setEndX(detailBarCenterX + (DEFAULT_BAR_WIDTH / 2.));
        
        nameText.setText(column.getName());
        if (nameText.getLayoutBounds().getWidth() > bounds.getWidth()) {
            // truncate the column name to fit axis bounds
            while (nameText.getLayoutBounds().getWidth() > bounds.getWidth()) {
                nameText.setText(nameText.getText().substring(0, nameText.getText().length() - 1));
            }
        }

//        nameText.setFont(new Font(DEFAULT_TEXT_SIZE));
//        adjustTextSize(nameText, width, DEFAULT_TEXT_SIZE);

        nameText.setX(bounds.getX() + ((width - nameText.getLayoutBounds().getWidth()) / 2.));
        nameText.setY(bounds.getY() + nameText.getLayoutBounds().getHeight());
        nameText.setRotate(getNameTextRotation());
//        nameText.setY(barTopY - (DEFAULT_NAME_LABEL_HEIGHT / 2.));
//        nameText.setRotate(-10.);

//        overviewStartInstantText.setX(bounds.getX() + ((width - overviewStartInstantText.getLayoutBounds().getWidth()) / 2.));
        overviewStartInstantText.setX(overviewAxisBar.getLayoutBounds().getMaxX() - overviewStartInstantText.getLayoutBounds().getWidth());
        overviewStartInstantText.setY(barBottomY + overviewStartInstantText.getLayoutBounds().getHeight());

//        overviewEndInstantText.setX(bounds.getX() + ((width - overviewEndInstantText.getLayoutBounds().getWidth()) / 2.));
        overviewEndInstantText.setX(overviewAxisBar.getLayoutBounds().getMaxX() - overviewEndInstantText.getLayoutBounds().getWidth());
        overviewEndInstantText.setY(barTopY - 4d);

        detailStartInstantText.setX(detailAxisBar.getX());
        detailStartInstantText.setY(barBottomY + detailStartInstantText.getLayoutBounds().getHeight());

//        overviewEndInstantText.setX(bounds.getX() + ((width - overviewEndInstantText.getLayoutBounds().getWidth()) / 2.));
        detailEndInstantText.setX(detailAxisBar.getX());
        detailEndInstantText.setY(barTopY - 4d);
//        log.debug("nameText x=" + nameText.getX() + " y=" + nameText.getY() + " text: " + nameText.getText());
//        log.debug("overviewStartInstantText x=" + overviewStartInstantText.getX() + " y=" + overviewStartInstantText.getY() + " text: " + overviewStartInstantText.getText());
    }
}
