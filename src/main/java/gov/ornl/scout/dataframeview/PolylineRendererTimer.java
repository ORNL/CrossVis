package gov.ornl.scout.dataframeview;

import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Orientation;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

public class PolylineRendererTimer extends AnimationTimer {
    public final static Logger log = Logger.getLogger(PolylineRendererTimer.class.getName());

    private Canvas canvas;
    private Color color;
    private ArrayBlockingQueue<RowPolyline> polylineQueue;
    private int maxPolylinesPerFrame;
    private ArrayList<Axis> axisList;
    private Orientation orientation;
    private BooleanProperty running;
    public long id;

    public PolylineRendererTimer (Canvas canvas, Collection<RowPolyline> polylines, ArrayList<Axis> axisList,
                                  Color polylineColor, int maxPolylinesPerFrame, Orientation orientation) {
        id = System.currentTimeMillis();
        this.canvas = canvas;
        this.orientation = orientation;
        this.axisList = axisList;
        this.color = polylineColor;
        polylineQueue = new ArrayBlockingQueue<>(polylines.size());
        polylineQueue.addAll(polylines);
        this.maxPolylinesPerFrame = maxPolylinesPerFrame;
        running = new SimpleBooleanProperty(false);
    }

    public final boolean isRunning() { return running.get(); }

    public ReadOnlyBooleanProperty runningProperty() { return running; }

    @Override
    public void handle(long now) {
        canvas.getGraphicsContext2D().setStroke(color);

        for (int ipolyline = 0; ipolyline < maxPolylinesPerFrame; ipolyline++) {
            RowPolyline polyline = polylineQueue.poll();
            if (polyline == null) {
                this.stop();
                break;
            } else {
                for (int i = 1; i < polyline.getRow().length; i++) {
                    if (orientation == Orientation.HORIZONTAL) {
                        canvas.getGraphicsContext2D().strokeLine(axisList.get(i-1).getAxisBarRight(), polyline.getyValues()[i - 1],
                                axisList.get(i).getAxisBarLeft(), polyline.getyValues()[i]);
                    } else {
                        canvas.getGraphicsContext2D().strokeLine(polyline.getXValues()[i-1], axisList.get(i-1).getAxisBarBottom(),
                                polyline.getXValues()[i], axisList.get(i).getAxisBarTop());
                    }
                }
            }
        }
    }

    @Override
    public void start() {
        super.start();
        running.set(true);
    }

    @Override
    public void stop() {
        super.stop();
        running.set(false);
    }
}
