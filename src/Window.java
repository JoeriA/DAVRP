import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by Joeri on 8-5-2014.
 */
public class Window extends JFrame {

    private JPanel contentPane;
    private JLabel lblPicture;
    private Dimension imgDim, screenSize;
    private Dimension mapDim = new Dimension(5, 5);
    private BufferedImage map;
    private DataSet dataSet;
    private Solution solution;

    /**
     * Create a window to display output
     */
    public Window() {
        pack();
        setExtendedState(Window.MAXIMIZED_BOTH);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Create contentpane
        contentPane = new JPanel();
//        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        // Calculate dimensions
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int dimensionSize = Math.min((int) screenSize.getHeight() - 80, (int) screenSize.getWidth() - 10);
        imgDim = new Dimension(dimensionSize, dimensionSize);

        lblPicture = new JLabel();
        lblPicture.setBounds(5, 5, imgDim.width, imgDim.height);
        contentPane.add(lblPicture);

        // Create graphics
        map = new BufferedImage(imgDim.width, imgDim.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = map.createGraphics();

        // Improve quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setBackground(Color.white);
        g2d.fillRect(0, 0, imgDim.width, imgDim.height);

        setVisible(true);
    }

    /**
     * Create a map of the customers
     *
     * @param dataSet dataset containing info about customers
     */
    public void createMap(DataSet dataSet) {

        // Make dataset global
        this.dataSet = dataSet;

        // Add title to window
        setTitle(dataSet.getInstance());
        // Add title to frame
        JLabel lblTitle = new JLabel(dataSet.getInstance());
        lblTitle.setBackground(Color.white);
        lblTitle.setOpaque(true);
        lblTitle.setBounds(imgDim.width + 50, 25, (int) (screenSize.getWidth() - imgDim.getWidth()) - 100, 50);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        contentPane.add(lblTitle);

        // Add description of data set
        String description = "Number of customers:\t" + dataSet.getNumberOfCustomers();
        description += "\r\nVehicle capacity:\t" + dataSet.getVehicleCapacity();
        description += "\r\nAlpha:\t\t" + dataSet.getAlpha();
        description += "\r\nNumber of scenarios:\t" + dataSet.getNumberOfScenarios();
        JTextArea lblDescription = new JTextArea(description);
        lblDescription.setBackground(Color.white);
        lblDescription.setOpaque(true);
        lblDescription.setBounds(imgDim.width + 50, 110, (int) (screenSize.getWidth() - imgDim.getWidth()) - 100, 80);
        lblDescription.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblDescription.setEditable(false);
        contentPane.add(lblDescription);

        // Create graphics
        Graphics2D g2d = map.createGraphics();
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

        // Print the map on screen
        ImageIcon icon = new ImageIcon(map);
        lblPicture.setIcon(icon);
        contentPane.repaint();
    }

    public void drawResults(Solution solution) {
        this.solution = solution;

        // Add subtitle
        JLabel lblSubTitle = new JLabel("Solved with " + solution.getName());
        lblSubTitle.setBackground(Color.white);
        lblSubTitle.setOpaque(true);
        lblSubTitle.setBounds(imgDim.width + 50, 75, (int) (screenSize.getWidth() - imgDim.getWidth()) - 100, 25);
        lblSubTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        contentPane.add(lblSubTitle);

        // Title scenario selector
        JLabel lblSelector = new JLabel("Choose scenario");
        lblSelector.setBackground(Color.white);
        lblSelector.setOpaque(true);
        lblSelector.setBounds(imgDim.width + 50, 200, 150, 25);
        lblSelector.setFont(new Font("SansSerif", Font.PLAIN, 14));
        contentPane.add(lblSelector);

        // Add scenario selector
        SpinnerNumberModel scenariosList = new SpinnerNumberModel(1, 1, dataSet.getNumberOfScenarios(), 1);
//        SpinnerNumberModel scenariosList = new SpinnerNumberModel();
        JSpinner scenarioSpinner = new JSpinner(scenariosList);
        scenarioSpinner.setForeground(Color.black);
        scenarioSpinner.setBackground(Color.white);
        scenarioSpinner.setOpaque(true);
//        scenarioSpinner.setBounds(imgDim.width + 225, 200, 100, 50);
        scenarioSpinner.setLocation(imgDim.width + 225, 200);
        contentPane.add(scenarioSpinner);

        drawAssignments();

        if (solution.getxSol() != null) {
            drawScenario(1);
        }
    }

    private void drawAssignments() {

        BufferedImage assignment = new BufferedImage(imgDim.width, imgDim.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = assignment.createGraphics();
        g2d.setColor(Color.darkGray);

        double[][] z = solution.getzSol();

        int n = dataSet.getNumberOfCustomers() + 1;

        Customer[] customers = dataSet.getCustomers();

        int x1, x2, y1, y2;

        // Add lines on driven routes

        for (int j = 0; j < n; j++) {
            for (int i = 1; i < n; i++) {
                if (z[i][j] == 1) {
                    x1 = transformX(customers[i].getxCoordinate());
                    x2 = transformX(customers[j].getxCoordinate());
                    y1 = transformY(customers[i].getyCoordinate());
                    y2 = transformY(customers[j].getyCoordinate());
                    g2d.drawLine(x1, y1, x2, y2);
                }
            }
        }

        // Repaint the map on screen
        BufferedImage combined = new BufferedImage((int) imgDim.getWidth(), (int) imgDim.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = combined.getGraphics();
        g.drawImage(map, 0, 0, null);
        g.drawImage(assignment, 0, 0, null);
        ImageIcon icon = new ImageIcon(combined);
        lblPicture.setIcon(icon);
        contentPane.repaint();
    }

    private void drawScenario(int scenario) {
        BufferedImage routes = new BufferedImage(imgDim.width, imgDim.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = routes.createGraphics();
        g2d.setColor(Color.darkGray);

        double[][][][] x = solution.getxSol();

        int n = dataSet.getNumberOfCustomers() + 1;
        int m = dataSet.getNumberOfVehicles();

        Customer[] customers = dataSet.getCustomers();

        int x1, x2, y1, y2;

        // Add lines on driven routes
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

        // Repaint the map on screen
        BufferedImage combined = new BufferedImage((int) imgDim.getWidth(), (int) imgDim.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = combined.getGraphics();
        g.drawImage(map, 0, 0, null);
        g.drawImage(routes, 0, 0, null);
        ImageIcon icon = new ImageIcon(combined);
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