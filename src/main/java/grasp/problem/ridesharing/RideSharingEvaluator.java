package grasp.problem.ridesharing;

import grasp.framework.Evaluator;
import grasp.framework.Solution;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RideSharingEvaluator  implements Evaluator<Integer> {
    private static final int METADATA_HEADER_OFFSET = 7;
    private static final int DIMENSION_OFFSET = 12;
    private static final int CAPACITY_OFFSET = 11;

    public Integer domainSize;
    public final Double[] variables;

    public final List<NodeCoord> ridersOriginCoords = new ArrayList<>();
    public final List<NodeCoord> ridersDestinationCoords = new ArrayList<>();

    public final List<NodeCoord> driversOriginCoords = new ArrayList<>();
    public final List<NodeCoord> driversDestinationCoords = new ArrayList<>();

    public int drivers;
    public int riders;
    public int maxRequestsPerDriver;
    public int maxDrivingTime;
    public int capacity;

    public RideSharingEvaluator(Instance instance) {
        readInput(instance);
        variables = new Double[domainSize];
        Arrays.fill(variables, 0.0);
    }

    @Override
    public Integer getDomainSize() {
        return domainSize;
    }

    @Override
    public Double evaluate(Solution<Integer> sol) {
        return 1.0;
    }

    @Override
    public Double evaluateInsertionCost(Integer elem, Solution<Integer> sol) {
        return 1.0;
    }

    @Override
    public Double evaluateRemovalCost(Integer elem, Solution<Integer> sol) {
        return 1.0;
    }

    @Override
    public Double evaluateExchangeCost(Integer elemIn, Integer elemOut, Solution<Integer> sol) {
        return 1.0;
    }

    private void readInput(Instance instance) {
        try {
            List<String> allLines = Files.readAllLines(Paths.get(instance.getFilename()));

            domainSize = getDomainSize(allLines);
            capacity = 4;

            List<NodeCoord> allCoords = new ArrayList<>();

            for (int i = METADATA_HEADER_OFFSET; i < METADATA_HEADER_OFFSET + domainSize; i++) {
                String[] values = allLines.get(i).split("\\s");
                allCoords.add(new NodeCoord(values));
            }

            // Using scenario described in Table 2. Drivers and Riders having different origins and destinations
            switch (instance) {
                case P_N16:
                    read16(allCoords);
                    break;
                default:
                    throw new RuntimeException("INSTANCE INPUT NOT IMPLEMENTED YET FOR " + instance);
            }

            System.out.println("domainSize: " + domainSize);
            System.out.println("Coords: " + allCoords);
            System.out.println("Riders: " + riders);
            System.out.println("Riders Origin Coords: " + ridersOriginCoords);
            System.out.println("Riders Destination Coords: " + ridersDestinationCoords);

            System.out.println("Drivers: " + drivers);
            System.out.println("Capacity: " + capacity);
            System.out.println("Max Driving Time: " + maxDrivingTime);
            System.out.println("Max Requests Per Driver: " + maxRequestsPerDriver);
            System.out.println("Drivers Origin Coords: " + driversOriginCoords);
            System.out.println("Drivers Destination Coords: " + driversDestinationCoords);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void read16(List<NodeCoord> allCoords) {
        drivers = 3;
        maxRequestsPerDriver = 4;
        riders = 5;
        maxDrivingTime = 110;
        loadDriverAndRiderCoordsFor16(allCoords);
    }

    private void loadDriverAndRiderCoordsFor16(List<NodeCoord> allCoords) {
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

        driversOriginCoords.add(allCoords.get(0));
        driversOriginCoords.add(allCoords.get(1));
        driversOriginCoords.add(allCoords.get(2));

        driversDestinationCoords.add(allCoords.get(3));
        driversDestinationCoords.add(allCoords.get(4));
        driversDestinationCoords.add(allCoords.get(5));
    }

    private int getDomainSize(List<String> allLines) {
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
