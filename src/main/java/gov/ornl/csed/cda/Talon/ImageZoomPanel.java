package gov.ornl.csed.cda.Talon;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;

import static javafx.application.Application.launch;

/**
 * Created by whw on 9/1/16.
 */

/*
    guys.. what we need is a class that can display a single image within a defined boudary

    CONSIDERATIONS
    - also when multiple ones of them are grouped together in the program the desired behavior is that they
        - all display the image at the same size
        - all display the same portion of a single image
        - the image may resized underneath the panel
 */

public class ImageZoomPanel extends JComponent {

    private static Rectangle panelRect = null;
    private static Rectangle imageRect = null;

    private Image image = null;
    private Double imageValue = null;

    public static Dimension getPanelDimension() {
        return new Dimension(panelRect.width, panelRect.height);
    }

    public static void setPanelDimension(Dimension dimension) {
        panelRect.width = dimension.width;
        panelRect.height = dimension.height;
    }

    public static Dimension getImageDimension() {
        return new Dimension(imageRect.width, imageRect.height);
    }

    public static void setImageDimension(Dimension dimension) {
        imageRect.width = dimension.width;
        imageRect.height = dimension.height;
    }

    public ImageZoomPanel(Image image, Double imageValue) {
        this.image = image;
        this.imageValue = imageValue;

        if (imageRect == null) {
            imageRect = new Rectangle(0, 0, 0, 0);
        }
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;

        g2.drawImage(image, imageRect.x, imageRect.y, imageRect.width, imageRect.height, this);
    }
}
