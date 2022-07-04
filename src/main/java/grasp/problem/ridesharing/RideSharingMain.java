package grasp.problem.ridesharing;

import grasp.framework.AbstractGRASP;

import java.io.IOException;
import java.time.Duration;

public class RideSharingMain {
    public static final Long MAX_EXECUTION_TIME = Duration.ofMinutes(5).getSeconds();

    public static void main(String[] args) throws IOException {
        RideSharingEvaluator evaluator = new RideSharingEvaluator("");
        RideSharingGRASP rideSharingGRASP = new RideSharingGRASP(0.25, 100, MAX_EXECUTION_TIME, evaluator);

        AbstractGRASP.ConstructiveMethod constructiveMethod = AbstractGRASP.ConstructiveMethod.STANDARD;

        // TODO: Not working yet
        // rideSharingGRASP.solve(constructiveMethod);
    }
}
