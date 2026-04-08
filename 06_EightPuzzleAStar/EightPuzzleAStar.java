
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

// =============================================================================
// Eight Puzzle Solver - Algoritma A* (A-Star)
//
// APA ITU A*?
// A* adalah algoritma pencarian yang menggabungkan dua hal:
//   1. g(n) = biaya nyata dari state awal ke state saat ini (jumlah langkah)
//   2. h(n) = estimasi biaya dari state saat ini ke tujuan (heuristik)
//
// Keduanya dijumlahkan menjadi: f(n) = g(n) + h(n)
// Node dengan nilai f(n) TERKECIL akan diproses lebih dulu.
//
// Analogi: kamu sedang mencari rute ke suatu tempat.
// g(n) = jarak yang sudah kamu tempuh.
// h(n) = perkiraan jarak tersisa (misalnya jarak lurus ke tujuan).
// A* memilih jalan yang total estimasinya paling pendek — bukan hanya yang
// paling dekat dari awal, dan bukan hanya yang kelihatan paling dekat ke tujuan.
// Hasilnya: jalur OPTIMAL dan EFISIEN.
//
// PERBANDINGAN DENGAN BFS:
// - BFS hanya mempertimbangkan g(n) secara implisit (semua langkah setara).
// - A* mempertimbangkan KEDUA g(n) dan h(n), sehingga lebih terarah.
// - Jika h(n) selalu 0, A* berperilaku sama seperti BFS/Dijkstra.
// - Jika h(n) tidak pernah melebihi biaya nyata (admissible), A* PASTI optimal.
//
// HEURISTIK YANG DIGUNAKAN: Manhattan Distance
// Untuk setiap angka (selain ruang kosong), hitung jarak vertikal + horizontal
// dari posisinya saat ini ke posisi tujuannya.
//
// Contoh: angka '5' saat ini di baris 2 kolom 0, tujuannya di baris 1 kolom 2.
//   h kontribusi dari '5' = |2-1| + |0-2| = 1 + 2 = 3
//
// h(n) total = jumlah Manhattan Distance semua angka (kecuali '0').
//
// Manhattan Distance bersifat ADMISSIBLE karena tidak pernah melebih-lebihkan
// biaya nyata (setiap angka butuh minimal sebanyak jaraknya untuk sampai ke tujuan).
//
// REPRESENTASI PUZZLE
// Papan 3x3 disimpan sebagai String 9 karakter, dibaca baris per baris:
//
//   Papan nyata:    String di program:
//   1 | 2 | 3
//   ---------   ->  "123456780"
//   4 | 5 | 6
//   ---------
//   7 | 8 | 0   <- angka '0' = ruang kosong
//
// Setiap karakter punya nomor urut posisi (indeks) dari 0 sampai 8:
//
//   indeks:  0 | 1 | 2   -> baris pertama  (atas)
//            ---------
//            3 | 4 | 5   -> baris kedua    (tengah)
//            ---------
//            6 | 7 | 8   -> baris ketiga   (bawah)
//
// STRUKTUR DATA YANG DIGUNAKAN
// - PriorityQueue (openPQ)  : antrean berprioritas. Node dengan f(n) terkecil
//                             selalu diambil lebih dulu, bukan yang paling lama
//                             menunggu (berbeda dengan Queue di BFS).
//
// - Set (visitedStates)     : daftar susunan papan yang sudah pernah dikunjungi,
//                             agar tidak memproses papan yang sama dua kali.
// =============================================================================
public class EightPuzzleAStar {

    private final String initialState; // Kondisi awal puzzle
    private final String goalState;    // Kondisi tujuan yang ingin dicapai

    // PriorityQueue = antrean berprioritas. Node dengan f(n) terkecil diambil lebih dulu.
    // Inilah perbedaan utama dibanding BFS yang menggunakan Queue biasa (FIFO).
    private final PriorityQueue<Node> openPQ = new PriorityQueue<>(
            Comparator.comparingInt(Node::getF)
    );

    // Set = kumpulan data unik. Digunakan untuk menyimpan susunan papan
    // yang sudah pernah dikunjungi agar tidak diperiksa ulang.
    private final Set<String> visitedStates = new HashSet<>();

    // Penghitung untuk memberi nomor urut pada setiap node yang dibuat
    private int nomor = 1;

    public EightPuzzleAStar(String initialState, String goalState) {
        this.initialState = initialState;
        this.goalState = goalState;
    }

    // =========================================================================
    // HEURISTIK: MANHATTAN DISTANCE
    //
    // Untuk setiap angka 1-8, hitung berapa langkah minimum yang dibutuhkan
    // untuk mencapai posisi tujuannya jika tidak ada halangan.
    //
    // Caranya:
    //   1. Temukan posisi angka tersebut di state saat ini -> (barisSaatIni, kolomSaatIni)
    //   2. Temukan posisi angka tersebut di goalState     -> (barisTujuan, kolomTujuan)
    //   3. Kontribusi angka ini = |barisSaatIni - barisTujuan| + |kolomSaatIni - kolomTujuan|
    //
    // h(n) total = jumlah kontribusi semua angka (selain '0').
    //
    // Mengapa tidak menghitung '0'?
    // Karena '0' adalah ruang kosong, bukan angka yang harus dipindahkan.
    // Memasukkan '0' dalam hitungan tidak mengubah admissibility, tapi konvensinya
    // adalah tidak menghitungnya.
    // =========================================================================
    private int manhattanDistance(String state) {
        int total = 0;
        for (int i = 0; i < 9; i++) {
            char tile = state.charAt(i);
            if (tile == '0') continue; // Lewati ruang kosong

            // Posisi tile saat ini
            int currentRow = i / 3;
            int currentCol = i % 3;

            // Posisi tile di goalState
            int goalIndex = goalState.indexOf(tile);
            int goalRow = goalIndex / 3;
            int goalCol = goalIndex % 3;

            total += Math.abs(currentRow - goalRow) + Math.abs(currentCol - goalCol);
        }
        return total;
    }

    // =========================================================================
    // OPERATOR PERGERAKAN
    // Ada 4 gerakan yang mungkin: atas, bawah, kiri, kanan.
    // Setiap method mencoba menggeser ruang kosong ('0') ke satu arah.
    // Jika gerakan valid, state baru dibuat dan dimasukkan ke PriorityQueue.
    //
    // Perbedaan dengan BFS: setiap state baru dihitung h(n)-nya (Manhattan Distance)
    // dan disimpan ke dalam node, lalu PriorityQueue otomatis mengurutkan berdasarkan f(n).
    // =========================================================================

    // -------------------------------------------------------------------------
    // GESER KE ATAS
    // Angka yang ada DI ATAS ruang kosong turun mengisi ruang kosong.
    // SYARAT: '0' tidak boleh ada di baris paling atas (emptyPos > 2)
    // -------------------------------------------------------------------------
    private void up(Node node) {
        String state = node.getState();
        int emptyPos = state.indexOf("0");

        if (emptyPos > 2) {
            String newState = state.substring(0, emptyPos - 3) + "0"
                    + state.substring(emptyPos - 2, emptyPos)
                    + state.charAt(emptyPos - 3)
                    + state.substring(emptyPos + 1);

            int newG = node.getG() + 1;
            int newH = manhattanDistance(newState);
            addNodeToQueue(new Node(newState, "up", newG, newH, 0, node));
        }
    }

    // -------------------------------------------------------------------------
    // GESER KE BAWAH
    // Angka yang ada DI BAWAH ruang kosong naik mengisi ruang kosong.
    // SYARAT: '0' tidak boleh ada di baris paling bawah (emptyPos < 6)
    // -------------------------------------------------------------------------
    private void down(Node node) {
        String state = node.getState();
        int emptyPos = state.indexOf("0");

        if (emptyPos < 6) {
            String newState = state.substring(0, emptyPos)
                    + state.substring(emptyPos + 3, emptyPos + 4)
                    + state.substring(emptyPos + 1, emptyPos + 3)
                    + "0"
                    + state.substring(emptyPos + 4);

            int newG = node.getG() + 1;
            int newH = manhattanDistance(newState);
            addNodeToQueue(new Node(newState, "down", newG, newH, 0, node));
        }
    }

    // -------------------------------------------------------------------------
    // GESER KE KIRI
    // Angka yang ada DI KIRI ruang kosong berpindah ke kanan mengisi ruang kosong.
    // SYARAT: '0' tidak boleh ada di kolom paling kiri (bukan indeks 0, 3, 6)
    // -------------------------------------------------------------------------
    private void left(Node node) {
        String state = node.getState();
        int emptyPos = state.indexOf("0");

        if (emptyPos != 0 && emptyPos != 3 && emptyPos != 6) {
            String newState = state.substring(0, emptyPos - 1)
                    + "0"
                    + state.charAt(emptyPos - 1)
                    + state.substring(emptyPos + 1);

            int newG = node.getG() + 1;
            int newH = manhattanDistance(newState);
            addNodeToQueue(new Node(newState, "left", newG, newH, 0, node));
        }
    }

    // -------------------------------------------------------------------------
    // GESER KE KANAN
    // Angka yang ada DI KANAN ruang kosong berpindah ke kiri mengisi ruang kosong.
    // SYARAT: '0' tidak boleh ada di kolom paling kanan (bukan indeks 2, 5, 8)
    // -------------------------------------------------------------------------
    private void right(Node node) {
        String state = node.getState();
        int emptyPos = state.indexOf("0");

        if (emptyPos != 2 && emptyPos != 5 && emptyPos != 8) {
            String newState = state.substring(0, emptyPos)
                    + state.charAt(emptyPos + 1)
                    + "0"
                    + state.substring(emptyPos + 2);

            int newG = node.getG() + 1;
            int newH = manhattanDistance(newState);
            addNodeToQueue(new Node(newState, "right", newG, newH, 0, node));
        }
    }

    // -------------------------------------------------------------------------
    // SOLVE - Method utama yang menjalankan algoritma A*
    //
    // LANGKAH-LANGKAH ALGORITMA A*:
    //
    //   1. Hitung h(n) untuk state awal, masukkan ke PriorityQueue.
    //   2. Ambil node dengan f(n) = g(n) + h(n) terkecil dari PriorityQueue.
    //      a. Apakah ini kondisi tujuan? -> Selesai! Cetak urutan langkahnya.
    //      b. Bukan tujuan? -> Coba semua 4 arah gerakan.
    //         Setiap gerakan valid dihitung f(n)-nya dan masuk PriorityQueue.
    //   3. Ulangi langkah 2 sampai tujuan ditemukan atau PriorityQueue habis.
    //
    // KENAPA A* LEBIH EFISIEN DARI BFS?
    // BFS menjelajahi semua arah secara merata. A* "tahu" arah mana yang lebih
    // menjanjikan berkat heuristik h(n), sehingga lebih sedikit node yang
    // diperiksa sebelum menemukan solusi.
    // -------------------------------------------------------------------------
    public void solve() {
        int startH = manhattanDistance(initialState);
        addNodeToQueue(new Node(initialState, "", 0, startH, 0, null));

        while (!openPQ.isEmpty()) {

            // Ambil node dengan f(n) terkecil dari PriorityQueue
            Node currentNode = openPQ.poll();

            String parentInfo = currentNode.getParent() != null
                    ? "parent: {state: '" + currentNode.getParent().getState() + "'"
                    + ", g: " + currentNode.getParent().getG()
                    + ", no_urut: " + currentNode.getParent().getNomor() + "}"
                    : "parent: null (node awal)";
            System.err.println("Sedang mengecek: " + currentNode + " | " + parentInfo);

            if (currentNode.getState().equals(goalState)) {
                System.out.println("\n=== SOLUSI BERHASIL DITEMUKAN! ===");
                System.out.println("Total langkah: " + currentNode.getG()
                        + " langkah | Node ke-" + currentNode.getNomor() + " yang diperiksa");
                System.out.println("\nUrutan langkah dari awal:");
                printPath(currentNode);
                return;
            }

            left(currentNode);
            up(currentNode);
            right(currentNode);
            down(currentNode);
        }

        System.out.println("Solusi tidak ditemukan.");
    }

    // -------------------------------------------------------------------------
    // ADD NODE TO QUEUE - Memasukkan node ke PriorityQueue jika belum dikunjungi
    // -------------------------------------------------------------------------
    private void addNodeToQueue(Node node) {
        if (!visitedStates.contains(node.getState())) {
            visitedStates.add(node.getState());
            node.setNomor(nomor++);
            openPQ.add(node);
        }
    }

    // -------------------------------------------------------------------------
    // PRINT PATH - Mencetak urutan langkah dari kondisi awal hingga tujuan
    // Menggunakan rekursi untuk menelusuri parent dari tujuan ke awal,
    // lalu mencetak dari awal ke tujuan saat rekursi kembali.
    // -------------------------------------------------------------------------
    private void printPath(Node node) {
        if (node == null) return;
        printPath(node.getParent());

        String langkah = node.getOperator().isEmpty() ? "KONDISI AWAL" : "Geser ke " + node.getOperator();
        System.out.println("Langkah " + node.getG() + ": " + langkah
                + " -> " + formatPuzzle(node.getState())
                + "  [g=" + node.getG() + ", h=" + node.getH() + ", f=" + node.getF() + "]");
    }

    // Mengubah string puzzle menjadi tampilan yang lebih mudah dibaca.
    // Contoh: "123456780" -> "123-456-780"
    private String formatPuzzle(String state) {
        return state.substring(0, 3) + "-" + state.substring(3, 6) + "-" + state.substring(6, 9);
    }

    public static void main(String[] args) {
        // Ganti nilai di bawah untuk mencoba kondisi awal dan tujuan yang berbeda.
        // Gunakan angka 0-8 masing-masing tepat satu kali. '0' = ruang kosong.
        String asal   = "283164705"; // Kondisi awal puzzle (acak)
        String tujuan = "123804765"; // Kondisi tujuan yang ingin dicapai

        System.out.println("Mencari solusi Eight Puzzle dengan A*...");
        System.out.println("Kondisi awal  : " + asal.substring(0, 3) + "-" + asal.substring(3, 6) + "-" + asal.substring(6));
        System.out.println("Kondisi tujuan: " + tujuan.substring(0, 3) + "-" + tujuan.substring(3, 6) + "-" + tujuan.substring(6));
        System.out.println();

        EightPuzzleAStar puzzle = new EightPuzzleAStar(asal, tujuan);
        puzzle.solve();
    }
}
