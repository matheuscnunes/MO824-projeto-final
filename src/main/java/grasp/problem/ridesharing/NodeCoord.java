package grasp.problem.ridesharing;

public class NodeCoord {
    public final int x;
    public final int y;

    public NodeCoord(String[] coord) {
        this.x = Integer.parseInt(coord[1]);
        this.y = Integer.parseInt(coord[2]);
    }

    @Override
    public String toString() {
        return "{" + x + ", " + y + "}";
    }
}
