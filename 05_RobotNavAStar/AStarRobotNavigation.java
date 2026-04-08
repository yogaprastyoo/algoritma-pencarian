
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

// =============================================================================
// Robot Navigation - Algoritma A* (A-Star)
//
// PROBLEM:
// Robot berada di titik START dalam sebuah grid berisi dinding (obstacle).
// Robot harus menemukan jalur TERPENDEK ke titik GOAL.
//
// GRID (5 baris × 11 kolom):
//
//   Kolom:  0    1    2    3    4    5    6    7    8    9   10
//   Baris 0: .    .    .    .    .    .    .    .    .    .    .
//   Baris 1: .    #    .    .    .    #    #    #    #    #    .
//   Baris 2: .    #    #    .    .    .    S    .    .    #    .
//   Baris 3: G    .    #    #    #    #    #    #    #    #    .
//   Baris 4: .    .    .    .    .    .    .    .    .    .    .
//
//   S = Start (2,6)    G = Goal (3,0)
//   # = Dinding        . = Bisa dilalui
//
// ALGORITMA A*:
// A* menggabungkan kelebihan BFS (jalur terpendek) dan Greedy (cepat/terarah)
// dengan rumus: f(n) = g(n) + h(n)
//
//   g(n) = biaya jalur dari START ke node n (jumlah langkah yang sudah ditempuh)
//   h(n) = estimasi biaya dari node n ke GOAL (Manhattan Distance)
//   f(n) = total estimasi biaya jalur melalui node n
//
// Node dengan f(n) TERKECIL diproses duluan → PriorityQueue (min-heap)
//
// PERBANDINGAN DENGAN ALGORITMA SEBELUMNYA:
// ┌──────────────┬──────────────┬─────────────────────────────────────────┐
// │ Algoritma    │ Prioritas    │ Keterangan                              │
// ├──────────────┼──────────────┼─────────────────────────────────────────┤
// │ BFS          │ -            │ FIFO, jamin terpendek, tidak terarah    │
// │ DFS          │ -            │ LIFO, tidak jamin terpendek             │
// │ Greedy/PQ    │ h(n)         │ Terarah tapi bisa tidak optimal         │
// │ A*           │ g(n) + h(n)  │ Terarah DAN jamin terpendek            │
// └──────────────┴──────────────┴─────────────────────────────────────────┘
//
// MENGAPA A* MENJAMIN JALUR TERPENDEK?
// Karena g(n) mencatat biaya jalur NYATA yang sudah ditempuh, sedangkan h(n)
// hanya estimasi. Kombinasi keduanya membuat A* tidak "tergoda" mengambil
// jalur yang kelihatan dekat ke goal tapi sebenarnya mahal.
// =============================================================================
public class AStarRobotNavigation {

    // =========================================================================
    // DEFINISI GRID
    // true  = bisa dilalui (open)
    // false = dinding/obstacle (wall)
    // =========================================================================
    private static final boolean[][] GRID = {
        // col: 0     1      2      3      4      5      6      7      8      9     10
        {  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true }, // baris 0
        {  true, false,  true,  true,  true, false, false, false, false, false,  true }, // baris 1
        {  true, false, false,  true,  true,  true,  true,  true,  true, false,  true }, // baris 2
        {  true,  true, false, false, false, false, false, false, false, false,  true }, // baris 3
        {  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true }, // baris 4
    };

    private static final int ROWS = GRID.length;       // 5
    private static final int COLS = GRID[0].length;    // 11

    // Posisi START dan GOAL
    private static final int START_ROW = 2, START_COL = 6;
    private static final int GOAL_ROW  = 3, GOAL_COL  = 0;

    // 4 arah gerakan: atas, bawah, kiri, kanan
    // {delta_baris, delta_kolom}
    private static final int[][] ARAH = { {-1,0}, {1,0}, {0,-1}, {0,1} };
    private static final String[] NAMA_ARAH = { "atas", "bawah", "kiri", "kanan" };

    // Open list: PriorityQueue, node dengan f terkecil keluar duluan
    private final PriorityQueue<Node> openList = new PriorityQueue<>(
            Comparator.comparingInt(Node::getF)
    );

    // Closed list: posisi yang sudah selesai diproses
    private final Set<String> closedList = new HashSet<>();

    // Penghitung nomor urut node
    private int nomor = 1;

    // =========================================================================
    // HEURISTIK - Manhattan Distance
    //
    // h(n) = |baris_sekarang - baris_goal| + |kolom_sekarang - kolom_goal|
    //
    // Manhattan Distance adalah heuristik yang ADMISSIBLE (tidak pernah
    // melebih-lebihkan biaya sebenarnya) karena robot hanya bisa gerak
    // 4 arah (tidak diagonal). Ini menjamin A* menemukan jalur optimal.
    // =========================================================================
    private int heuristic(int row, int col) {
        return Math.abs(row - GOAL_ROW) + Math.abs(col - GOAL_COL);
    }

    // Mengubah posisi (row, col) menjadi String unik untuk disimpan di Set
    private String posToKey(int row, int col) {
        return row + "," + col;
    }

    // =========================================================================
    // SOLVE - Algoritma A*
    //
    // LANGKAH-LANGKAH:
    // 1. Masukkan node START ke open list
    // 2. Ambil node dengan f terkecil dari open list
    // 3. Jika node = GOAL → selesai, rekonstruksi jalur
    // 4. Tandai node sebagai visited (masuk closed list)
    // 5. Expand: coba 4 arah gerakan, masukkan yang valid ke open list
    // 6. Ulangi dari langkah 2
    // =========================================================================
    public void solve() {
        int hAwal = heuristic(START_ROW, START_COL);
        Node startNode = new Node(START_ROW, START_COL, 0, hAwal, nomor++, null);
        openList.add(startNode);

        System.out.println("A* Robot Navigation");
        System.out.println("Start : (" + START_ROW + "," + START_COL + ")");
        System.out.println("Goal  : (" + GOAL_ROW  + "," + GOAL_COL  + ")");
        System.out.println("f(n) = g(n) + h(n), h = Manhattan Distance");
        System.out.println();
        printGrid(null); // tampilkan grid awal
        System.out.println();

        while (!openList.isEmpty()) {

            // Ambil node dengan f terkecil
            Node current = openList.poll();

            String parentInfo = current.getParent() != null
                    ? "parent: (" + current.getParent().getRow() + "," + current.getParent().getCol() + ")"
                            + " g=" + current.getParent().getG()
                    : "parent: null (START)";
            System.err.println("Proses: " + current + " | " + parentInfo);

            // Cek apakah ini GOAL
            if (current.getRow() == GOAL_ROW && current.getCol() == GOAL_COL) {
                System.out.println("=== JALUR DITEMUKAN! ===");
                List<Node> path = rekonstruksiJalur(current);
                System.out.println("Total langkah: " + (path.size() - 1));
                System.out.println();
                cetakJalur(path);
                System.out.println();
                printGrid(path);
                return;
            }

            // Masukkan ke closed list (sudah diproses)
            closedList.add(posToKey(current.getRow(), current.getCol()));

            // Expand: coba semua 4 arah
            for (int i = 0; i < ARAH.length; i++) {
                int newRow = current.getRow() + ARAH[i][0];
                int newCol = current.getCol() + ARAH[i][1];

                // Skip jika di luar grid
                if (newRow < 0 || newRow >= ROWS || newCol < 0 || newCol >= COLS) continue;

                // Skip jika dinding
                if (!GRID[newRow][newCol]) continue;

                // Skip jika sudah di closed list
                if (closedList.contains(posToKey(newRow, newCol))) continue;

                int newG = current.getG() + 1;            // setiap langkah biaya = 1
                int newH = heuristic(newRow, newCol);
                // newF = newG + newH (dihitung otomatis di constructor Node)

                Node neighbor = new Node(newRow, newCol, newG, newH, nomor++, current);
                openList.add(neighbor);

                System.err.println("  -> tambah tetangga (" + newRow + "," + newCol + ")"
                        + " via " + NAMA_ARAH[i]
                        + " | g=" + newG + " h=" + newH + " f=" + neighbor.getF());
            }
        }

        System.out.println("Jalur tidak ditemukan.");
    }

    // =========================================================================
    // REKONSTRUKSI JALUR
    // Telusuri balik dari node GOAL ke START lewat pointer parent,
    // lalu balik urutannya agar dari START ke GOAL.
    // =========================================================================
    private List<Node> rekonstruksiJalur(Node goal) {
        List<Node> path = new ArrayList<>();
        Node current = goal;
        while (current != null) {
            path.add(0, current); // tambah ke depan agar urutan START → GOAL
            current = current.getParent();
        }
        return path;
    }

    // =========================================================================
    // CETAK JALUR - tampilkan setiap langkah dengan nilai g, h, f
    // =========================================================================
    private void cetakJalur(List<Node> path) {
        System.out.println("Langkah-langkah:");
        System.out.printf("%-8s %-10s %-6s %-6s %-6s%n", "Langkah", "Posisi", "g(n)", "h(n)", "f(n)");
        System.out.println("-".repeat(40));
        for (int i = 0; i < path.size(); i++) {
            Node n = path.get(i);
            String label = (i == 0) ? "(START)" : (i == path.size()-1 ? "(GOAL)" : "");
            System.out.printf("%-8d (%d,%-2d) %-4s %-6d %-6d %-6d%n",
                    i, n.getRow(), n.getCol(), label, n.getG(), n.getH(), n.getF());
        }
    }

    // =========================================================================
    // PRINT GRID - visualisasi grid dengan jalur yang ditemukan
    //
    // Simbol:
    //   S = Start     G = Goal
    //   * = Jalur     # = Dinding
    //   . = Kosong
    // =========================================================================
    private void printGrid(List<Node> path) {
        // Kumpulkan posisi jalur ke Set untuk lookup O(1)
        Set<String> pathSet = new HashSet<>();
        if (path != null) {
            for (Node n : path) pathSet.add(posToKey(n.getRow(), n.getCol()));
        }

        // Header kolom
        System.out.print("     ");
        for (int c = 0; c < COLS; c++) System.out.printf("%3d", c);
        System.out.println();
        System.out.print("     ");
        System.out.println("-".repeat(COLS * 3));

        for (int r = 0; r < ROWS; r++) {
            System.out.printf("%2d | ", r);
            for (int c = 0; c < COLS; c++) {
                String key = posToKey(r, c);
                if (r == START_ROW && c == START_COL)      System.out.print("  S");
                else if (r == GOAL_ROW && c == GOAL_COL)   System.out.print("  G");
                else if (!GRID[r][c])                       System.out.print("  #");
                else if (pathSet.contains(key))             System.out.print("  *");
                else                                        System.out.print("  .");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        new AStarRobotNavigation().solve();
    }
}
