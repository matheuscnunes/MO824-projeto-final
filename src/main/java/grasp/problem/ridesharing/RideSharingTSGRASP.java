package grasp.problem.ridesharing;

import grasp.framework.AbstractTSGRASP;
import grasp.framework.Solution;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RideSharingTSGRASP extends AbstractTSGRASP<Integer> {

    private final RideSharingEvaluator rideSharingEvaluator;

    private final Integer fakeTLElem = new Integer(-1);

    public RideSharingTSGRASP(Double alpha, Integer iterations, Duration maxExecutionTime, RideSharingEvaluator rideSharingEvaluator, Integer tenure) throws IOException {
        super(rideSharingEvaluator, alpha, iterations, maxExecutionTime, tenure);
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
	public ArrayDeque<Integer> makeTL() {
        Integer size = 2 * tenure;

        ArrayDeque<Integer> aux = new ArrayDeque<Integer>(size);
		for (int i = 0; i < size; i++) {
			aux.add(fakeTLElem);
		}

		return aux;
	}

    @Override
    public void updateCL() {
        Set<Integer> candidates = makeCL();
        List<List<Integer>> ridersPerDriverLists = rideSharingEvaluator.getRidersPerDriverLists(sol);

        // If driver is overloaded with the max requests, remove it all the riders possibilities for it
        for (int driverIndex = 0; driverIndex < ridersPerDriverLists.size(); driverIndex++) {
            if (ridersPerDriverLists.get(driverIndex).stream().filter(value -> value == 1).count() >= rideSharingEvaluator.maxRequests) {
                for (int d = rideSharingEvaluator.riders * driverIndex; d < (rideSharingEvaluator.riders * driverIndex) + rideSharingEvaluator.riders; d++) {
                    candidates.remove(d);
                }
            }
        }

        // Remove the selected rider from all the other drivers possibilities
        for (Integer solCandidate : sol) {
            int rider = solCandidate % rideSharingEvaluator.riders;
            for (int r = rider; r < rideSharingEvaluator.getDomainSize(); r = r + rideSharingEvaluator.riders) {
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
    public Solution<Integer> localSearch(LocalSearchMethod method) {
    	switch (method) {
			case FIRST_IMPROVING:
				return localSearchFirstImproving();
			case BEST_IMPROVING:
				return localSearchBestImproving();
			case TABU_SEARCH:
				return tabuSearch();
            case TABU_PROBABILISTIC_50_PERCENT:
                return tabuSearch(0.5);
			default:
				System.out.println("Method not implemented");
				return sol;
		}
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

    private Solution<Integer> tabuSearch() {
        return tabuSearch(1.0);
    }

    private Solution<Integer> tabuSearch(Double percentage) {
    	Double minDeltaCost;
		Integer bestCandIn = null, bestCandOut = null;

		do {
			minDeltaCost = Double.POSITIVE_INFINITY;
			updateCL();

            // Evaluate insertions
			for (Integer candIn : CL) {
				if (rng.nextDouble() < percentage && !TL.contains(candIn)) {
                    double deltaCost = rideSharingEvaluator.evaluateInsertionCost(candIn, sol);
                    if (deltaCost < minDeltaCost) {
                        minDeltaCost = deltaCost;
                        bestCandIn = candIn;
                        bestCandOut = null;
                    }
                }
			}

            // Evaluate removals
            for (Integer candOut : sol) {
                if (rng.nextDouble() < percentage && !TL.contains(candOut)) {
                    double deltaCost = rideSharingEvaluator.evaluateRemovalCost(candOut, sol);
                    if (deltaCost < minDeltaCost) {
                        minDeltaCost = deltaCost;
                        bestCandIn = null;
                        bestCandOut = candOut;
                    }
                }
            }

            // Evaluate exchanges
            for (Integer candIn : CL) {
                for (Integer candOut : sol) {
                    if (rng.nextDouble() < percentage && !TL.contains(candIn) && !TL.contains(candOut)) {
                        double deltaCost = rideSharingEvaluator.evaluateExchangeCost(candIn, candOut, sol);
                        if (deltaCost < minDeltaCost) {
                            minDeltaCost = deltaCost;
                            bestCandIn = candIn;
                            bestCandOut = candOut;
                        }
                    }
                }
            }

            // Implement the best move, if it reduces the solution cost.
            if (minDeltaCost < -Double.MIN_VALUE) {  
                TL.poll();
                if (bestCandOut != null) {
                    sol.remove(bestCandOut);
                    CL.add(bestCandOut);
                    TL.add(bestCandOut);
                } else {
                    TL.add(fakeTLElem);
                }

                TL.poll();
                if (bestCandIn != null) {
                    sol.add(bestCandIn);
                    CL.remove(bestCandIn);
                    TL.add(bestCandIn);
                } else {
                    TL.add(fakeTLElem);
                }

                rideSharingEvaluator.evaluate(sol);
            }

        } while (minDeltaCost < -Double.MIN_VALUE);

        return sol;
    }
}
