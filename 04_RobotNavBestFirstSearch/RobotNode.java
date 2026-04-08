public class RobotNode {

    private final int row;
    private final int col;
    private final String operator;
    private final int level;
    private int nomor;
    private int fitness;
    private final RobotNode parent;

    public RobotNode(int row, int col, String operator, int level, int nomor, int fitness, RobotNode parent) {
        this.row = row;
        this.col = col;
        this.operator = operator;
        this.level = level;
        this.nomor = nomor;
        this.fitness = fitness;
        this.parent = parent;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public String getOperator() { return operator; }
    public int getLevel() { return level; }
    public int getNomor() { return nomor; }
    public int getFitness() { return fitness; }
    public RobotNode getParent() { return parent; }

    public void setNomor(int nomor) { this.nomor = nomor; }
    public void setFitness(int fitness) { this.fitness = fitness; }

    public String key() {
        return row + "," + col;
    }

    @Override
    public String toString() {
        return "RobotNode {"
                + "posisi: (" + row + "," + col + ")"
                + ", langkah: '" + (operator.isEmpty() ? "START" : operator) + '\''
                + ", level: " + level
                + ", fitness: " + fitness
                + ", no_urut: " + nomor
                + '}';
    }
}
