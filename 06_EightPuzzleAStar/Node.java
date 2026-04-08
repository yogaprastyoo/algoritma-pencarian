
public class Node {

    /**
     * Konfigurasi papan (state) dalam bentuk String panjang 9, contoh:
     * "283164705". Karakter '0' merepresentasikan ruang kosong.
     */
    private String state;

    /**
     * Operator/aksi yang dipakai untuk mencapai state ini dari parent. Nilai:
     * "up", "down", "left", "right". Untuk node awal biasanya string kosong.
     */
    private String operator;

    /**
     * g(n) = biaya nyata dari state awal ke state ini (jumlah langkah).
     * Sama dengan "level" atau "depth" pada BFS.
     */
    private int g;

    /**
     * h(n) = estimasi biaya dari state ini ke state tujuan (heuristik).
     * Dihitung dengan Manhattan Distance.
     */
    private int h;

    /**
     * f(n) = g(n) + h(n). Nilai inilah yang digunakan PriorityQueue
     * untuk menentukan node mana yang diproses lebih dulu.
     * Semakin kecil f(n), semakin prioritas node tersebut.
     */
    private int f;

    /**
     * Nomor urut node saat dimasukkan ke antrean (untuk keperluan
     * tracing/output).
     */
    private int nomor;

    /**
     * Pointer ke node sebelumnya (parent) untuk rekonstruksi jalur solusi.
     */
    private Node parent;

    public Node(String state, String operator, int g, int h, int nomor, Node parent) {
        this.state = state;
        this.operator = operator;
        this.g = g;
        this.h = h;
        this.f = g + h;
        this.nomor = nomor;
        this.parent = parent;
    }

    public String getState() { return state; }
    public String getOperator() { return operator; }
    public int getG() { return g; }
    public int getH() { return h; }
    public int getF() { return f; }
    public int getLevel() { return g; } // alias untuk kompatibilitas
    public int getNomor() { return nomor; }
    public Node getParent() { return parent; }

    public void setNomor(int nomor) { this.nomor = nomor; }

    @Override
    public String toString() {
        return "Node {"
                + "state: '" + state + '\''
                + ", langkah: '" + (operator.isEmpty() ? "START" : operator) + '\''
                + ", g: " + g
                + ", h: " + h
                + ", f: " + f
                + ", no_urut: " + nomor
                + '}';
    }
}
