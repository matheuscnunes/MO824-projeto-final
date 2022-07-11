package grasp.problem.ridesharing;

import grasp.framework.AbstractGRASP;
import grasp.framework.Solution;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RideSharingGRASP extends AbstractGRASP<Integer> {

    private final RideSharingEvaluator rideSharingEvaluator;

    public RideSharingGRASP(Double alpha, Integer iterations, Duration maxExecutionTime, RideSharingEvaluator rideSharingEvaluator) throws IOException {
        super(rideSharingEvaluator, alpha, iterations, maxExecutionTime);
        this.rideSharingEvaluator = rideSharingEvaluator;
    }

    @Override
    public List<Integer> makeCL() {
        return IntStream.range(0, rideSharingEvaluator.getDomainSize()).boxed().collect(Collectors.toList());
    }

    @Override
    public ArrayList<Integer> makeRCL() {
        return new ArrayList<>();
    }

    @Override
    public void updateCL() {
        // TODO
        // Check if any rider was added and remove it from other drivers
    }

    @Override
    public Solution<Integer> createEmptySol() {
        // Create empty solution, no riders are served.
        return new Solution<>(IntStream.range(0, rideSharingEvaluator.getDomainSize()).boxed().collect(Collectors.toList()));
    }

    @Override
    public Solution<Integer> localSearch() {
        return null;
    }
}
