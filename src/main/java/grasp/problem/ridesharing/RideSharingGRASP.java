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
    public Solution<Integer> localSearch(LocalSearchMethod localSearchMethod) {
    	return LocalSearchMethod.FIRST_IMPROVING.equals(localSearchMethod) 
            ? localSearchFirstImproving() 
            : localSearchBestImproving();
    }

    private Solution<Integer> localSearchFirstImproving() {
        Double minDeltaCost;
		Integer bestCandIn = null, bestCandOut = null;

		do {
			minDeltaCost = Double.POSITIVE_INFINITY;
			updateCL();

            // Evaluate insertions
			for (Integer candIn : CL) {
				double deltaCost = rideSharingEvaluator.evaluateInsertionCost(candIn, sol);
				if (deltaCost < -Double.MIN_VALUE) {
					minDeltaCost = deltaCost;
					bestCandIn = candIn;
					bestCandOut = null;
					break;
				}
			}

            if (bestCandIn == null) {
				// Evaluate removals
				for (Integer candOut : sol) {
					double deltaCost = rideSharingEvaluator.evaluateRemovalCost(candOut, sol);
					if (deltaCost < -Double.MIN_VALUE) {
						minDeltaCost = deltaCost;
						bestCandIn = null;
						bestCandOut = candOut;
						break;
					}
				}

				if (bestCandOut == null) {
					// Evaluate exchanges
					for (Integer candIn : CL) {
						for (Integer candOut : sol) {
							double deltaCost = rideSharingEvaluator.evaluateExchangeCost(candIn, candOut, sol);
							if (deltaCost < -Double.MIN_VALUE) {
								minDeltaCost = deltaCost;
								bestCandIn = candIn;
								bestCandOut = candOut;
								break;
							}
						}
					}
				}
			}

            // Implement the best move, if it reduces the solution cost.
            if (minDeltaCost < -Double.MIN_VALUE) {
                if (bestCandOut != null) {
                    sol.remove(bestCandOut);
                    CL.add(bestCandOut);
                }
                if (bestCandIn != null) {
                    sol.add(bestCandIn);
                    CL.remove(bestCandIn);
                }
                rideSharingEvaluator.evaluate(sol);
            }

        } while (minDeltaCost < -Double.MIN_VALUE);

        return sol;
    }

    private Solution<Integer> localSearchBestImproving() {
        Double minDeltaCost;
		Integer bestCandIn = null, bestCandOut = null;

		do {
			minDeltaCost = Double.POSITIVE_INFINITY;
			updateCL();

            // Evaluate insertions
			for (Integer candIn : CL) {
				double deltaCost = rideSharingEvaluator.evaluateInsertionCost(candIn, sol);
				if (deltaCost < minDeltaCost) {
					minDeltaCost = deltaCost;
					bestCandIn = candIn;
					bestCandOut = null;
				}
			}

            // Evaluate removals
            for (Integer candOut : sol) {
                double deltaCost = rideSharingEvaluator.evaluateRemovalCost(candOut, sol);
                if (deltaCost < minDeltaCost) {
                    minDeltaCost = deltaCost;
                    bestCandIn = null;
                    bestCandOut = candOut;
                }
            }

            // Evaluate exchanges
            for (Integer candIn : CL) {
                for (Integer candOut : sol) {
                    double deltaCost = rideSharingEvaluator.evaluateExchangeCost(candIn, candOut, sol);
                    if (deltaCost < minDeltaCost) {
                        minDeltaCost = deltaCost;
                        bestCandIn = candIn;
                        bestCandOut = candOut;
                    }
                }
            }

            // Implement the best move, if it reduces the solution cost.
            if (minDeltaCost < -Double.MIN_VALUE) {
                if (bestCandOut != null) {
                    sol.remove(bestCandOut);
                    CL.add(bestCandOut);
                }
                if (bestCandIn != null) {
                    sol.add(bestCandIn);
                    CL.remove(bestCandIn);
                }
                rideSharingEvaluator.evaluate(sol);
            }

        } while (minDeltaCost < -Double.MIN_VALUE);

        return sol;
    }
}
