package gov.ornl.csed.cda.pcpview;

import gov.ornl.csed.cda.datatable.DataModel;
import gov.ornl.csed.cda.datatable.TemporalColumn;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.converter.LocalDateStringConverter;
import javafx.util.converter.LocalDateTimeStringConverter;
import javafx.util.converter.NumberStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.Rectangle2D;

public class PCPTemporalAxis extends PCPAxis {
    public final static Logger log = LoggerFactory.getLogger(PCPTemporalAxis.class);

    public final static Color DEFAULT_LABEL_COLOR = Color.BLACK;
    public final static double DEFAULT_NAME_LABEL_HEIGHT = 30d;
    public final static double DEFAULT_NAME_TEXT_SIZE = 12d;
    public final static double DEFAULT_CONTEXT_HEIGHT = 20d;
    public final static double DEFAULT_BAR_WIDTH = 10d;
    public final static double DEFAULT_TEXT_SIZE = 10d;
    public final static double DEFAULT_STROKE_WIDTH = 1.5;

//    private TemporalColumn column;

    private double barTopY;
    private double barBottomY;

    private Rectangle overviewAxisBar;
    private Line overViewTopCrossBarLine;
    private Line overviewBottomCrossBarLine;
    private Rectangle detailAxisBar;
    private Line detailTopCrossBarLine;
    private Line detailBottomCrossBarLine;

    private Text startInstantText;
    private Text endInstantText;

    public PCPTemporalAxis(PCPView pcpView, TemporalColumn column, DataModel dataModel, Pane pane) {
        super(pcpView, column, dataModel, pane);

        this.pcpView = pcpView;
        this.column = column;
        this.dataModel = dataModel;
        this.pane = pane;

        barTopY = 0d;
        barBottomY = 0d;

        startInstantText = new Text();
        startInstantText.textProperty().bindBidirectional(temporalColumn().startLocalDateTimeProperty(), new LocalDateTimeStringConverter());
        startInstantText.setFont(new Font(DEFAULT_TEXT_SIZE));
        startInstantText.setSmooth(true);

        endInstantText = new Text();
        endInstantText.textProperty().bindBidirectional(column.endLocalDateTimeProperty(), new LocalDateTimeStringConverter());
        endInstantText.setFont(new Font(DEFAULT_TEXT_SIZE));
        endInstantText.setSmooth(true);

        overviewAxisBar = new Rectangle();
        overviewAxisBar.setStroke(Color.DARKGRAY);
        overviewAxisBar.setFill(Color.WHITESMOKE);
        overviewAxisBar.setSmooth(true);
        overviewAxisBar.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        overViewTopCrossBarLine = makeLine();
        overviewBottomCrossBarLine = makeLine();

        graphicsGroup.getChildren().addAll(overviewAxisBar, overViewTopCrossBarLine, overviewBottomCrossBarLine,
                startInstantText, endInstantText);

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
        barBottomY = bounds.getY() + bounds.getHeight() - endInstantText.getLayoutBounds().getHeight();

        overviewAxisBar.setX(centerX - (DEFAULT_BAR_WIDTH / 2d));
        overviewAxisBar.setY(barTopY);
        overviewAxisBar.setWidth(DEFAULT_BAR_WIDTH);
        overviewAxisBar.setHeight(barBottomY - barTopY);

        overViewTopCrossBarLine.setStartY(barTopY);
        overViewTopCrossBarLine.setEndY(barTopY);
        overViewTopCrossBarLine.setStartX(centerX - (DEFAULT_BAR_WIDTH / 2.));
        overViewTopCrossBarLine.setEndX(centerX + (DEFAULT_BAR_WIDTH / 2.));

        overviewBottomCrossBarLine.setStartY(barBottomY);
        overviewBottomCrossBarLine.setEndY(barBottomY);
        overviewBottomCrossBarLine.setStartX(centerX - (DEFAULT_BAR_WIDTH / 2.));
        overviewBottomCrossBarLine.setEndX(centerX + (DEFAULT_BAR_WIDTH / 2.));

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

        startInstantText.setX(bounds.getX() + ((width - startInstantText.getLayoutBounds().getWidth()) / 2.));
        startInstantText.setY(barBottomY + startInstantText.getLayoutBounds().getHeight());

        endInstantText.setX(bounds.getX() + ((width - endInstantText.getLayoutBounds().getWidth()) / 2.));
        endInstantText.setY(barTopY - 4d);

        log.debug("nameText x=" + nameText.getX() + " y=" + nameText.getY() + " text: " + nameText.getText());
        log.debug("startInstantText x=" + startInstantText.getX() + " y=" + startInstantText.getY() + " text: " + startInstantText.getText());
    }
}
