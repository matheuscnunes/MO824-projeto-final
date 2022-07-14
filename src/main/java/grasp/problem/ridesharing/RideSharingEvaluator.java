package grasp.problem.ridesharing;

import grasp.framework.Evaluator;
import grasp.framework.Solution;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RideSharingEvaluator  implements Evaluator<Integer> {
    private static final int METADATA_HEADER_OFFSET = 7;
    private static final int DIMENSION_OFFSET = 12;
    private static final int CAPACITY_OFFSET = 11;

    /**
     * Problem generic variables
     */
    public Integer domainSize;
    public List<List<Integer>> driverServingRidersVariable;

    /**
     * Riders related variables
     */
    public int riders;
    public final List<NodeCoord> ridersOriginCoords = new ArrayList<>();
    public final List<NodeCoord> ridersDestinationCoords = new ArrayList<>();


    /**
     * Drivers related variables
     */
    public int drivers;
    public final List<NodeCoord> driversOriginCoords = new ArrayList<>();
    public final List<NodeCoord> driversDestinationCoords = new ArrayList<>();
    public int maxRequests;
    public int maxDrivingTime;
    public double penalty;

    public RideSharingEvaluator(Instance instance) {
        driverServingRidersVariable = new ArrayList<>();
        readInput(instance);
    }

    @Override
    public Integer getDomainSize() {
        return domainSize;
    }

    @Override
    public Double evaluate(Solution<Integer> sol) {
        int penaltiesNeeded = riders - sol.size();
        double penaltyCost = penaltiesNeeded * penalty;
        driverServingRidersVariable = getRidersPerDriverLists(sol);

        int rideCosts = 0;
        List<List<NodeCoord>> allRoutes = new ArrayList<>();
        for (int i = 0; i < driverServingRidersVariable.size(); i++) {
            List<Integer> driverRiders = driverServingRidersVariable.get(i);
            List<NodeCoord> driverRoute = new ArrayList<>();
            List<NodeCoord> driverRouteDestinations = new ArrayList<>();

            driverRoute.add(driversOriginCoords.get(i));
            for (int j = 0; j < driverRiders.size(); j++) {
                if (driverRiders.get(j) > 0) {
                    driverRoute.add(ridersOriginCoords.get(j));
                    if (!driverRouteDestinations.contains(ridersDestinationCoords.get(j))) {
                        driverRouteDestinations.add(ridersDestinationCoords.get(j));
                    }
                }
            }

            driverRoute.addAll(driverRouteDestinations);
            if (!driverRoute.contains(driversDestinationCoords.get(i))) {
                driverRoute.add(driversDestinationCoords.get(i));
            }
            allRoutes.add(driverRoute);
        }

        for (List<NodeCoord> route : allRoutes) {
            for (int i = 0; i < route.size() - 1; i++) {
                NodeCoord currentOrigin = route.get(i);
                NodeCoord currentDestination = route.get(i+1);

                rideCosts += currentOrigin.getDistanceFrom(currentDestination);
            }
        }

        double cost = rideCosts + penaltyCost;
        sol.cost = cost;
        return cost;
    }

    public List<List<Integer>> getRidersPerDriverLists(Solution<Integer> sol) {
        List<List<Integer>> driversDecisionVariables = new ArrayList<>();
        IntStream.range(0, drivers).forEach(ign -> {
            driversDecisionVariables.add(new ArrayList<>(Collections.nCopies(riders, 0))); // Drivers start not serving any riders
        });

        for (Integer item : sol) {
            int rider = item % riders;
            int driver = item / riders;
            driversDecisionVariables.get(driver).set(rider, 1);
        }

        return driversDecisionVariables;
    }

    @Override
    public Double evaluateInsertionCost(Integer elem, Solution<Integer> sol) {
        Double currentCost = evaluate(sol);
        Solution<Integer> newSol = new Solution<>(sol);
        newSol.add(elem);

        return evaluate(newSol) - currentCost;
    }

    @Override
    public Double evaluateRemovalCost(Integer elem, Solution<Integer> sol) {
        Double currentCost = evaluate(sol);
        Solution<Integer> newSol = new Solution<>(sol);
        newSol.remove(elem);

        return evaluate(newSol) - currentCost;
    }

    @Override
    public Double evaluateExchangeCost(Integer elemIn, Integer elemOut, Solution<Integer> sol) {
        Double currentCost = evaluate(sol);
        Solution<Integer> newSol = new Solution<>(sol);
        newSol.remove(elemOut);
        newSol.add(elemIn);

        return evaluate(newSol) - currentCost;
    }

    private void readInput(Instance instance) {
        try {
            List<String> allLines = Files.readAllLines(Paths.get(instance.getFilename()));

            List<NodeCoord> allCoords = new ArrayList<>();
            Integer dimension = getDimensionSize(allLines);

            for (int i = METADATA_HEADER_OFFSET; i < METADATA_HEADER_OFFSET + dimension; i++) {
                String[] values = allLines.get(i).split("\\s");
                allCoords.add(new NodeCoord(values));
            }

            // Using scenario described in Table 2. Drivers and Riders having different origins and destinations
            switch (instance) {
                case P_N16:
                    read16(allCoords);
                    break;
                case A_N32:
                    read32(allCoords);
                    break;
                case A_N44:
                    read44(allCoords);
                    break;
                default:
                    throw new RuntimeException("INSTANCE INPUT NOT IMPLEMENTED YET FOR " + instance);
            }

            System.out.println("domainSize: " + domainSize);
            System.out.println("penalty: " + penalty);
            System.out.println("Coords: " + allCoords);
            System.out.println("Riders: " + riders);
            System.out.println("Riders Origin Coords: " + ridersOriginCoords);
            System.out.println("Riders Destination Coords: " + ridersDestinationCoords);

            System.out.println("Drivers: " + drivers);
            System.out.println("Max Driving Time: " + maxDrivingTime);
            System.out.println("Max Requests Per Driver: " + maxRequests);
            System.out.println("Drivers Origin Coords: " + driversOriginCoords);
            System.out.println("Drivers Destination Coords: " + driversDestinationCoords);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void read16(List<NodeCoord> allCoords) {
        drivers = 3;
        riders = 5;
        maxRequests = 4;
        maxDrivingTime = 110;
        penalty = 100.0;

        domainSize = drivers * riders;
        loadDriverAndRiderCoordsFor16(allCoords);

        IntStream.range(0, drivers).forEach(ign ->
            driverServingRidersVariable.add(new ArrayList<>(Collections.nCopies(riders, 0))) // Drivers start not serving any riders
        );
    }

    private void loadDriverAndRiderCoordsFor16(List<NodeCoord> allCoords) {
        driversOriginCoords.add(allCoords.get(0));
        driversOriginCoords.add(allCoords.get(1));
        driversOriginCoords.add(allCoords.get(2));

        driversDestinationCoords.add(allCoords.get(3));
        driversDestinationCoords.add(allCoords.get(4));
        driversDestinationCoords.add(allCoords.get(5));

        ridersOriginCoords.add(allCoords.get(6));
        ridersOriginCoords.add(allCoords.get(7));
        ridersOriginCoords.add(allCoords.get(8));
        ridersOriginCoords.add(allCoords.get(9));
        ridersOriginCoords.add(allCoords.get(10));

        ridersDestinationCoords.add(allCoords.get(11));
        ridersDestinationCoords.add(allCoords.get(12));
        ridersDestinationCoords.add(allCoords.get(13));
        ridersDestinationCoords.add(allCoords.get(14));
        ridersDestinationCoords.add(allCoords.get(15));
    }

    private void read32(List<NodeCoord> allCoords) {
        drivers = 5;
        riders = 26;
        maxRequests = 4;
        maxDrivingTime = 110;
        penalty = 100.0;

        domainSize = drivers * riders;
        loadDriverAndRiderCoordsFor32(allCoords);

        IntStream.range(0, drivers).forEach(ign ->
            driverServingRidersVariable.add(new ArrayList<>(Collections.nCopies(riders, 0))) // Drivers start not serving any riders
        );
    }

    private void loadDriverAndRiderCoordsFor32(List<NodeCoord> allCoords) {
        NodeCoord commonDestination = allCoords.get(31);

        driversOriginCoords.add(allCoords.get(0));
        driversOriginCoords.add(allCoords.get(1));
        driversOriginCoords.add(allCoords.get(2));
        driversOriginCoords.add(allCoords.get(3));
        driversOriginCoords.add(allCoords.get(4));

        ridersOriginCoords.add(allCoords.get(5));
        ridersOriginCoords.add(allCoords.get(6));
        ridersOriginCoords.add(allCoords.get(7));
        ridersOriginCoords.add(allCoords.get(8));
        ridersOriginCoords.add(allCoords.get(9));

        ridersOriginCoords.add(allCoords.get(10));
        ridersOriginCoords.add(allCoords.get(11));
        ridersOriginCoords.add(allCoords.get(12));
        ridersOriginCoords.add(allCoords.get(13));
        ridersOriginCoords.add(allCoords.get(14));

        ridersOriginCoords.add(allCoords.get(15));
        ridersOriginCoords.add(allCoords.get(16));
        ridersOriginCoords.add(allCoords.get(17));
        ridersOriginCoords.add(allCoords.get(18));
        ridersOriginCoords.add(allCoords.get(19));

        ridersOriginCoords.add(allCoords.get(20));
        ridersOriginCoords.add(allCoords.get(21));
        ridersOriginCoords.add(allCoords.get(22));
        ridersOriginCoords.add(allCoords.get(23));
        ridersOriginCoords.add(allCoords.get(24));

        ridersOriginCoords.add(allCoords.get(25));
        ridersOriginCoords.add(allCoords.get(26));
        ridersOriginCoords.add(allCoords.get(27));
        ridersOriginCoords.add(allCoords.get(28));
        ridersOriginCoords.add(allCoords.get(29));

        ridersOriginCoords.add(allCoords.get(30));

        ridersDestinationCoords.addAll(new ArrayList<>(Collections.nCopies(riders, commonDestination)));
        driversDestinationCoords.addAll(new ArrayList<>(Collections.nCopies(drivers, commonDestination)));
    }

    private void read44(List<NodeCoord> allCoords) {
        drivers = 6;
        riders = 37;
        maxRequests = 4;
        maxDrivingTime = 110;
        penalty = 100.0;

        domainSize = drivers * riders;
        loadDriverAndRiderCoordsFor44(allCoords);

        IntStream.range(0, drivers).forEach(ign ->
            driverServingRidersVariable.add(new ArrayList<>(Collections.nCopies(riders, 0))) // Drivers start not serving any riders
        );
    }

    private void loadDriverAndRiderCoordsFor44(List<NodeCoord> allCoords) {
        NodeCoord commonDestination = allCoords.get(43);

        driversOriginCoords.add(allCoords.get(0));
        driversOriginCoords.add(allCoords.get(1));
        driversOriginCoords.add(allCoords.get(2));
        driversOriginCoords.add(allCoords.get(3));
        driversOriginCoords.add(allCoords.get(4));
        driversOriginCoords.add(allCoords.get(5));

        ridersOriginCoords.add(allCoords.get(6));
        ridersOriginCoords.add(allCoords.get(7));
        ridersOriginCoords.add(allCoords.get(8));
        ridersOriginCoords.add(allCoords.get(9));
        ridersOriginCoords.add(allCoords.get(10));

        ridersOriginCoords.add(allCoords.get(11));
        ridersOriginCoords.add(allCoords.get(12));
        ridersOriginCoords.add(allCoords.get(13));
        ridersOriginCoords.add(allCoords.get(14));
        ridersOriginCoords.add(allCoords.get(15));

        ridersOriginCoords.add(allCoords.get(16));
        ridersOriginCoords.add(allCoords.get(17));
        ridersOriginCoords.add(allCoords.get(18));
        ridersOriginCoords.add(allCoords.get(19));
        ridersOriginCoords.add(allCoords.get(20));

        ridersOriginCoords.add(allCoords.get(21));
        ridersOriginCoords.add(allCoords.get(22));
        ridersOriginCoords.add(allCoords.get(23));
        ridersOriginCoords.add(allCoords.get(24));
        ridersOriginCoords.add(allCoords.get(25));

        ridersOriginCoords.add(allCoords.get(26));
        ridersOriginCoords.add(allCoords.get(27));
        ridersOriginCoords.add(allCoords.get(28));
        ridersOriginCoords.add(allCoords.get(29));
        ridersOriginCoords.add(allCoords.get(30));

        ridersOriginCoords.add(allCoords.get(31));
        ridersOriginCoords.add(allCoords.get(32));
        ridersOriginCoords.add(allCoords.get(33));
        ridersOriginCoords.add(allCoords.get(34));
        ridersOriginCoords.add(allCoords.get(35));

        ridersOriginCoords.add(allCoords.get(36));
        ridersOriginCoords.add(allCoords.get(37));
        ridersOriginCoords.add(allCoords.get(38));
        ridersOriginCoords.add(allCoords.get(39));
        ridersOriginCoords.add(allCoords.get(40));

        ridersOriginCoords.add(allCoords.get(41));
        ridersOriginCoords.add(allCoords.get(42));

        ridersDestinationCoords.addAll(new ArrayList<>(Collections.nCopies(riders, commonDestination)));
        driversDestinationCoords.addAll(new ArrayList<>(Collections.nCopies(drivers, commonDestination)));
    }

    private int getDimensionSize(List<String> allLines) {
        return Integer.parseInt(allLines.get(3).substring(DIMENSION_OFFSET));
    }

    private int getCapacity(List<String> allLines) {
        return Integer.parseInt(allLines.get(5).substring(CAPACITY_OFFSET));
    }

    private int getDrivers(List<String> allLines) {
        String[] split = allLines.get(1).split("trucks: ");
        return Integer.parseInt(split[1].substring(0, split[1].indexOf(",")));
    }
}
