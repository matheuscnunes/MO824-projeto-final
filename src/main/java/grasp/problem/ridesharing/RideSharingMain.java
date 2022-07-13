package grasp.problem.ridesharing;

import grasp.framework.AbstractTSGRASP;
import grasp.framework.Solution;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

public class RideSharingMain {

    public static void main(String[] args) throws IOException {
        Duration maxExecutionTime = Duration.ofMinutes(5);
        Instance instance = Instance.P_N16;
        double alpha = 0.15;
        int iterations = 100;
        
        AbstractTSGRASP.ConstructiveMethod constructiveMethod = AbstractTSGRASP.ConstructiveMethod.STANDARD;
        AbstractTSGRASP.LocalSearchMethod localSearchMethod = AbstractTSGRASP.LocalSearchMethod.FIRST_IMPROVING;

        System.out.println("------ Running RideSharingGRASP ------" +
                "\ninstance = " + instance +
                "\nconstructiveMethod = " + constructiveMethod +
                "\nmaxExecutionTime = " + maxExecutionTime +
                "\nalpha = " + alpha +
                "\niterations = " + iterations);

        RideSharingEvaluator evaluator = new RideSharingEvaluator(instance);
        RideSharingTSGRASP rideSharingTSGRASP = new RideSharingTSGRASP(alpha, iterations, maxExecutionTime, evaluator, 1);

        Solution<Integer> solution = rideSharingTSGRASP.solve(constructiveMethod, localSearchMethod);
        evaluator.evaluate(solution);
        System.out.println("Found solution: " + solution + "\nRiders distribution: " + evaluator.driverServingRidersVariable);
    }
}
