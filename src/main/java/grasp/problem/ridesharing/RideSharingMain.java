package grasp.problem.ridesharing;

import grasp.framework.AbstractTSGRASP;
import grasp.framework.Solution;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

public class RideSharingMain {

    public static void main(String[] args) throws IOException {
        Duration maxExecutionTime = Duration.ofMinutes(5);
        Instance instance = Instance.A_N44;
        double alpha = 0.40;
        int iterations = 100;
        
        AbstractTSGRASP.ConstructiveMethod constructiveMethod = AbstractTSGRASP.ConstructiveMethod.STANDARD;
        AbstractTSGRASP.LocalSearchMethod localSearchMethod = AbstractTSGRASP.LocalSearchMethod.TABU_SEARCH;

        System.out.println("------ Running RideSharingGRASP ------" +
                "\ninstance = " + instance +
                "\nconstructiveMethod = " + constructiveMethod +
                "\nlocalSearchMethod = " + localSearchMethod +
                "\nmaxExecutionTime = " + maxExecutionTime +
                "\nalpha = " + alpha +
                "\niterations = " + iterations);

        RideSharingEvaluator evaluator = new RideSharingEvaluator(instance);
        RideSharingTSGRASP rideSharingTSGRASP = new RideSharingTSGRASP(alpha, iterations, maxExecutionTime, evaluator, 10);

        Instant started = Instant.now();
        Solution<Integer> solution = rideSharingTSGRASP.solve(constructiveMethod, localSearchMethod);
        Instant ended = Instant.now();
        evaluator.evaluate(solution);
        System.out.println("Solution: " + solution + "\nTime took: " + (ended.toEpochMilli() - started.toEpochMilli()) + "ms\nRiders distribution: " + evaluator.driverServingRidersVariable);
    }
}
