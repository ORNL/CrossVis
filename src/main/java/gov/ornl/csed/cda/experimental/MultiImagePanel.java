package gov.ornl.csed.cda.experimental;

import gov.ornl.csed.cda.timevis.TimeSeriesPanel;
import javafx.geometry.Orientation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by csg on 4/5/16.
 */
public class MultiImagePanel extends JComponent implements ComponentListener {
    private final static Logger log = LoggerFactory.getLogger(MultiImagePanel.class);


    private Orientation orientation;
    private TreeMap<Double, ImageInfo> imageInfoMap = new TreeMap<>();
    private HashMap<ImageInfo, BufferedImage> imageCacheMap = new HashMap<>();
    private int maxImageCacheSize = 10;

    private int imageSpacing = 4;
    private Insets margins = new Insets(2,2,2,2);

    public MultiImagePanel(Orientation orientation) {
        this.orientation = orientation;
        addComponentListener(this);
    }

    public void setImageFileInfo(ArrayList<File> files, ArrayList<Double> heightValues, ArrayList<Dimension> dimensions) {
        imageInfoMap.clear();
        for (int i = 0; i < files.size(); i++) {
            ImageInfo info = new ImageInfo();
            info.heightValue = heightValues.get(i);
            info.image = null;
            info.screenRect = null;
            info.file = files.get(i);
            info.imageDimension = dimensions.get(i);
            info.aspectRatio = (double)info.imageDimension.width / info.imageDimension.height;
            imageInfoMap.put(info.heightValue, info);
        }

        layoutComponent();
        repaint();
    }

    private void layoutComponent() {
        if (!imageInfoMap.isEmpty()) {
            Dimension panelDimension = new Dimension();

            if (orientation == Orientation.VERTICAL) {
                panelDimension.width = getWidth() - 1;


                int x = 0;
                int y = 0;
                for (ImageInfo info : imageInfoMap.values()) {
                    int width = (info.imageDimension.width < panelDimension.width) ? info.imageDimension.width : panelDimension.width;
                    int height = (int) (width / info.aspectRatio);
//                    double height = (double)(info.imageDimension.height * width) / info.imageDimension.width;
//                    info.screenRect = new Rectangle2D.Double(x, y, width, height);
                    info.screenRect = new Rectangle(x, y, width, height);
//                    log.debug(info.screenRect.toString());
                    y += height + imageSpacing;
                    log.debug("width: " + width + "height: " + height + " aspect ratio: " + info.aspectRatio);
                }

                panelDimension.height = y - imageSpacing;
            }

            log.debug("panel dimension:  " + panelDimension.toString());
            setPreferredSize(panelDimension);
        }
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());

        Rectangle clipRect = g2.getClipBounds();

        if (!imageInfoMap.isEmpty()) {
            g2.setColor(Color.blue);
            for (ImageInfo info : imageInfoMap.values()) {
                if (info.screenRect.intersects(clipRect)) {
                    BufferedImage image = imageCacheMap.get(info);
                    if (image == null) {
                        try {
                            image = ImageIO.read(info.file);
                            imageCacheMap.put(info, image);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // we need to get the info object for this image out of the cache queue
                        // and re-add it (this essentially helps us keep track of the order of last
                        // reading each image so that we can remove the older image when we need to)

                    }
                    g2.drawImage(image, info.screenRect.x, info.screenRect.y, info.screenRect.width, info.screenRect.height, this);
                    g2.draw(info.screenRect);
                }
            }
        }
    }

    public static void main (String args[]) {
        // read images
        File imageDirectory = new File("/Users/csg/Desktop/TestAMImages");
        ArrayList<File> imageFiles = new ArrayList<>();
        ArrayList<Double> heightValues = new ArrayList<>();
        ArrayList<Dimension> imageDimensions = new ArrayList<>();

        File[] files = imageDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith(".png")) {
                    return true;
                }
                return false;
            }
        });

        String prefix = "Layer";
        String postfix = "Image";

        log.debug("Scanning " + files.length + " image files from " + imageDirectory.getAbsolutePath());
        for (File imageFile : files) {
            try {
                BufferedImage image = ImageIO.read(imageFile);
                imageFiles.add(imageFile);
                Dimension dimension = new Dimension(image.getWidth(), image.getHeight());
                imageDimensions.add(dimension);
                // parse build height value from file name
                String heightString = imageFile.getName().substring(prefix.length(), imageFile.getName().indexOf(postfix));
                heightValues.add(Double.valueOf(heightString));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        log.debug("Finished Reading files");

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                MultiImagePanel imagePanel = new MultiImagePanel(Orientation.VERTICAL);
                imagePanel.setImageFileInfo(imageFiles, heightValues, imageDimensions);

                JScrollPane scrollPane = new JScrollPane(imagePanel);
                scrollPane.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        int height = imagePanel.getPreferredSize().height;
                        int width = scrollPane.getWidth();
                        imagePanel.setPreferredSize(new Dimension(width, height));
                        log.debug("image panel preferred size: " + imagePanel.getPreferredSize().toString());
//                        super.componentResized(e);
                    }
                });

                ((JPanel)frame.getContentPane()).setLayout(new BorderLayout());
                ((JPanel)frame.getContentPane()).add(scrollPane, BorderLayout.CENTER);

                frame.setSize(new Dimension(200, 600));
                frame.setVisible(true);
            }
        });
    }

    @Override
    public void componentResized(ComponentEvent e) {
        layoutComponent();
        repaint();
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }

    private class ImageInfo {
        public Image image;
        public File file;
        public Dimension imageDimension;
        public double aspectRatio; // width / height
        public Rectangle screenRect;
        public double heightValue;
    }
}
