package grasp.problem.ridesharing;

public enum Instance {
    P_N16("instances/in/P-n16-k8.vrp"), A_N32("instances/in/A-n32-k5.vrp"), A_N44("instances/in/A-n44-k6.vrp");

    private final String filename;
    Instance(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }
}
