package grasp.problem.ridesharing;

import grasp.framework.AbstractGRASP;
import grasp.framework.Solution;

import java.io.IOException;
import java.util.ArrayList;

public class RideSharingGRASP extends AbstractGRASP<Integer> {

    private final RideSharingEvaluator rideSharingEvaluator;

    public RideSharingGRASP(Double alpha, Integer iterations, Long maxTimeInSeconds, RideSharingEvaluator rideSharingEvaluator) throws IOException {
        super(rideSharingEvaluator, alpha, iterations, maxTimeInSeconds);
        this.rideSharingEvaluator = rideSharingEvaluator;
    }

    @Override
    public ArrayList<Integer> makeCL() {
        return null;
    }

    @Override
    public ArrayList<Integer> makeRCL() {
        return null;
    }

    @Override
    public void updateCL() {

    }

    @Override
    public Solution<Integer> createEmptySol() {
        return null;
    }

    @Override
    public Solution<Integer> localSearch() {
        return null;
    }
}
