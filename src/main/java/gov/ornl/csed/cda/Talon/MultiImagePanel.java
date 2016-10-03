/*
 *
 *  Class:  MultiImagePanel
 *
 *      Author:     whw
 *                  csg
 *
 *      Created:    26 Feb 2016
 *
 *      Purpose:    [A description of why this class exists.  For what
 *                  reason was it written?  Which jobs does it perform?]
 *
 *
 *  Inherits From:  JComponent
 *
 *  Interfaces:     ComponentListener,
 *                  MouseMotionListener,
 *                  TalonDataListener
 *
 */

package gov.ornl.csed.cda.Talon;



import javafx.geometry.Orientation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


public class MultiImagePanel extends JComponent implements ComponentListener, MouseMotionListener, TalonDataListener {





    // =-= CLASS FIELDS =-=
    private final static Logger log = LoggerFactory.getLogger(MultiImagePanel.class);





    // =-= INSTANCE FIELDS =-=
    private TalonData data = null;
    private Orientation orientation;
    private TreeMap<Double, ImageInfo> imageInfoMap = new TreeMap<>();
    private HashMap<ImageInfo, BufferedImage> imageCacheMap = new HashMap<>();
    private HashMap<ImageInfo, ImageZoomPanel> izpCacheMap = new HashMap<>();
    private TreeMap<Long, ImageInfo> imageCacheTimeMap = new TreeMap<>();
    private int maxImageCacheSize = 30;
    private int imageSpacing = 4;
    private Insets margins = new Insets(2,2,2,2);


    // need to wrap every IZP in a scroll pane
    private ImageZoomPanel[] imageZoomArray = null;
    private JScrollPane[] imageScrollPaneArray = null;




    // =-= CONSTRUCTOR =-=
    public MultiImagePanel(Orientation orientation, TalonData data) {
        this.orientation = orientation;
        this.data = data;

        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        addComponentListener(this);
        addMouseMotionListener(this);
        data.addTalonDataListener(this);
    }





    // =-= INSTANCE METHODS =-=
    public void setImageFileInfo(ArrayList<File> files, ArrayList<Double> heightValues, ArrayList<Dimension> dimensions) {
        imageInfoMap.clear();

        imageZoomArray = new ImageZoomPanel[files.size()];
        imageScrollPaneArray = new JScrollPane[files.size()];

        this.removeAll();

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
//        repaint();
    }

    // !! not sure I will have to do any of this
    public void layoutComponent() {
        if (!imageInfoMap.isEmpty()) {

            // clear caches
            imageCacheMap.clear();
            imageCacheTimeMap.clear();

            // initialize new panel dimensions
            Dimension panelDimension = new Dimension();

            if (orientation == Orientation.VERTICAL) {

                // get the current width of the MultiImagePanel
                panelDimension.width = getWidth() - 1;

                // initialize x, y for first image location
                // !! not sure i will have to do this
                int x = 0;
                int y = 0;
                int i = 0;
                for (ImageInfo info : imageInfoMap.values()) {

                    int width = (info.imageDimension.width < panelDimension.width) ? info.imageDimension.width : panelDimension.width;
                    int height = (int) (width / info.aspectRatio);
//                    double height = (double)(info.imageDimension.height * width) / info.imageDimension.width;
                    info.screenRect = new Rectangle(x, y, width, height);

                    y += height + imageSpacing;
                }

                panelDimension.height = y - imageSpacing;

                // Reverses the order of the images by "inverting" the y value of each image
                for (ImageInfo info : imageInfoMap.values()) {
                    info.screenRect = new Rectangle(info.screenRect.x, panelDimension.height - info.screenRect.y - info.screenRect.height, info.screenRect.width, info.screenRect.height);
                }
            }

//            log.debug("Panel dimension:  " + panelDimension.toString());
            setPreferredSize(panelDimension);

            System.out.println(this.getWidth());
            System.out.println(this.getPreferredSize().getWidth());
            System.out.println(panelDimension.getWidth());

            for (int i = 0; i < imageScrollPaneArray.length; i++) {
                imageZoomArray[i] = new ImageZoomPanel();
                imageZoomArray[i].setMinimumSize(new Dimension(2 * this.getWidth(), 2 * this.getWidth()));

                imageScrollPaneArray[i] = new JScrollPane(imageZoomArray[i]);
                imageScrollPaneArray[i].setMinimumSize(new Dimension(this.getWidth(), this.getWidth()));

                this.add(imageScrollPaneArray[i]);
            }

        }
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
//        g2.setColor(getBackground());
//        g2.fillRect(0, 0, getWidth(), getHeight());

        Rectangle clipRect = g2.getClipBounds();

        if (!imageInfoMap.isEmpty()) {
//            g2.setColor(Color.blue);

            // for all images in the directory
            int i = 0;
            for (ImageInfo info : imageInfoMap.values()) {
//            for (int i = 0; i < imageScrollPaneArray.length; i++) {

                // if this image intersects the screen boundaries
//                if (info.screenRect.intersects(clipRect)) {
                if (imageScrollPaneArray[i].getBounds().intersects(clipRect)) {

                    // retrieve each image to be drawn [try from cache first]
                    BufferedImage image = imageCacheMap.get(info);

                    // if image is not in the cache
                    if (image == null) {

                        // try reading it from disk
                        try {

                            image = ImageIO.read(info.file);
                            imageZoomArray[i].setImage(image);

                            // if the cache is full
                            if (imageCacheMap.size() == maxImageCacheSize) {

                                // delete image and corresponding info
                                ImageInfo infoToDelete = imageCacheTimeMap.pollFirstEntry().getValue();
                                imageCacheMap.remove(infoToDelete);

                                // remove the image from the IZP
                                izpCacheMap.get(infoToDelete).deleteImage();
                                izpCacheMap.remove(infoToDelete);
                            }

                            // insert current image into cache and entry into time stamp cache
                            info.lastAccessTime = System.nanoTime();
                            imageCacheMap.put(info, image);
                            izpCacheMap.put(info, imageZoomArray[i]);

                            imageCacheTimeMap.put(info.lastAccessTime, info);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    // if the image DOES exist in the cache
                    } else {

                        // we need to get the info object for this image out of the cache queue
                        // and re-add it (this essentially helps us keep track of the order of last
                        // reading each image so that we can remove the older image when we need to)
                        imageCacheTimeMap.remove(info.lastAccessTime);
                        info.lastAccessTime = System.nanoTime();
                        imageCacheTimeMap.put(info.lastAccessTime, info);
                    }

//                    g2.drawImage(image, info.screenRect.x, info.screenRect.y, info.screenRect.width, info.screenRect.height, this);
//                    g2.draw(info.screenRect);
                    imageZoomArray[i].repaint();
                    imageScrollPaneArray[i].revalidate();
                }

                i++;
            }
        }
    }


    // ComponentListener methods
    @Override
    public void componentResized(ComponentEvent e) {
//        layoutComponent();
//        repaint();
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


    // MouseMotionListener methods
    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        setToolTipText("");

        for (Map.Entry<Double, ImageInfo> image  : imageInfoMap.entrySet()) {

            if (image.getValue().screenRect.contains(e.getPoint())) {
                setToolTipText("Build Height: " + image.getKey());
                break;
            }
        }
    }


    // TalonDataListener methods
    @Override
    public void TalonDataPlgFileChange() {
//        log.debug("PLG File Change");
    }

    @Override
    public void TalonDataSegmentingVariableChange() {
//        log.debug("Segmenting Variable Change");
    }

    @Override
    public void TalonDataSegmentedVariableChange() {
//        log.debug("Segmented Variable Change");
    }

    @Override
    public void TalonDataReferenceValueChange() {
//        log.debug("Reference Value Change");
    }

    @Override
    public void TalonDataImageDirectoryChange() {
//        log.debug("Image Directory Change");
        setImageFileInfo(data.getImageFiles(), data.getHeightValues(), data.getImageDimensions());
        revalidate();
    }


    // will make the tooltip follow the cursor when hovering over images if desired
//    @Override
//    public Point getToolTipLocation(MouseEvent event) {
//        Point pt = new Point(event.getX(), event.getY());
//        return pt;
//    }





    // =-= NESTED CLASS =-=
    private class ImageInfo {





        // =-= INSTANCE FIELDS =-=
        public Image image;
        public File file;
        public Dimension imageDimension;
        public double aspectRatio; // width / height
        public Rectangle screenRect;
        public double heightValue;
        public long lastAccessTime;
    }





    // =-= MAIN =-=
    public static void main (String args[]) {

        // read images
        File imageDirectory = new File("/Volumes/home/RemoteStorage/Data/07.28.16/subset");
//        File imageDirectory = new File("/Users/csg/Desktop/TestAMImages");
//        File imageDirectory = new File("/Users/csg/Desktop/AM_data/R1140_2015-01-30_15.06/R1140_2015-01-30_15.06_Images/Image_2");

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                TalonData talonData = new TalonData(ChronoUnit.SECONDS);

                MultiImagePanel imagePanel = new MultiImagePanel(Orientation.VERTICAL, talonData);
//                imagePanel.setImageFileInfo(imageFiles, heightValues, imageDimensions);

                imagePanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

                JScrollPane scrollPane = new JScrollPane(imagePanel);
//                scrollPane.addComponentListener(new ComponentAdapter() {
//                    @Override
//                    public void componentResized(ComponentEvent e) {
//                        int height = imagePanel.getPreferredSize().height;
//                        int width = scrollPane.getWidth();
//                        imagePanel.setPreferredSize(new Dimension(width, height));
//                        log.debug("image Panel preferred size: " + imagePanel.getPreferredSize().toString());
////                        super.componentResized(e);
//                    }
//                });

                ((JPanel)frame.getContentPane()).setLayout(new BorderLayout());
                ((JPanel)frame.getContentPane()).add(scrollPane, BorderLayout.CENTER);

                frame.setSize(new Dimension(600, 600));

                frame.setVisible(true);

                talonData.setImageDirectory(imageDirectory);
            }
        });
    }


    public class ImageZoomPanel extends JComponent {

        private final Double ZOOM_DEFAULT = 0.5;

        private Double zoom = ZOOM_DEFAULT;
        private Double percentage = 0.01;

        private BufferedImage image = null;

        public ImageZoomPanel(BufferedImage image) {
            this.image = image;
            layoutComponent();
        }

        public ImageZoomPanel() {

        }

        public ImageZoomPanel(Double zoom) {
            this.zoom = zoom;
            layoutComponent();
        }

        public ImageZoomPanel(BufferedImage image, Double zoom) {
            this.image = image;
            this.zoom = zoom;
        }

        public void layoutComponent() {
            if (this.isVisible() && this.image != null) {
                this.setPreferredSize(new Dimension((int) (image.getWidth() * zoom), (int) (image.getHeight() * zoom)));
                repaint();
            }
        }

        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.scale(zoom, zoom);

            g2.drawImage(image, 0, 0, this);
        }

        public Double getZoom() {
            return zoom;
        }

        public void setImage(BufferedImage image) {
            this.image = image;
        }

        public void deleteImage() {
            this.image = null;
        }

        public void originalSize() {
            zoom = ZOOM_DEFAULT;
        }

        public void zoomIn() {
            zoom += percentage;
        }

        public void zoomOut() {
            zoom -= percentage;

            if (zoom < percentage) {
                if (percentage > 1.0) {
                    zoom = 1.0;
                } else {
                    zoomIn();
                }
            }
        }

    }

}
