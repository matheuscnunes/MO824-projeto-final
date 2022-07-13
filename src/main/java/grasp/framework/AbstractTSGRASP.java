/**
 * 
 */
package grasp.framework;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Abstract class for metaheuristic GRASP (Greedy Randomized Adaptive Search
 * Procedure). It consider a minimization problem.
 * 
 * @author ccavellucci, fusberti
 * @param <E>
 *            Generic type of the element which composes the solution.
 */
public abstract class AbstractTSGRASP<E> {

	/**
	 * flag that indicates whether the code should print more information on
	 * screen
	 */
	public static boolean verbose = true;

	/**
	 * a random number generator
	 */
	static Random rng = new Random(0);

	/**
	 * a random number generator for alpha's in Reactive GRASP
	 */
	static Random alphaRng = new Random(0);

	/**
	 * the objective function being optimized
	 */
	protected Evaluator<E> evaluator;

	/**
	 * the GRASP greediness-randomness parameter
	 */
	protected Double usedAlpha;

	/**
	 * array of alphas when Reactive GRASP applied
	 */
	protected double[] alphas;

    /**
	 * the tabu tenure.
	 */
	protected Integer tenure;

	/**
	 * the best (incumbent) solution cost
	 */
	protected Double bestCost;

	/**
	 * the current solution cost
	 */
	protected Double cost;

	/**
	 * the best solution
	 */
	protected Solution<E> bestSol;

	/**
	 * the current solution
	 */
	protected Solution<E> sol;

	/**
	 * the number of iterations the GRASP main loop executes.
	 */
	protected Integer iterations;

	/**
	 * the Candidate List of elements to enter the solution.
	 */
	protected Set<E> CL;

	/**
	 * the Restricted Candidate List of elements to enter the solution.
	 */
	protected ArrayList<E> RCL;

    /**
	 * the Tabu List of elements to enter the solution.
	 */
	protected ArrayDeque<E> TL;

	/**
	 * the max time to run the solver in seconds
	 */
	protected Duration maxExecutionTime;

	/**
	 * Creates the Candidate List, which is an Set of candidate elements
	 * that can enter a solution.
	 * 
	 * @return The Candidate List.
	 */
	public abstract Set<E> makeCL();

	/**
	 * Creates the Restricted Candidate List, which is an ArrayList of the best
	 * candidate elements that can enter a solution. The best candidates are
	 * defined through a quality threshold, delimited by the GRASP
	 * {@link #usedAlpha} greedyness-randomness parameter.
	 * 
	 * @return The Restricted Candidate List.
	 */
	public abstract ArrayList<E> makeRCL();

    /**
	 * Creates the Tabu List, which is an ArrayDeque of the Tabu
	 * candidate elements. The number of iterations a candidate
	 * is considered tabu is given by the Tabu Tenure {@link #tenure}
	 * 
	 * @return The Tabu List.
	 */
	public abstract ArrayDeque<E> makeTL();

	/**
	 * Updates the Candidate List according to the current solution
	 * {@link #sol}. In other words, this method is responsible for
	 * updating which elements are still viable to take part into the solution.
	 */
	public abstract void updateCL();

	/**
	 * Creates a new solution which is empty, i.e., does not contain any
	 * element.
	 * 
	 * @return An empty solution.
	 */
	public abstract Solution<E> createEmptySol();

	/**
	 * The GRASP local search phase is responsible for repeatedly applying a
	 * neighborhood operation while the solution is getting improved, i.e.,
	 * until a local optimum is attained.
	 * 
	 * @return An local optimum solution.
	 */
	public abstract Solution<E> localSearch(LocalSearchMethod localSearchMethod);

	/**
	 * Constructor for the AbstractGRASP class.
	 * 
	 * @param evaluator
	 *            The objective function being minimized.
	 * @param usedAlpha
	 *            The GRASP greediness-randomness parameter (within the range
	 *            [0,1])
	 * @param iterations
	 *            The number of iterations which the GRASP will be executed.
	 */
	public AbstractTSGRASP(Evaluator<E> evaluator, Double usedAlpha, Integer iterations, Duration maxExecutionTime, Integer tenure) {
		this.evaluator = evaluator;
		this.usedAlpha = usedAlpha;
		this.iterations = iterations;
		this.maxExecutionTime = maxExecutionTime;
		this.tenure = tenure;
	}
	
	/**
	 * The GRASP constructive heuristic, which is responsible for building a
	 * feasible solution by selecting in a greedy-random fashion, candidate
	 * elements to enter the solution.
	 * 
	 * @return A feasible solution to the problem being minimized.
	 */
	public Solution<E> constructiveHeuristic(ConstructiveMethod method, String... args) {
		switch (method) {
			case STANDARD:
				alphas = new double[]{usedAlpha};
				return standardConstructiveHeuristic();
			case RANDOM_PLUS_GREEDY:
				alphas = new double[]{usedAlpha};
				return randomPlusGreedyConstructiveHeuristic(args);
			case RANDOM_REACTIVE_GRASP:
				alphas = getAlphasForRandomReactive();
				return standardConstructiveHeuristic();
			case BEST_ALPHA_REACTIVE_GRASP:
				return standardConstructiveHeuristic();
			default:
				System.out.println("Method not implemented");
				return createEmptySol();
		}
	}

	private double[] getAlphasForRandomReactive() {
		return new double[]{
				0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 	// 7 out of 28 	=~ 25%
				0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 		// 6 out of 28	=~ 21%
				0.3, 0.3, 0.3, 0.3, 0.3, 			// 5 out of 28 	=~ 18%
				0.4, 0.4, 0.4, 0.4,					// 4 out of 28	=~ 14%
				0.5, 0.5, 0.5, 						// 3 out of 28	=~ 11%
				0.6, 0.6, 							// 2 out of 28	=~ 7%
				0.7 								// 1 out of 28	=~ 4%
		};
	}

	private double[] getAlphasForBestReactive() {
		return new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8};
	}

	private Solution<E> randomPlusGreedyConstructiveHeuristic(String... args) {
		Integer p = Integer.parseInt(args[0]);
		CL = makeCL();
		RCL = makeRCL();
		sol = createEmptySol();
		cost = Double.POSITIVE_INFINITY;

		usedAlpha = alphas[alphaRng.nextInt(alphas.length)];

		/* Main loop, which repeats until the stopping criteria is reached. */
		int i = 0;
		while (!constructiveStopCriteria()) {

			double maxCost = Double.NEGATIVE_INFINITY, minCost = Double.POSITIVE_INFINITY;
			cost = evaluator.evaluate(sol);
			updateCL();

			/*
			 * Explore all candidate elements to enter the solution, saving the
			 * highest and lowest cost variation achieved by the candidates.
			 */
			for (E c : CL) {
				Double deltaCost = evaluator.evaluateInsertionCost(c, sol);
				if (deltaCost < minCost)
					minCost = deltaCost;
				if (deltaCost > maxCost)
					maxCost = deltaCost;
			}

			/*
			 * Among all candidates, insert into the RCL those with the highest
			 * performance using parameter alpha as threshold.
			 */
			for (E c : CL) {
				Double deltaCost = evaluator.evaluateInsertionCost(c, sol);
				if (deltaCost <= minCost + (i > p ? 0 : usedAlpha) * (maxCost - minCost)) {
					RCL.add(c);
				}
			}

			/* Choose a candidate randomly from the RCL */
			if (!RCL.isEmpty()) {
				int rndIndex = rng.nextInt(RCL.size());
				E inCand = RCL.get(rndIndex);
				CL.remove(inCand);
				sol.add(inCand);
				evaluator.evaluate(sol);
				RCL.clear();
			}
			i++;
		}

		return sol;

	}

	private Solution<E> standardConstructiveHeuristic() {
		CL = makeCL();
		RCL = makeRCL();
		sol = createEmptySol();
		cost = Double.POSITIVE_INFINITY;

		usedAlpha = alphas[alphaRng.nextInt(alphas.length)];

		/* Main loop, which repeats until the stopping criteria is reached. */
		while (!constructiveStopCriteria()) {

			double maxCost = Double.NEGATIVE_INFINITY, minCost = Double.POSITIVE_INFINITY;
			cost = evaluator.evaluate(sol);
			updateCL();

			/*
			 * Explore all candidate elements to enter the solution, saving the
			 * highest and lowest cost variation achieved by the candidates.
			 */
			for (E c : CL) {
				Double deltaCost = evaluator.evaluateInsertionCost(c, sol);
				if (deltaCost < minCost)
					minCost = deltaCost;
				if (deltaCost > maxCost)
					maxCost = deltaCost;
			}

			/*
			 * Among all candidates, insert into the RCL those with the highest
			 * performance using parameter alpha as threshold.
			 */
			for (E c : CL) {
				Double deltaCost = evaluator.evaluateInsertionCost(c, sol);
				if (deltaCost <= minCost + usedAlpha * (maxCost - minCost)) {
					RCL.add(c);
				}
			}

			/* Choose a candidate randomly from the RCL */
			if (!RCL.isEmpty()) {
				int rndIndex = rng.nextInt(RCL.size());
				E inCand = RCL.get(rndIndex);
				CL.remove(inCand);
				sol.add(inCand);
				evaluator.evaluate(sol);
				RCL.clear();
			}
		}

		return sol;
	}

	/**
	 * The GRASP mainframe. It consists of a loop, in which each iteration goes
	 * through the constructive heuristic and local search. The best solution is
	 * returned as result.
	 * 
	 * @return The best feasible solution obtained throughout all iterations.
	 */
	public Solution<E> solve(ConstructiveMethod constructiveMethod, LocalSearchMethod localSearchMethod, String... args) {
		Instant started = Instant.now();
		bestSol = createEmptySol();
		alphas = getAlphasForBestReactive();
        TL = makeTL();

        for (int i = 0; i < iterations; i++) {
			if (ConstructiveMethod.RANDOM_PLUS_GREEDY.equals(constructiveMethod)) {
				args[1] = String.valueOf(i);
			}

			constructiveHeuristic(constructiveMethod, args);
			localSearch(localSearchMethod);

			if (bestSol.cost > sol.cost) {
				bestSol = new Solution<E>(sol);
				if (constructiveMethod.equals(ConstructiveMethod.BEST_ALPHA_REACTIVE_GRASP)) {
					double[] newAlphas = new double[alphas.length + 1];
					for (int j = 0; j < alphas.length; j++) {
						newAlphas[j] = alphas[j];
					}
					newAlphas[alphas.length] = usedAlpha;
					alphas = newAlphas;
				}
				if (verbose)
					System.out.println("(Iter. " + i + ") BestSol = " + bestSol);
			}

			if (Instant.now().isAfter(started.plus(maxExecutionTime))) {
				System.out.println("Interrupting - Max execution time exceeded.");
				break;
			}
		}

		return bestSol;
	}

	/**
	 * A standard stopping criteria for the constructive heuristic is to repeat
	 * until the current solution improves by inserting a new candidate
	 * element.
	 * 
	 * @return true if the criteria is met.
	 */
	public Boolean constructiveStopCriteria() {
		return cost <= sol.cost;
	}

	public enum ConstructiveMethod {
		STANDARD, RANDOM_PLUS_GREEDY, RANDOM_REACTIVE_GRASP, BEST_ALPHA_REACTIVE_GRASP
	}

    public enum LocalSearchMethod {
		FIRST_IMPROVING, BEST_IMPROVING, TABU_SEARCH
	}

}
