package grasp.problem.ridesharing;

import grasp.framework.AbstractGRASP;
import grasp.framework.Solution;

import java.io.IOException;
import java.time.Duration;

public class RideSharingMain {

    public static void main(String[] args) throws IOException {
        Duration maxExecutionTime = Duration.ofMinutes(5);
        Instance instance = Instance.P_N16;
        double alpha = 0.25;
        int iterations = 100;
        
        AbstractGRASP.ConstructiveMethod constructiveMethod = AbstractGRASP.ConstructiveMethod.STANDARD;
        AbstractGRASP.LocalSearchMethod localSearchMethod = AbstractGRASP.LocalSearchMethod.FIRST_IMPROVING;

        System.out.println("------ Running RideSharingGRASP ------" +
                "\ninstance = " + instance +
                "\nconstructiveMethod = " + constructiveMethod +
                "\nmaxExecutionTime = " + maxExecutionTime +
                "\nalpha = " + alpha +
                "\niterations = " + iterations);

        RideSharingEvaluator evaluator = new RideSharingEvaluator(instance);
        RideSharingGRASP rideSharingGRASP = new RideSharingGRASP(alpha, iterations, maxExecutionTime, evaluator);

        Solution<Integer> solution = rideSharingGRASP.solve(constructiveMethod, localSearchMethod);
        System.out.println("Found solution: " + solution);
    }
}
