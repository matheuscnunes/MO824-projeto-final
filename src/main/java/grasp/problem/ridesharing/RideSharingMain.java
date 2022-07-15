package grasp.problem.ridesharing;

import grasp.framework.AbstractTSGRASP;
import grasp.framework.Solution;
import grasp.framework.AbstractTSGRASP.ConstructiveMethod;
import grasp.framework.AbstractTSGRASP.LocalSearchMethod;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class RideSharingMain {

    public static void main(String[] args) throws IOException {

        List<Instance> instances = Arrays.asList(Instance.P_N16, Instance.A_N32, Instance.A_N44);
        List<ConstructiveMethod> constructiveMethods = Arrays.asList(ConstructiveMethod.STANDARD, ConstructiveMethod.RANDOM_REACTIVE_GRASP, ConstructiveMethod.RANDOM_PLUS_GREEDY, ConstructiveMethod.BEST_ALPHA_REACTIVE_GRASP);
        List<LocalSearchMethod> localSearchMethods = Arrays.asList(LocalSearchMethod.FIRST_IMPROVING, LocalSearchMethod.BEST_IMPROVING, LocalSearchMethod.TABU_SEARCH, LocalSearchMethod.TABU_PROBABILISTIC_50_PERCENT);

        for (Instance instance : instances) {
            for (ConstructiveMethod constructiveMethod : constructiveMethods) {
                for (LocalSearchMethod localSearchMethod : localSearchMethods) {
                    String path = String.format("%s/%s_%s.txt", instance.getSolutionPath(), constructiveMethod, localSearchMethod);

                    File file = new File(path);
                    file.createNewFile();
                    FileWriter writer = new FileWriter(file);

                    writer.write(String.format("Instance: %s; Constructive Method: %s; LocalSearch Method: %s\n\n", instance, constructiveMethod, localSearchMethod));

                    Duration maxExecutionTime = Duration.ofMinutes(5);
                    double alpha = 0.15;
                    int iterations = 100;
                    
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
                    Solution<Integer> solution = rideSharingTSGRASP.solve(constructiveMethod, localSearchMethod, "3");
                    Instant ended = Instant.now();
                    evaluator.evaluate(solution);
                    
                    long time = ended.toEpochMilli() - started.toEpochMilli();
                    System.out.println("Solution: " + solution + "\nTime took: " + time + "ms\nRiders distribution: " + evaluator.driverServingRidersVariable);
                
                    for (int i = 0; i < evaluator.driverServingRidersVariable.size(); i++) {
                        writer.write(String.format("D%d - %s\n", i, evaluator.driverServingRidersVariable.get(i).toString()));
                    }

                    writer.write(String.format("\nCost: %.1f; Time took: %dms", solution.cost, time));

                    writer.close();
                }
            }
        }

    }
}
