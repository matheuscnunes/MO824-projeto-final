package grasp.problem.ridesharing;

import grasp.framework.AbstractGRASP;
import grasp.framework.Solution;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RideSharingGRASP extends AbstractGRASP<Integer> {

    private final RideSharingEvaluator rideSharingEvaluator;

    public RideSharingGRASP(Double alpha, Integer iterations, Duration maxExecutionTime, RideSharingEvaluator rideSharingEvaluator) throws IOException {
        super(rideSharingEvaluator, alpha, iterations, maxExecutionTime);
        this.rideSharingEvaluator = rideSharingEvaluator;
    }

    @Override
    public Set<Integer> makeCL() {
        return IntStream.range(0, rideSharingEvaluator.getDomainSize()).boxed().collect(Collectors.toSet());
    }

    @Override
    public ArrayList<Integer> makeRCL() {
        return new ArrayList<>();
    }

    @Override
    public void updateCL() {
        Set<Integer> candidates = makeCL();
        for (Integer solCandidate : sol) {
            int rider = solCandidate % rideSharingEvaluator.riders;
            int allCandidatesSize = candidates.size();
            for (int r = rider; r < allCandidatesSize; r = r + rideSharingEvaluator.riders) {
                candidates.remove(r);
            }
        }

        CL = candidates;
    }

    @Override
    public Solution<Integer> createEmptySol() {
        // Create empty solution, no riders are served and penalty is added.
        Solution<Integer> emptySolution = new Solution<>();
        emptySolution.cost = rideSharingEvaluator.penalty * rideSharingEvaluator.riders;
        return emptySolution;
    }

    @Override
    public Solution<Integer> localSearch() {
        return sol;
    }
}
