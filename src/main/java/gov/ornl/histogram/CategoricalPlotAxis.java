package gov.ornl.histogram;

import gov.ornl.util.GraphicsUtil;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Collection;

public class CategoricalPlotAxis extends PlotAxis {
    private ArrayList<String> categories = new ArrayList<>();
    private ArrayList<Text> categoryTextList = new ArrayList<>();

    public CategoricalPlotAxis(Orientation orientation, Collection<String> categories) {
        super(orientation);

        this.categories.addAll(categories);
        initialize();
    }

    private void initialize() {
        categories.forEach(category -> {
            Text categoryText = new Text(category);
            categoryText.setFont(Font.font(fontSize));
            if (orientation == Orientation.HORIZONTAL) {
                categoryText.setTextOrigin(VPos.TOP);
            } else {
                categoryText.setTextOrigin(VPos.CENTER);
            }
            categoryTextList.add(categoryText);
        });
        graphicsGroup.getChildren().addAll(categoryTextList);
    }

    @Override
    public void layout(Bounds bounds) {
        super.layout(bounds);

        if (orientation == Orientation.HORIZONTAL) {
            double categoryTickSpacing = bounds.getWidth() / categories.size();
            double left = categoryTickSpacing / 2.;

            for (int i = 0; i < categoryTextList.size(); i++) {
                if (categoryTextList.get(i).getLayoutBounds().getWidth() > bounds.getWidth()) {
                    GraphicsUtil.adjustTextSize(categoryTextList.get(i), bounds.getWidth(), 10.);
                }
                double x = (left + left + (i * categoryTickSpacing)) - (categoryTextList.get(i).getLayoutBounds().getWidth() / 2.);
                categoryTextList.get(i).setX(x);
                categoryTextList.get(i).setY(bounds.getMinY() + 1);
            }
        } else {
            double categoryTickSpacing = bounds.getHeight() / categories.size();
            double bottom = bounds.getMaxY() - categoryTickSpacing / 2.;

            for (int i = 0; i < categoryTextList.size(); i++) {
                if (categoryTextList.get(i).getLayoutBounds().getWidth() > bounds.getWidth()) {
                    GraphicsUtil.adjustTextSize(categoryTextList.get(i), bounds.getWidth(), 10.);
                }
                categoryTextList.get(i).setX(bounds.getMaxX() - (2 + categoryTextList.get(i).getLayoutBounds().getWidth()));
                categoryTextList.get(i).setY(bottom - (i * categoryTickSpacing));
            }
        }
    }
}
