
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

// =============================================================================
// Eight Puzzle Solver - Algoritma DFS (Depth-First Search)
//
// APA ITU DFS?
// DFS adalah algoritma pencarian yang menjelajahi satu jalur secara MENDALAM
// sampai tidak bisa lanjut, baru kemudian mundur (backtrack) dan mencoba
// jalur lain.
//
// Analogi: bayangkan kamu menjelajahi labirin. Kamu terus maju ke satu arah
// sampai mentok, lalu balik ke persimpangan sebelumnya dan coba arah lain.
// DFS bekerja persis seperti itu.
//
// PERBEDAAN UTAMA DFS vs BFS:
// ┌─────────────┬──────────────────────────────┬──────────────────────────────┐
// │             │ BFS                          │ DFS                          │
// ├─────────────┼──────────────────────────────┼──────────────────────────────┤
// │ Struktur    │ Queue (FIFO - antrean)       │ Stack (LIFO - tumpukan)      │
// │ data        │ yang masuk duluan diproses   │ yang masuk terakhir          │
// │             │ duluan                       │ diproses duluan              │
// ├─────────────┼──────────────────────────────┼──────────────────────────────┤
// │ Arah        │ Melebar (level per level)    │ Menyelam (satu jalur penuh)  │
// │ jelajah     │                              │ sebelum coba jalur lain      │
// ├─────────────┼──────────────────────────────┼──────────────────────────────┤
// │ Jaminan     │ PASTI jalur terpendek        │ TIDAK dijamin terpendek      │
// │ solusi      │                              │                              │
// └─────────────┴──────────────────────────────┴──────────────────────────────┘
//
// REPRESENTASI PUZZLE (sama dengan BFS)
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
// - Stack (openStack)    : tumpukan node yang menunggu untuk diperiksa.
//                          Node yang dimasukkan TERAKHIR akan diambil PERTAMA
//                          (seperti tumpukan piring — ambil dari atas).
//                          Sifat inilah yang membuat DFS menyelam lebih dalam
//                          sebelum mencoba jalur lain.
//                          Di Java diimplementasikan dengan ArrayDeque.
//
// - Set (visitedStates)  : daftar susunan papan yang sudah pernah dikunjungi,
//                          agar tidak memproses papan yang sama dua kali dan
//                          menghindari loop tak terbatas.
// =============================================================================
public class EightPuzzleDFS {

    private final String initialState; // Kondisi awal puzzle
    private final String goalState;    // Kondisi tujuan yang ingin dicapai

    // Stack = tumpukan. Node yang dimasukkan TERAKHIR akan diproses PERTAMA (LIFO).
    // ArrayDeque digunakan sebagai Stack karena lebih efisien dari class Stack bawaan Java.
    // push() = masukkan ke atas tumpukan, pop() = ambil dari atas tumpukan.
    private final Deque<Node> openStack = new ArrayDeque<>();

    // Set = kumpulan data unik. Digunakan untuk menyimpan susunan papan
    // yang sudah pernah dikunjungi agar tidak diperiksa ulang.
    private final Set<String> visitedStates = new HashSet<>();

    // Penghitung untuk memberi nomor urut pada setiap node yang dibuat
    private int nomor = 1;

    public EightPuzzleDFS(String initialState, String goalState) {
        this.initialState = initialState;
        this.goalState = goalState;
    }

    // =========================================================================
    // OPERATOR PERGERAKAN
    // Ada 4 gerakan yang mungkin: atas, bawah, kiri, kanan.
    // Setiap method mencoba menggeser ruang kosong ('0') ke satu arah.
    // Jika gerakan tidak melanggar batas papan, state baru dibuat dan
    // dimasukkan ke TUMPUKAN untuk diperiksa nanti.
    //
    // Perhatikan: di DFS, urutan pemanggilan gerakan mempengaruhi arah mana
    // yang "dijelajahi lebih dulu", karena yang dipanggil TERAKHIR akan berada
    // di ATAS tumpukan dan diproses duluan.
    // =========================================================================
    // -------------------------------------------------------------------------
    // GESER KE ATAS
    // Angka yang ada DI ATAS ruang kosong turun mengisi ruang kosong.
    //
    // Contoh:
    //   Sebelum:  1 | 2 | 3       Sesudah:  1 | 0 | 3
    //             ---------   ->            ---------
    //             4 | 0 | 5                 4 | 2 | 5
    //             ---------                 ---------
    //             6 | 7 | 8                 6 | 7 | 8
    //
    // SYARAT: '0' tidak boleh ada di baris paling atas (tidak ada baris di atasnya).
    //
    //   indeks:  [ 0  1  2 ]  <- baris atas   -> TIDAK BISA naik
    //            [ 3  4  5 ]  <- baris tengah  -> bisa naik
    //            [ 6  7  8 ]  <- baris bawah   -> bisa naik
    //
    //   Baris atas = indeks 0, 1, 2.
    //   Jadi syaratnya: emptyPos > 2
    //
    // CARA MENUKAR DI DALAM STRING
    // Puzzle disimpan sebagai String, jadi tidak bisa diubah langsung per karakter.
    // Kita harus potong-potong lalu sambung ulang.
    //
    // Contoh konkret: state = "142053678", emptyPos = 4 (posisi '0')
    //
    //   Papan:   1 | 4 | 2       String: "1 4 2 0 5 3 6 7 8"
    //            ---------               indeks: 0 1 2 3 4 5 6 7 8
    //            5 | 0 | 3
    //            ---------
    //            6 | 7 | 8
    //
    //   Angka di ATAS '0' = indeks emptyPos - 3 = 1 = '4'
    //   (kenapa -3? karena setiap baris punya 3 kolom, naik 1 baris = mundur 3 indeks)
    //
    //   Cara menyusun String baru (5 potongan):
    //   ① state.substring(0, emptyPos-3) = "1"    -> semua sebelum angka atas
    //   ② "0"                                     -> '0' pindah ke posisi angka atas
    //   ③ state.substring(emptyPos-2, emptyPos) = "25" -> karakter di antaranya
    //   ④ state.charAt(emptyPos-3)               = "4" -> angka atas turun ke posisi '0' lama
    //   ⑤ state.substring(emptyPos+1)            = "3678" -> sisa setelah '0' lama
    //
    //   Hasilnya: "1" + "0" + "25" + "4" + "3678" = "102543678"
    // -------------------------------------------------------------------------
    private void up(Node node) {
        String state = node.getState();
        int emptyPos = state.indexOf("0"); // Cari posisi indeks dari ruang kosong

        // Boleh geser ke atas hanya jika '0' bukan di baris paling atas (indeks > 2)
        if (emptyPos > 2) {
            String newState = state.substring(0, emptyPos - 3) + "0"
                    + state.substring(emptyPos - 2, emptyPos)
                    + state.charAt(emptyPos - 3)
                    + state.substring(emptyPos + 1);

            int newLevel = node.getLevel() + 1;
            addNodeToStack(new Node(newState, "up", newLevel, 0, node));
        }
    }

    // -------------------------------------------------------------------------
    // GESER KE BAWAH
    // Angka yang ada DI BAWAH ruang kosong naik mengisi ruang kosong.
    //
    // Contoh:
    //   Sebelum:  1 | 2 | 3       Sesudah:  1 | 2 | 3
    //             ---------   ->            ---------
    //             4 | 0 | 5                 4 | 7 | 5
    //             ---------                 ---------
    //             6 | 7 | 8                 6 | 0 | 8
    //
    // SYARAT: '0' tidak boleh ada di baris paling bawah (tidak ada baris di bawahnya).
    //
    //   indeks:  [ 0  1  2 ]  <- baris atas   -> bisa turun
    //            [ 3  4  5 ]  <- baris tengah  -> bisa turun
    //            [ 6  7  8 ]  <- baris bawah   -> TIDAK BISA turun
    //
    //   Baris bawah = indeks 6, 7, 8.
    //   Jadi syaratnya: emptyPos < 6
    //
    // CARA MENUKAR DI DALAM STRING
    // Angka di BAWAH '0' = indeks emptyPos + 3
    // (kenapa +3? karena turun 1 baris = maju 3 indeks)
    //
    //   ① state.substring(0, emptyPos)            -> semua sebelum '0'
    //   ② state.charAt(emptyPos+3)                -> angka bawah naik ke posisi '0'
    //   ③ state.substring(emptyPos+1, emptyPos+3) -> karakter di antaranya
    //   ④ "0"                                     -> '0' turun ke posisi angka bawah tadi
    //   ⑤ state.substring(emptyPos+4)             -> sisa setelahnya
    // -------------------------------------------------------------------------
    private void down(Node node) {
        String state = node.getState();
        int emptyPos = state.indexOf("0");

        // Boleh geser ke bawah hanya jika '0' bukan di baris paling bawah (indeks < 6)
        if (emptyPos < 6) {
            String newState = state.substring(0, emptyPos)
                    + state.substring(emptyPos + 3, emptyPos + 4)
                    + state.substring(emptyPos + 1, emptyPos + 3)
                    + "0"
                    + state.substring(emptyPos + 4);

            int newLevel = node.getLevel() + 1;
            addNodeToStack(new Node(newState, "down", newLevel, 0, node));
        }
    }

    // -------------------------------------------------------------------------
    // GESER KE KIRI
    // Angka yang ada DI KIRI ruang kosong berpindah ke kanan mengisi ruang kosong.
    //
    // Contoh:
    //   Sebelum:  1 | 2 | 3       Sesudah:  1 | 2 | 3
    //             ---------   ->            ---------
    //             4 | 5 | 0                 4 | 0 | 5
    //             ---------                 ---------
    //             6 | 7 | 8                 6 | 7 | 8
    //
    // SYARAT: '0' tidak boleh ada di kolom paling kiri.
    //
    //   indeks:  [(0) 1   2 ]  <- indeks 0 = kolom kiri -> DILARANG
    //            [(3) 4   5 ]  <- indeks 3 = kolom kiri -> DILARANG
    //            [(6) 7   8 ]  <- indeks 6 = kolom kiri -> DILARANG
    //
    //   Kenapa perlu dicek? Karena String bersambung terus tanpa jeda baris.
    //   Kalau '0' ada di indeks 3 (awal baris ke-2) dan kita geser kiri,
    //   program akan mengambil indeks 2 yang merupakan akhir baris ke-1.
    //   Di papan nyata mereka berbeda baris, tapi di String indeksnya berdekatan!
    //
    // CARA MENUKAR DI DALAM STRING
    // Angka di KIRI '0' = indeks emptyPos - 1
    //
    //   ① state.substring(0, emptyPos-1) -> semua sebelum angka kiri
    //   ② "0"                            -> '0' pindah ke posisi angka kiri
    //   ③ state.charAt(emptyPos-1)       -> angka kiri pindah ke posisi '0' lama
    //   ④ state.substring(emptyPos+1)    -> sisa setelah '0' lama
    // -------------------------------------------------------------------------
    private void left(Node node) {
        String state = node.getState();
        int emptyPos = state.indexOf("0");

        // Boleh geser ke kiri hanya jika '0' bukan di kolom paling kiri
        if (emptyPos != 0 && emptyPos != 3 && emptyPos != 6) {
            String newState = state.substring(0, emptyPos - 1)
                    + "0"
                    + state.charAt(emptyPos - 1)
                    + state.substring(emptyPos + 1);

            int newLevel = node.getLevel() + 1;
            addNodeToStack(new Node(newState, "left", newLevel, 0, node));
        }
    }

    // -------------------------------------------------------------------------
    // GESER KE KANAN
    // Angka yang ada DI KANAN ruang kosong berpindah ke kiri mengisi ruang kosong.
    //
    // Contoh:
    //   Sebelum:  1 | 2 | 3       Sesudah:  1 | 2 | 3
    //             ---------   ->            ---------
    //             0 | 4 | 5                 4 | 0 | 5
    //             ---------                 ---------
    //             6 | 7 | 8                 6 | 7 | 8
    //
    // SYARAT: '0' tidak boleh ada di kolom paling kanan.
    //
    //   indeks:  [ 0   1  (2)]  <- indeks 2 = kolom kanan -> DILARANG
    //            [ 3   4  (5)]  <- indeks 5 = kolom kanan -> DILARANG
    //            [ 6   7  (8)]  <- indeks 8 = kolom kanan -> DILARANG
    //
    // CARA MENUKAR DI DALAM STRING
    // Angka di KANAN '0' = indeks emptyPos + 1
    //
    //   ① state.substring(0, emptyPos)  -> semua sebelum '0'
    //   ② state.charAt(emptyPos+1)      -> angka kanan pindah ke posisi '0'
    //   ③ "0"                           -> '0' pindah ke posisi angka kanan tadi
    //   ④ state.substring(emptyPos+2)   -> sisa setelah angka kanan
    // -------------------------------------------------------------------------
    private void right(Node node) {
        String state = node.getState();
        int emptyPos = state.indexOf("0");

        // Boleh geser ke kanan hanya jika '0' bukan di kolom paling kanan
        if (emptyPos != 2 && emptyPos != 5 && emptyPos != 8) {
            String newState = state.substring(0, emptyPos)
                    + state.charAt(emptyPos + 1)
                    + "0"
                    + state.substring(emptyPos + 2);

            int newLevel = node.getLevel() + 1;
            addNodeToStack(new Node(newState, "right", newLevel, 0, node));
        }
    }

    // -------------------------------------------------------------------------
    // SOLVE - Method utama yang menjalankan algoritma DFS
    //
    // LANGKAH-LANGKAH ALGORITMA DFS:
    //
    //   1. Masukkan kondisi awal puzzle ke dalam tumpukan (stack).
    //   2. Ambil kondisi paling ATAS tumpukan (yang paling baru dimasukkan), lalu cek:
    //      a. Apakah ini kondisi tujuan? -> Selesai! Cetak urutan langkahnya.
    //      b. Bukan tujuan? -> Coba semua 4 arah gerakan.
    //         Setiap gerakan valid menghasilkan kondisi baru -> masuk ke ATAS tumpukan.
    //   3. Ulangi langkah 2 sampai tujuan ditemukan atau tumpukan habis.
    //
    // KENAPA DFS TIDAK SELALU MENEMUKAN JALUR TERPENDEK?
    // DFS langsung menyelam dalam satu jalur. Bayangkan tumpukan piring:
    //
    //   Kondisi awal  -> push A
    //   Tumpukan: [A]
    //
    //   Proses A -> hasilkan B, C, D -> push semua
    //   Tumpukan: [B, C, D]  (D di atas = diproses duluan)
    //
    //   Proses D -> hasilkan E, F -> push semua
    //   Tumpukan: [B, C, E, F]  (F di atas = diproses duluan)
    //
    //   DFS terus menyelam di jalur D -> F -> ...
    //   bukan melebar ke B, C dulu seperti BFS.
    //
    // Hasilnya: DFS menemukan solusi lebih cepat (memori lebih hemat),
    // tapi jalur yang ditemukan bisa lebih panjang dari jalur optimal.
    // -------------------------------------------------------------------------
    public void solve() {
        // Langkah 1: Masukkan kondisi awal. Level=0, belum ada gerakan, tidak ada parent.
        addNodeToStack(new Node(initialState, "", 0, 0, null));

        // Langkah 2: Terus proses selama tumpukan masih ada isinya
        while (!openStack.isEmpty()) {

            // Ambil node paling ATAS tumpukan (yang paling baru dimasukkan = LIFO)
            Node currentNode = openStack.pop();
            currentNode.setNomor(nomor++); // Nomor urut diberikan saat node diperiksa

            // Cetak ke stderr agar log progres tidak tercampur dengan output solusi
            String parentInfo = currentNode.getParent() != null
                    ? "parent: {state: '" + currentNode.getParent().getState() + "'"
                    + ", level: " + currentNode.getParent().getLevel()
                    + ", no_urut: " + currentNode.getParent().getNomor() + "}"
                    : "parent: null (node awal)";
            System.err.println("Sedang mengecek: " + currentNode + " | " + parentInfo);

            // Cek apakah kondisi ini adalah kondisi tujuan
            if (currentNode.getState().equals(goalState)) {
                System.out.println("\n=== SOLUSI BERHASIL DITEMUKAN! ===");
                System.out.println("Total langkah: " + currentNode.getLevel()
                        + " langkah | Node ke-" + currentNode.getNomor() + " yang diperiksa");
                System.out.println("\nUrutan langkah dari awal:");
                printPath(currentNode);
                return;
            }

            // Bukan tujuan -> coba semua 4 arah gerakan dari kondisi ini.
            // PERHATIAN: Di DFS, urutan push mempengaruhi arah yang dijelajahi duluan.
            // Gerakan yang dipush TERAKHIR akan berada di ATAS tumpukan dan diproses duluan.
            // Agar diproses dengan urutan left->up->right->down, push dilakukan terbalik.
            down(currentNode);
            right(currentNode);
            up(currentNode);
            left(currentNode);
        }

        System.out.println("Solusi tidak ditemukan.");
    }

    // -------------------------------------------------------------------------
    // ADD NODE TO STACK - Memasukkan node ke tumpukan jika belum pernah dikunjungi
    //
    // Kenapa perlu dicek dulu?
    // Tanpa pengecekan ini, DFS bisa terjebak mengulang kondisi yang sama
    // selamanya karena sifatnya yang menyelam terus ke satu arah.
    //
    // Perbedaan dengan BFS (struktur data):
    // BFS -> openQueue.add(node)   : node masuk ke BELAKANG antrean (FIFO)
    // DFS -> openStack.push(node)  : node masuk ke ATAS tumpukan   (LIFO)
    //
    // Kenapa menggunakan Set, bukan List biasa?
    // Set menyimpan data tanpa duplikat dan pengecekan "sudah ada atau belum"
    // berlangsung sangat cepat — tidak peduli ada 10 atau 100.000 kondisi
    // tersimpan, kecepatannya tetap sama.
    // -------------------------------------------------------------------------
    private void addNodeToStack(Node node) {
        if (!visitedStates.contains(node.getState())) {
            visitedStates.add(node.getState()); // Tandai kondisi ini sudah pernah dikunjungi
            openStack.push(node);               // Masukkan ke ATAS tumpukan untuk diproses nanti
        }
    }

    // -------------------------------------------------------------------------
    // PRINT PATH - Mencetak urutan langkah dari kondisi awal hingga tujuan
    //
    // Cara kerjanya:
    // Setiap node menyimpan node "sebelumnya" (parent). Misalnya:
    //   node tujuan -> node langkah ke-N -> ... -> node langkah ke-1 -> node awal -> null
    //
    // Kita ingin mencetak dari awal ke akhir, tapi yang kita pegang adalah node
    // TUJUAN. Solusinya: gunakan rekursi untuk mundur ke node awal dulu,
    // baru cetak saat kembali ke depan.
    //
    // Analogi: seperti tumpukan piring. Kita ambil satu per satu dari atas
    // (mundur ke awal), taruh di tempat, lalu cetak dari piring paling bawah
    // (kondisi awal) ke piring paling atas (kondisi tujuan).
    // -------------------------------------------------------------------------
    private void printPath(Node node) {
        // Kumpulkan semua node dari tujuan ke awal, lalu cetak terbalik
        java.util.Deque<Node> path = new java.util.ArrayDeque<>();
        while (node != null) {
            path.push(node);
            node = node.getParent();
        }
        while (!path.isEmpty()) {
            Node n = path.pop();
            String langkah = n.getOperator().isEmpty() ? "KONDISI AWAL" : "Geser ke " + n.getOperator();
            System.out.println("Langkah " + n.getLevel() + ": " + langkah
                    + " -> " + formatPuzzle(n.getState()));
        }
    }

    // Mengubah string puzzle menjadi tampilan yang lebih mudah dibaca.
    // Contoh: "123456780" -> "123-456-780"  (tanda '-' memisahkan tiap baris)
    private String formatPuzzle(String state) {
        return state.substring(0, 3) + "-" + state.substring(3, 6) + "-" + state.substring(6, 9);
    }

    public static void main(String[] args) {
        // Ganti nilai di bawah untuk mencoba kondisi awal dan tujuan yang berbeda.
        // Gunakan angka 0-8 masing-masing tepat satu kali. '0' = ruang kosong.
        // String tujuan = "123804765"; // Kondisi tujuan yang ingin dicapai
        // String tujuan = "283104765"; // Kondisi tujuan yang ingin dicapai
        String asal = "283164705"; // Kondisi awal puzzle (acak)
        String tujuan = "830264175"; // Kondisi tujuan yang ingin dicapai

        System.out.println("Mencari solusi Eight Puzzle dengan DFS...");
        System.out.println("Kondisi awal  : " + asal.substring(0, 3) + "-" + asal.substring(3, 6) + "-" + asal.substring(6));
        System.out.println("Kondisi tujuan: " + tujuan.substring(0, 3) + "-" + tujuan.substring(3, 6) + "-" + tujuan.substring(6));
        System.out.println();

        EightPuzzleDFS puzzle = new EightPuzzleDFS(asal, tujuan);
        puzzle.solve();
    }
}
