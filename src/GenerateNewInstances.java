import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Random;

/**
 * Created by Joeri on 24-3-2015.
 * Create new instances
 */
public class GenerateNewInstances {

    public static void main(String[] args) {

        int vehicleCapacity = 50;
        double alpha = 0.75;
        double size = 2.0;

//        int[] nrOfCustomers = new int[] {20, 30, 50, 100, 150, 200, 300, 500};
//        int[] nrOfScenarios = new int[] {1, 3, 10, 50, 100};
        int[] nrOfCustomers = new int[] {1500};
        int[] nrOfScenarios = new int[] {3};
        int nrOfInstances = 10;
        Customer depot = new Customer(0);
        depot.setxCoordinate(0.0);
        depot.setyCoordinate(0.0);

        Random generator = new Random();

        for(int nrOfCust: nrOfCustomers) {
            for(int nrOfScen: nrOfScenarios) {
                for (int iteration = 0; iteration < nrOfInstances; iteration++) {
                    DataSet dataSet = new DataSet();
                    dataSet.setNumberOfCustomers(nrOfCust);
                    dataSet.setNumberOfVehicles(nrOfCust);
                    dataSet.setNumberOfScenarios(nrOfScen);
                    dataSet.setVehicleCapacity(vehicleCapacity);
                    dataSet.setAlpha(alpha);
                    depot.setDemandPerScenario(new int[nrOfScen]);
                    Customer[] customers = new Customer[nrOfCust + 1];
                    customers[0] = depot;
                    for (int cust = 1; cust <= nrOfCust; cust++) {
                        Customer customer = new Customer(cust + 1);
                        double xCoord = generator.nextDouble() * 2 * size - size;
                        customer.setxCoordinate(xCoord);
                        double yCoord = generator.nextDouble() * 2 * size - size;
                        customer.setyCoordinate(yCoord);
                        double dv = 5 + Math.sqrt(1.5) * generator.nextGaussian();
                        int[] demandPerScen = new int[nrOfScen];
                        if (nrOfScen == 3) {
                            double u1 = 0.7 + generator.nextDouble()/10.0;
                            int demand1 = (int) Math.round(u1 * dv);
                            demandPerScen[0] = demand1;
                            double u2 = 0.95 + generator.nextDouble()/10.0;
                            int demand2 = (int) Math.round(u2 * dv);
                            demandPerScen[1] = demand2;
                            double u3 = 1.2 + generator.nextDouble()/10.0;
                            int demand3 = (int) Math.round(u3 * dv);
                            demandPerScen[2] = demand3;
                        } else {
                            for (int i = 0; i < nrOfScen; i++) {
                                double demand = dv + Math.sqrt(1.5) * generator.nextGaussian();
                                if (demand < 1) {
                                    demand = 1;
                                } else if (demand > vehicleCapacity) {
                                    demand = vehicleCapacity;
                                }
                                demandPerScen[i] = (int) Math.round(demand);
                            }
                        }
                        customer.setDemandPerScenario(demandPerScen);
                        customers[cust] = customer;
                    }
                    dataSet.setCustomers(customers);
                    double[] probabilities = new double[nrOfScen];
                    for (int scen = 0; scen < nrOfScen; scen++) {
                        probabilities[scen] = 1.0/nrOfScen;
                    }
                    dataSet.setScenarioProbabilities(probabilities);
//                    double[][] costs = new double[nrOfCust + 1][nrOfCust + 1];
//                    for (int i = 0; i <= nrOfCust; i++) {
//                        for (int j = i; j <= nrOfCust; j++) {
//                            costs[i][j] = customers[i].getDistance(customers[j]);
//                            costs[j][i] = costs[i][j];
//                        }
//                    }
//                    dataSet.setTravelCosts(costs);
                    String fileName = "DAVRPInstance " + nrOfCust + " " + nrOfScen + " " + (iteration+1);
                    writeToFile(dataSet, fileName);
                }
            }
        }
    }

    private static void writeToFile(DataSet dataSet, String fileName) {
        // Write y
        try {
            // Create file
            FileWriter fstream = new FileWriter("Test Instances/Joeri2/" + fileName + ".txt");
            BufferedWriter out = new BufferedWriter(fstream);

            String line = "Number of customers: " + dataSet.getNumberOfCustomers();
            line += "\r\nVehicle capacity     " + dataSet.getVehicleCapacity();
            line += "\r\nAlpha:               " + dataSet.getAlpha();
            line += "\r\nNumber of scenarios: " + dataSet.getNumberOfScenarios();

            line += "\r\n\r\nLocation coordinates:\r\n";
            for (int i = 0; i <= dataSet.getNumberOfCustomers(); i++) {
                Customer cust = dataSet.getCustomers()[i];
                line += i + " " + round(cust.getxCoordinate(),5) + " " + round(cust.getyCoordinate(),5) + "\r\n";
            }
            out.write(line);
            line = "";

            line += "\r\nDemand per scenario:\r\n";
            for (int i = 1; i <= dataSet.getNumberOfCustomers(); i++) {
                Customer cust = dataSet.getCustomers()[i];
                line += i;
                for (int j = 0; j < dataSet.getNumberOfScenarios(); j++) {
                    int[] demands = cust.getDemandPerScenario();
                    line += " " + demands[j];
                }
                line += "\r\n";
                out.write(line);
                line = "";
            }
            out.write(line);
            line = "";
            line += "\r\nProbability of each scenario occurring:\r\n";
            line += round(dataSet.getScenarioProbabilities()[0],6);
            if (dataSet.getNumberOfScenarios() > 1) {
                for (int i = 1; i < dataSet.getNumberOfScenarios(); i++) {
                    line += " " + round(dataSet.getScenarioProbabilities()[i],6);
                }
            }
            line += "\r\n";
            line += "\r\nTravel costs:\r\n";
//            double[][] costs = dataSet.getTravelCosts();
//            for (int i = 0; i <= dataSet.getNumberOfCustomers(); i++) {
//                for (int j = 0; j <= dataSet.getNumberOfCustomers(); j++) {
//                    if (j > 0) {
//                        line += " ";
//                    }
//                    line += round(costs[i][j],3);
//                }
//                line += "\r\n";
//                out.write(line);
//                line = "";
//            }

            out.write(line);

            // Close the output stream
            out.close();
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error in writing file: " + e.getMessage());
        }
    }

    private static double round(double before, int precision) {
        double factor = Math.pow(10.0, (double) precision);
        double big = Math.round(factor * before);
        double after = big / factor;
        return after;
    }
}
