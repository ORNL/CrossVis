package gov.ornl.csed.cda.timevis;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by csg on 9/23/15.
 */
public class TestFontAwesome {
    public static void main(String[] args) {
        new TestFontAwesome();
    }

    public TestFontAwesome() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                }

                try (InputStream is = TestFontAwesome.class.getResourceAsStream("fontawesome-webfont.ttf")) {
                    Font font = Font.createFont(Font.TRUETYPE_FONT, is);
                    font = font.deriveFont(Font.PLAIN, 24f);

//                    JLabel label = new JLabel("\uf0c0");
                    // fa-trash-o  [&#xf014;]
                    // see font awesome cheatsheet http://fortawesome.github.io/Font-Awesome/cheatsheet/
                    JLabel label = new JLabel("\uf014");
                    label.setFont(font);

                    JFrame frame = new JFrame("Testing");
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setLayout(new GridBagLayout());
                    frame.add(label);
                    frame.pack();
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                } catch (IOException | FontFormatException exp) {
                    exp.printStackTrace();
                }
            }
        });
    }
}
