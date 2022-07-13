package tabusearch.framework;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Random;

import grasp.framework.Evaluator;
import grasp.framework.Solution;

/**
 * Abstract class for metaheuristic Tabu Search. It consider a minimization problem.
 * 
 * @author ccavellucci, fusberti
 * @param <E>
 *            Generic type of the candidate to enter the solution.
 */
public abstract class AbstractTS<E> {

	/**
	 * flag that indicates whether the code should print more information on
	 * screen
	 */
	public static boolean verbose = false;

	/**
	 * a random number generator
	 */
	static Random rng = new Random(0);

	/**
	 * the objective function being optimized
	 */
	protected Evaluator<E> ObjFunction;

	/**
	 * the best solution cost
	 */
	protected Double bestCost;

	/**
	 * the incumbent solution cost
	 */
	protected Double cost;

	/**
	 * the best solution
	 */
	protected Solution<E> bestSol;

	/**
	 * the incumbent solution
	 */
	protected Solution<E> sol;

	/**
	 * the number of iterations the TS main loop executes.
	 */
	protected Integer iterations;
	
	/**
	 * the tabu tenure.
	 */
	protected Integer tenure;

	/**
	 * the Candidate List of elements to enter the solution.
	 */
	protected ArrayList<E> CL;

	/**
	 * the Restricted Candidate List of elements to enter the solution.
	 */
	protected ArrayList<E> RCL;
	
	/**
	 * the Tabu List of elements to enter the solution.
	 */
	protected ArrayDeque<E> TL;

	/**
	 * Creates the Candidate List, which is an ArrayList of candidate elements
	 * that can enter a solution.
	 * 
	 * @return The Candidate List.
	 */
	public abstract ArrayList<E> makeCL();

	/**
	 * Creates the Restricted Candidate List, which is an ArrayList of the best
	 * candidate elements that can enter a solution. 
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
	 * Updates the Candidate List according to the incumbent solution
	 * {@link #sol}. In other words, this method is responsible for
	 * updating the costs of the candidate solution elements.
	 */
	public abstract void updateCL();

	/**
	 * Creates a new solution which is empty, i.e., does not contain any
	 * candidate solution element.
	 * 
	 * @return An empty solution.
	 */
	public abstract Solution<E> createEmptySol();

	/**
	 * The TS local search phase is responsible for repeatedly applying a
	 * neighborhood operation while the solution is getting improved, i.e.,
	 * until a local optimum is attained. When a local optimum is attained
	 * the search continues by exploring moves which can make the current 
	 * solution worse. Cycling is prevented by not allowing forbidden
	 * (tabu) moves that would otherwise backtrack to a previous solution.
	 * 
	 * @return An local optimum solution.
	 */
	public abstract Solution<E> neighborhoodMove();

	/**
	 * Constructor for the AbstractTS class.
	 * 
	 * @param objFunction
	 *            The objective function being minimized.
	 * @param tenure
	 *            The Tabu tenure parameter. 
	 * @param iterations
	 *            The number of iterations which the TS will be executed.
	 */
	public AbstractTS(Evaluator<E> objFunction, Integer tenure, Integer iterations) {
		this.ObjFunction = objFunction;
		this.tenure = tenure;
		this.iterations = iterations;
	}

	/**
	 * A standard stopping criteria for the constructive heuristic is to repeat
	 * until the incumbent solution improves by inserting a new candidate
	 * element.
	 * 
	 * @return true if the criteria is met.
	 */
	public Boolean constructiveStopCriteria() {
		return (cost > sol.cost) ? false : true;
	}

}
