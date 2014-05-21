import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Created by Joeri on 13-5-2014.
 */
public class Frame extends JFrame implements ChangeListener {

    private JPanel mainPanel;
    private JLabel mapLabel;
    private JTextArea descriptionTextArea;
    private JSpinner spinner1;
    private JLabel titleLabel;
    private JLabel subTitleLabel;
    private JTextArea solverTextArea;
    private JTextArea programStatusTextArea;

    private Dimension imgDim, screenSize, mapDim;
    private BufferedImage mapImg, assignmentsImg, routesImg;
    private DataSet dataSet;
    private Solution solution;

    public Frame() {
        setContentPane(mainPanel);
        setExtendedState(Window.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Calculate dimensions
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int dimensionSize = Math.min((int) screenSize.getHeight() - 80, (int) screenSize.getWidth() - 10);
        imgDim = new Dimension(dimensionSize, dimensionSize);
        mapDim = new Dimension(5, 5);

        // Create graphics
        mapImg = new BufferedImage(imgDim.width, imgDim.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = mapImg.createGraphics();

        // Improve quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setBackground(Color.white);
        g2d.fillRect(0, 0, imgDim.width, imgDim.height);

        setVisible(true);

        spinner1.addChangeListener(this);
    }

    /**
     * Create a mapImg of the customers
     *
     * @param dataSet dataset containing info about customers
     */
    public void createMap(DataSet dataSet) {

        // Make dataset global
        this.dataSet = dataSet;

        // Add title to window
        setTitle(dataSet.getInstance());
        // Add title to frame
        titleLabel.setText(dataSet.getInstance());
        // Add description of data set
        String description = "Number of customers: \t" + dataSet.getNumberOfCustomers();
        description += "\r\nVehicle capacity: \t" + dataSet.getVehicleCapacity();
        description += "\r\nAlpha: \t\t" + dataSet.getAlpha();
        description += "\r\nNumber of scenarios: \t" + dataSet.getNumberOfScenarios();
        descriptionTextArea.setText(description);

        // Create graphics
        Graphics2D g2d = mapImg.createGraphics();
        g2d.setColor(Color.black);
        // Improve quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        Customer[] customers = dataSet.getCustomers();
        int x, y;
        int size = 6;
        // Draw depot
        x = transformX(customers[0].getxCoordinate());
        y = transformY(customers[0].getyCoordinate());
        g2d.fillRect(x - (int) (size / 2.0), y - (int) (size / 2.0), size, size);
        g2d.drawString("D", x + size, y + size);
        // Draw customers
        for (int i = 1; i < customers.length; i++) {
            x = transformX(customers[i].getxCoordinate());
            y = transformY(customers[i].getyCoordinate());
            g2d.fillOval(x - (int) (size / 2.0), y - (int) (size / 2.0), size, size);
            g2d.drawString("" + customers[i].getId(), x + size, y + size);
        }

        // Print the mapImg on screen
        ImageIcon icon = new ImageIcon(mapImg);
        mapLabel.setIcon(icon);
        mapLabel.setText("");
        mainPanel.repaint();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        drawScenario((Integer) spinner1.getValue() - 1);
    }

    public void drawResults(Solution solution) {
        this.solution = solution;

        // Add subtitle
        subTitleLabel.setText("Solved with " + solution.getName());

        // Add description of data set
        String description = "Objective value: \t" + solution.getObjectiveValue();
        description += "\r\nRuntime: \t\t" + solution.getRunTime();
        description += "\r\nGap: \t\t" + solution.getGap();
        solverTextArea.setText(description);

        // Add scenario selector
        SpinnerNumberModel scenariosList = new SpinnerNumberModel(1, 1, dataSet.getNumberOfScenarios(), 1);
        spinner1.setModel(scenariosList);

        // Print all customers assigned to the same driver with the same color
        drawAssignments();

        drawScenario(0);
    }

    private void drawAssignments() {

        // TODO check if assignments are made with boolean
        // Create image
        assignmentsImg = new BufferedImage(imgDim.width, imgDim.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = assignmentsImg.createGraphics();
        g2d.setColor(Color.darkGray);

        double[][] z = null;
        boolean clusters = false;
        boolean drivers = false;

        int m = 0;

        // Check whether clustering is done exactly or with heuristic
        if (solution.getzSol() != null) {
            z = solution.getzSol();
            clusters = true;
            m = dataSet.getNumberOfCustomers() + 1;
        } else if (solution.getaSol() != null) {
            z = solution.getaSol();
            drivers = true;
            m = dataSet.getNumberOfVehicles();
        }

        if (z != null) {

            int n = dataSet.getNumberOfCustomers() + 1;

            Customer[] customers = dataSet.getCustomers();

            int x1, y1, x2, y2;

            int size = 8;

            Random generator = new Random();

            // Add lines on driven routes
            for (int j = 0; j < m; j++) {
                g2d.setColor(new Color(generator.nextInt(256), generator.nextInt(256), generator.nextInt(256)));
                for (int i = 1; i < n; i++) {
                    if (z[i][j] == 1) {
                        x1 = transformX(customers[i].getxCoordinate());
                        y1 = transformY(customers[i].getyCoordinate());
                        // If it is done with clustering heuristic, color center and draw lines to others
                        if (clusters) {
                            x2 = transformX(customers[j].getxCoordinate());
                            y2 = transformY(customers[j].getyCoordinate());
                            g2d.drawLine(x1, y1, x2, y2);
                            g2d.fillOval(x2 - (int) (size / 2.0), y2 - (int) (size / 2.0), size, size);
                            // If it is done exactly, color customer locations
                        } else if (drivers) {
                            g2d.fillOval(x1 - (int) (size / 2.0), y1 - (int) (size / 2.0), size, size);
                        }
                    }
                }
            }

            // Repaint the map on screen with assignments
            // First combine map and assignments, than repaint
            BufferedImage combined = new BufferedImage((int) imgDim.getWidth(), (int) imgDim.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics g = combined.getGraphics();
            g.drawImage(mapImg, 0, 0, null);
            g.drawImage(assignmentsImg, 0, 0, null);
            ImageIcon icon = new ImageIcon(combined);
            mapLabel.setIcon(icon);
            mainPanel.repaint();
        }
    }

    private void drawScenario(int scenario) {

        // TODO add method to draw clarke-wright

        if (solution.getxSol() != null) {
            // Create image
            routesImg = new BufferedImage(imgDim.width, imgDim.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = routesImg.createGraphics();
            g2d.setColor(Color.darkGray);

            double[][][][] x = solution.getxSol();

            int n = dataSet.getNumberOfCustomers() + 1;
            int m = dataSet.getNumberOfVehicles();

            Customer[] customers = dataSet.getCustomers();

            int x1, x2, y1, y2;

            // Add lines on driven routesImg
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    for (int k = 0; k < m; k++) {
                        if (x[i][j][k][scenario] == 1) {
                            x1 = transformX(customers[i].getxCoordinate());
                            x2 = transformX(customers[j].getxCoordinate());
                            y1 = transformY(customers[i].getyCoordinate());
                            y2 = transformY(customers[j].getyCoordinate());
                            g2d.drawLine(x1, y1, x2, y2);
                        }
                    }
                }
            }

            // Repaint the map on screen with assignments and routes
            // First combine map, assignments and routes, than repaint
            BufferedImage combined = new BufferedImage((int) imgDim.getWidth(), (int) imgDim.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics g = combined.getGraphics();
            g.drawImage(mapImg, 0, 0, null);
            g.drawImage(assignmentsImg, 0, 0, null);
            g.drawImage(routesImg, 0, 0, null);
            ImageIcon icon = new ImageIcon(combined);
            mapLabel.setIcon(icon);
            mainPanel.repaint();
        }
    }

    /**
     * Transform x-coordinate to screen coordinate
     *
     * @param x x-coordinate
     * @return transformed x-coordinate
     */
    private int transformX(double x) {
        // Make all coordinates positive
        x += mapDim.getWidth() / 2.0;
        // Scale coordinates to screensize
        x *= imgDim.getWidth() / mapDim.getWidth();
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
        y += mapDim.getHeight() / 2.0;
        // Scale coordinates to screensize
        y *= imgDim.getHeight() / mapDim.getHeight();
        // Upside down
        y = imgDim.getHeight() - y;
        // Round off
        return (int) Math.round(y);
    }
}
