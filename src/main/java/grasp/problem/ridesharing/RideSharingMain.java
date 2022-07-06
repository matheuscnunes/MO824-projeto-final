package grasp.problem.ridesharing;

import grasp.framework.AbstractGRASP;

import java.io.IOException;
import java.time.Duration;

public class RideSharingMain {

    public static void main(String[] args) throws IOException {
        Duration maxExecutionTime = Duration.ofMinutes(5);
        AbstractGRASP.ConstructiveMethod constructiveMethod = AbstractGRASP.ConstructiveMethod.STANDARD;
        Instance instance = Instance.P_N16;
        double alpha = 0.25;
        int iterations = 100;

        System.out.println("------ Running RideSharingGRASP ------" +
                "\ninstance = " + instance +
                "\nconstructiveMethod = " + constructiveMethod +
                "\nmaxExecutionTime = " + maxExecutionTime +
                "\nalpha = " + alpha +
                "\niterations = " + iterations);

        RideSharingEvaluator evaluator = new RideSharingEvaluator(instance);
        RideSharingGRASP rideSharingGRASP = new RideSharingGRASP(alpha, iterations, maxExecutionTime, evaluator);
        // TODO: Not working yet
//        rideSharingGRASP.solve(constructiveMethod);
    }
}
