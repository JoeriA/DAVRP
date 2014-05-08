import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

/**
 * Created by Joeri on 8-5-2014.
 */
public class Window extends JFrame {

    private JPanel contentPane;
    private JLabel lblPicture;
    private Dimension imgDim = new Dimension(500, 500);

    /**
     * Create a window to display output
     */
    public Window() {
        pack();
        setExtendedState(Window.MAXIMIZED_BOTH);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        lblPicture = new JLabel();
        lblPicture.setBounds(1, 1, 500, 500);
        contentPane.add(lblPicture);

        setVisible(true);
    }

    /**
     * Create a map of the customers
     *
     * @param dataSet dataset containing info about customers
     */
    public void createMap(DataSet dataSet) {

        // Create graphics
        BufferedImage image = new BufferedImage(imgDim.width, imgDim.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setBackground(Color.WHITE);
        g2d.fillRect(0, 0, imgDim.width, imgDim.height);
        g2d.setColor(Color.BLACK);
        Customer[] customers = dataSet.getCustomers();
        double x, y;
        int size = 6;
        // Draw depot
        x = customers[0].getxCoordinate();
        y = customers[0].getyCoordinate();
        g2d.fillRect(transformX(x) - (int) (size / 2.0), transformY(y) - (int) (size / 2.0), size, size);
        //TODO add label
        // Draw customers
        for (int i = 1; i < customers.length; i++) {
            x = customers[i].getxCoordinate();
            y = customers[i].getyCoordinate();
            g2d.fillOval(transformX(x) - (int) (size / 2.0), transformY(y) - (int) (size / 2.0), size, size);
            //TODO add label
        }

        // Flip the image vertically (in Java the coordinate (0,0) is the top left corner)
        AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
        tx.translate(0, -image.getHeight(null));
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        image = op.filter(image, null);

        // Print the image on screen
        ImageIcon icon = new ImageIcon(image);
        lblPicture.setIcon(icon);
        contentPane.repaint();

    }

    /**
     * Transform x-coordinate to screen coordinate
     *
     * @param x x-coordinate
     * @return transformed x-coordinate
     */
    private int transformX(double x) {
        // Make all coordinates positive
        x += 2.5;
        // Scale coordinates to screensize
        x *= imgDim.getWidth() / 5.0;
        // Round off
        return (int) Math.round(x);
    }

    /**
     * Transform y-coordinate to screen coordinate
     *
     * @param y y-coordinate
     * @return transformed y-coordinate
     */
    private int transformY(double y) {
        // Make all coordinates positive
        y += 2.5;
        // Scale coordinates to screensize
        y *= imgDim.getHeight() / 5.0;
        // Round off
        return (int) Math.round(y);
    }
}