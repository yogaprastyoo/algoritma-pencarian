import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

// ============================================================================
// Robot Navigation - Best-First Search (Greedy) dengan PriorityQueue
//
// Peta:
// 0 = bisa dilalui
// 1 = rintangan/tembok
// S = start, G = goal
//
// Fitness yang dipakai: h(n) = Manhattan Distance ke goal
// Node dengan h(n) terkecil diproses lebih dulu.
// ============================================================================
public class RobotNavigationBestFirst {

    private final int[][] grid;
    private final int rows;
    private final int cols;

    private final int startRow;
    private final int startCol;
    private final int goalRow;
    private final int goalCol;

    private final PriorityQueue<RobotNode> openPQ = new PriorityQueue<>(
            Comparator.comparingInt(RobotNode::getFitness)
    );

    private final Set<String> visited = new HashSet<>();
    private int nomor = 1;

    public RobotNavigationBestFirst(int[][] grid, int startRow, int startCol, int goalRow, int goalCol) {
        this.grid = grid;
        this.rows = grid.length;
        this.cols = grid[0].length;
        this.startRow = startRow;
        this.startCol = startCol;
        this.goalRow = goalRow;
        this.goalCol = goalCol;
    }

    private int manhattanDistance(int row, int col) {
        return Math.abs(row - goalRow) + Math.abs(col - goalCol);
    }

    private boolean inBounds(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    private boolean isFreeCell(int row, int col) {
        return grid[row][col] == 0;
    }

    private void addNode(RobotNode node) {
        if (!visited.contains(node.key())) {
            visited.add(node.key());
            node.setNomor(nomor++);
            openPQ.add(node);
        }
    }

    private void expandUp(RobotNode current) {
        int nr = current.getRow() - 1;
        int nc = current.getCol();
        if (inBounds(nr, nc) && isFreeCell(nr, nc)) {
            int h = manhattanDistance(nr, nc);
            addNode(new RobotNode(nr, nc, "up", current.getLevel() + 1, 0, h, current));
        }
    }

    private void expandDown(RobotNode current) {
        int nr = current.getRow() + 1;
        int nc = current.getCol();
        if (inBounds(nr, nc) && isFreeCell(nr, nc)) {
            int h = manhattanDistance(nr, nc);
            addNode(new RobotNode(nr, nc, "down", current.getLevel() + 1, 0, h, current));
        }
    }

    private void expandLeft(RobotNode current) {
        int nr = current.getRow();
        int nc = current.getCol() - 1;
        if (inBounds(nr, nc) && isFreeCell(nr, nc)) {
            int h = manhattanDistance(nr, nc);
            addNode(new RobotNode(nr, nc, "left", current.getLevel() + 1, 0, h, current));
        }
    }

    private void expandRight(RobotNode current) {
        int nr = current.getRow();
        int nc = current.getCol() + 1;
        if (inBounds(nr, nc) && isFreeCell(nr, nc)) {
            int h = manhattanDistance(nr, nc);
            addNode(new RobotNode(nr, nc, "right", current.getLevel() + 1, 0, h, current));
        }
    }

    public void solve() {
        int startFitness = manhattanDistance(startRow, startCol);
        addNode(new RobotNode(startRow, startCol, "", 0, 0, startFitness, null));

        while (!openPQ.isEmpty()) {
            RobotNode current = openPQ.poll();

            String parentInfo = current.getParent() != null
                    ? "parent: {(" + current.getParent().getRow() + "," + current.getParent().getCol() + ")"
                        + ", level: " + current.getParent().getLevel()
                        + ", no_urut: " + current.getParent().getNomor() + "}"
                    : "parent: null (node awal)";
            System.err.println("Sedang mengecek: " + current + " | " + parentInfo);

            if (current.getRow() == goalRow && current.getCol() == goalCol) {
                System.out.println("\n=== RUTE BERHASIL DITEMUKAN! ===");
                System.out.println("Total langkah: " + current.getLevel()
                        + " langkah | Node ke-" + current.getNomor() + " yang diperiksa");
                System.out.println("\nUrutan langkah robot:");
                printPath(current);
                System.out.println("\nVisual peta rute:");
                printGridWithPath(current);
                return;
            }

            expandLeft(current);
            expandUp(current);
            expandRight(current);
            expandDown(current);
        }

        System.out.println("Rute tidak ditemukan.");
    }

    private List<RobotNode> buildPath(RobotNode goalNode) {
        List<RobotNode> path = new ArrayList<>();
        RobotNode current = goalNode;
        while (current != null) {
            path.add(current);
            current = current.getParent();
        }
        Collections.reverse(path);
        return path;
    }

    private void printPath(RobotNode goalNode) {
        List<RobotNode> path = buildPath(goalNode);
        for (RobotNode node : path) {
            String langkah = node.getOperator().isEmpty() ? "START" : "Gerak " + node.getOperator();
            System.out.println("Langkah " + node.getLevel() + ": " + langkah
                    + " -> (" + node.getRow() + "," + node.getCol() + ")"
                    + "  [fitness=" + node.getFitness() + "]");
        }

        StringBuilder compact = new StringBuilder("Path ringkas: ");
        for (int i = 0; i < path.size(); i++) {
            RobotNode node = path.get(i);
            compact.append("(").append(node.getRow()).append(",").append(node.getCol()).append(")");
            if (i < path.size() - 1) {
                compact.append(" -> ");
            }
        }
        System.out.println(compact);
    }

    private void printGridWithPath(RobotNode goalNode) {
        char[][] view = new char[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                view[r][c] = grid[r][c] == 1 ? '#' : '.';
            }
        }

        for (RobotNode node : buildPath(goalNode)) {
            int r = node.getRow();
            int c = node.getCol();
            if (!(r == startRow && c == startCol) && !(r == goalRow && c == goalCol)) {
                view[r][c] = '*';
            }
        }

        view[startRow][startCol] = 'S';
        view[goalRow][goalCol] = 'G';

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                System.out.print(view[r][c] + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        int[][] map = {
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 1, 0, 0, 0, 1, 1, 1, 1, 1, 0},
                {0, 1, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };

        int sRow = 3, sCol = 0;
        int gRow = 2, gCol = 6;

        System.out.println("Mencari rute robot dengan Best-First Search (Priority Queue)...");
        System.out.println("Start: (" + sRow + "," + sCol + ")");
        System.out.println("Goal : (" + gRow + "," + gCol + ")\n");

        RobotNavigationBestFirst nav = new RobotNavigationBestFirst(map, sRow, sCol, gRow, gCol);
        nav.solve();
    }
}
