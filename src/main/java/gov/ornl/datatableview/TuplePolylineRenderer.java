package gov.ornl.datatableview;

import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

/**
 * Created by csg on 10/21/16.
 */
public class TuplePolylineRenderer extends AnimationTimer {
    public final static Logger log = Logger.getLogger(TuplePolylineRenderer.class.getName());

    private Canvas canvas;
    private Color tupleColor;
    private ArrayBlockingQueue<TuplePolyline> tupleQueue;
    private int maxTuplesPerFrame;
    private ArrayList<Axis> axisList;
    private BooleanProperty running;
    public long id;

    public TuplePolylineRenderer(Canvas canvas, Collection<TuplePolyline> tuples, ArrayList<Axis> axisList,
                                 Color tupleColor, int maxTuplesPerFrame) {
        id = System.currentTimeMillis();
        this.canvas = canvas;
        this.axisList = axisList;
        this.tupleColor = tupleColor;
        tupleQueue = new ArrayBlockingQueue<TuplePolyline>(tuples.size());
        tupleQueue.addAll(tuples);
        this.maxTuplesPerFrame = maxTuplesPerFrame;
        running = new SimpleBooleanProperty(false);
    }

    public final boolean isRunning() { return running.get(); }

    public ReadOnlyBooleanProperty runningProperty() { return running; }

    @Override
    public void handle(long now) {
        canvas.getGraphicsContext2D().setStroke(tupleColor);

        for (int ituple = 0; ituple < maxTuplesPerFrame; ituple++) {
            TuplePolyline pcpTuple = tupleQueue.poll();

            if (pcpTuple == null) {
                this.stop();
                break;
            } else {
//                if (pcpTuple.isInContext()) {
//                    canvas.getGraphicsContext2D().setStroke(tupleColor.desaturate());
//                } else {
//                    canvas.getGraphicsContext2D().setStroke(tupleColor);
//                }
                for (int i = 1; i < pcpTuple.getXPoints().length; i++) {
                    double x0, x1;

                    if (axisList.get(i-1) instanceof BivariateAxis) {
                        x0 = ((BivariateAxis)axisList.get(i-1)).getScatterplot().plotBounds.getMaxX();
                    } else {
                        x0 = ((UnivariateAxis)axisList.get(i-1)).getBarRightX();
                    }

                    if (axisList.get(i) instanceof BivariateAxis) {
                        x1 = ((BivariateAxis)axisList.get(i)).getScatterplot().plotBounds.getMinX();
                    } else {
                        x1 = ((UnivariateAxis)axisList.get(i)).getBarLeftX();
                    }

                    canvas.getGraphicsContext2D().strokeLine(x0, pcpTuple.getYPoints()[i - 1],
                            x1, pcpTuple.getYPoints()[i]);
//                    canvas.getGraphicsContext2D().strokeLine(axisList.get(i - 1).getBarRightX(), pcpTuple.getYPoints()[i - 1],
//                            axisList.get(i).getBarLeftX(), pcpTuple.getYPoints()[i]);
//                    canvas.getGraphicsContext2D().strokeLine(pcpTuple.getXPoints()[i-1], pcpTuple.getYPoints()[i - 1],
//                            pcpTuple.getXPoints()[i], pcpTuple.getYPoints()[i]);
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
