package grasp.problem.ridesharing;

public enum Instance {
    P_N16("instances/in/P-n16-k8.vrp", "solutions/n16"),
    A_N32("instances/in/A-n32-k5.vrp", "solutions/n32"),
    A_N44("instances/in/A-n44-k6.vrp", "solutions/n44");

    private final String filename;
    
    private final String solutionPath;

    Instance(String filename, String solutionPath) {
        this.filename = filename;
        this.solutionPath = solutionPath;
    }

    public String getFilename() {
        return filename;
    }

    public String getSolutionPath() {
        return solutionPath;
    }
}
