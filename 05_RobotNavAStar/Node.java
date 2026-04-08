
// =============================================================================
// Node untuk Robot Navigation (berbeda dengan Node di BFS/DFS/PQ)
//
// Di BFS/DFS/PQ: node menyimpan STATE berupa String puzzle "283164705"
// Di sini       : node menyimpan POSISI (baris, kolom) di dalam grid
// =============================================================================
public class Node {

    /** Posisi baris di grid */
    private final int row;

    /** Posisi kolom di grid */
    private final int col;

    /**
     * g(n) = biaya jalur dari titik START sampai node ini.
     * Setiap langkah (atas/bawah/kiri/kanan) menambah g sebesar 1.
     */
    private final int g;

    /**
     * h(n) = estimasi jarak dari node ini ke GOAL (heuristik).
     * Dihitung dengan Manhattan Distance:
     *   h = |baris_sekarang - baris_goal| + |kolom_sekarang - kolom_goal|
     */
    private final int h;

    /**
     * f(n) = g(n) + h(n)
     * Nilai inilah yang digunakan PriorityQueue untuk menentukan
     * node mana yang diproses duluan (f terkecil = paling prioritas).
     *
     * Di kode Eight Puzzle sebelumnya, ini disebut "fitness".
     * Di literatur A*, ini disebut "f(n)".
     */
    private final int f;

    /** Nomor urut node saat dimasukkan ke open list */
    private int nomor;

    /** Pointer ke node sebelumnya untuk rekonstruksi jalur */
    private final Node parent;

    public Node(int row, int col, int g, int h, int nomor, Node parent) {
        this.row    = row;
        this.col    = col;
        this.g      = g;
        this.h      = h;
        this.f      = g + h; // f(n) = g(n) + h(n)  ← inti dari A*
        this.nomor  = nomor;
        this.parent = parent;
    }

    public int  getRow()    { return row; }
    public int  getCol()    { return col; }
    public int  getG()      { return g; }
    public int  getH()      { return h; }
    public int  getF()      { return f; }
    public int  getNomor()  { return nomor; }
    public Node getParent() { return parent; }

    public void setNomor(int nomor) { this.nomor = nomor; }

    @Override
    public String toString() {
        return "Node {"
                + "pos: (" + row + "," + col + ")"
                + ", g: " + g
                + ", h: " + h
                + ", f: " + f
                + ", no_urut: " + nomor
                + '}';
    }
}
