package gov.ornl.csed.cda.histogram;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * Created by csg on 1/5/16.
 */
public class Test extends JComponent implements ComponentListener {

    public Test () {
        addComponentListener(this);
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;

        g2.setColor(Color.blue);
        g2.drawRect(5, 5, getWidth()-10, getHeight() - 10);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        System.out.println("Component Resized");
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        System.out.println("Component Moved");
    }

    @Override
    public void componentShown(ComponentEvent e) {
        System.out.println("Component Shown");
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        System.out.println("Component Hidden");
    }

    public static void main (String args[]) {
        JFrame frame = new JFrame();
        frame.setSize(new Dimension(500, 500));

        JPanel panel = (JPanel)frame.getContentPane();
        panel.setLayout(new BorderLayout());

        panel.add(new Test(), BorderLayout.CENTER);

        frame.setVisible(true);
    }
}
