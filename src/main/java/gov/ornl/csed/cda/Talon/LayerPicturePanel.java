package gov.ornl.csed.cda.Talon;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by whw on 4/4/16.
 */
public class LayerPicturePanel extends JPanel {
    private File directory = null;
    private File[] imageFiles = null;
    private TreeMap<File, BufferedImage> images = new TreeMap<>();
    private int numberOfImages = 0;
    private Dimension originalImageDimensions = new Dimension();
    private double aspectRatio = 0;


    public LayerPicturePanel(File directory) {
        this.directory = directory;
        this.imageFiles = directory.listFiles();
        this.numberOfImages = this.imageFiles.length;

        int count = 0;

        for (File file : imageFiles) {

            if (count == 10) {
                break;
            }

            BufferedImage layerImage = null;

            try {
                layerImage = ImageIO.read(file);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (count == 0) {
                originalImageDimensions.setSize(new Dimension(layerImage.getWidth(), layerImage.getHeight()));
                aspectRatio = originalImageDimensions.getWidth() / (double)originalImageDimensions.getHeight();
            }

            images.put(file, layerImage);
            count++;
        }

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int width = gd.getDisplayMode().getWidth();
        int height = gd.getDisplayMode().getHeight();

        setMaximumSize(new Dimension((int)(width*0.1), Integer.MAX_VALUE));

        setPreferredSize(new Dimension(100, 200*10));
    }

    private static JFrame buildFrame() {
        JFrame frame = new JFrame();

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(200, 200);

        frame.setVisible(true);

        return frame;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int count = 0;

        for (Map.Entry<File, BufferedImage> image : images.entrySet()) {

            if (count >= 10) {
                break;
            }

            g.drawImage(image.getValue(), 0, (int)(this.getWidth()*count), (int)(this.getWidth()), (int)(this.getWidth()), null);
            count++;
        }
    }

    public static void main(String[] args) throws IOException {
        JFrame frame = buildFrame();

        File directory = new File("/Users/whw/ORNL Internship/Printer Log Files/FromServer/R1119_2016-01-22_09.53_20160121_Q10_MDF_ARCAM TEST ARTICLE BUILD 1_1_Images/Image_1/");

        LayerPicturePanel pane = new LayerPicturePanel(directory);

        JScrollPane scrollPane = new JScrollPane(pane);
        scrollPane.createVerticalScrollBar().setUnitIncrement(10);
        System.out.println("Directory: " + pane.directory.toString() + " Contains " + pane.numberOfImages + " imageFiles");

        frame.add(scrollPane);
    }

}
