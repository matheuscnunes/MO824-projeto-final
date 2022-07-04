package grasp.problem.ridesharing;

import grasp.framework.Evaluator;
import grasp.framework.Solution;

import java.io.IOException;
import java.util.Arrays;

public class RideSharingEvaluator  implements Evaluator<Integer> {

    public final Integer domainSize;
    public final Double[] variables;

    public RideSharingEvaluator(String filename) {
        domainSize = 1;
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
}
