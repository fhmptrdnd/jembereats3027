import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    private final Scanner in = new Scanner(System.in);

    private final List<Restoran> restoranList = new ArrayList<>();
    private final Pelanggan pelanggan = new Pelanggan("U001", "Andi", 100000);
    private final Keranjang keranjang = new Keranjang();
    private final KeranjangCaretaker caretaker = new KeranjangCaretaker();
    private final JemberEatsMediator mediator = new JemberEatsMediator();
    private final CommandInvoker invoker = new CommandInvoker();
    private final List<Pesanan> daftarPesanan = new ArrayList<>();

    public static void main(String[] args) {
        Main app = new Main();
        try {
            app.jalankan();
        } catch (NoSuchElementException e) {
            System.out.println("\n[Input berakhir]");
            app.keluar();
        }
    }

    private void jalankan() {
        siapkanData();
        System.out.println(Teks.garis(52));
        System.out.println("   JEMBEREATS - Pesan Makanan Online Jember");
        System.out.println(Teks.garis(52));
        tawarkanPemulihanKeranjang();

        boolean jalan = true;
        while (jalan) {
            tampilMenuUtama();
            int pilihan = askInt("Pilih menu: ", 0, 15);
            System.out.println();
            switch (pilihan) {
                case 1: lihatRestoranDanMenu(); break;
                case 2: tambahItem(); break;
                case 3: lihatKeranjang(); break;
                case 4: hapusItem(); break;
                case 5: pasangPromo(); break;
                case 6: undoKeranjang(); break;
                case 7: pulihkanKeranjangDariFile(); break;
                case 8: checkout(); break;
                case 9: daftarPesananSaya(); break;
                case 10: majukanStatus(); break;
                case 11: batalkanPesanan(); break;
                case 12: pesanUlang(); break;
                case 13: undoAksi(); break;
                case 14: riwayatCommand(); break;
                case 15: menuSimulasi(); break;
                default: jalan = false; break;
            }
            if (jalan) {
                tungguEnter();
            }
        }
        keluar();
    }

    private void siapkanData() {
        restoranList.add(new Restoran("Warung Pecel Jember", LocalTime.of(7, 0), LocalTime.of(21, 0))
                .tambahMenu("Nasi Pecel Telur", 15000)
                .tambahMenu("Rawon Daging", 28000)
                .tambahMenu("Es Tape Jember", 12000));
        restoranList.add(new Restoran("Kopi Tegalboto", LocalTime.of(10, 0), LocalTime.of(23, 0))
                .tambahMenu("Kopi Susu Tegalboto", 18000)
                .tambahMenu("Roti Bakar Cokelat", 16000)
                .tambahMenu("Mie Goreng Spesial", 22000));
    }

    private void tampilMenuUtama() {
        System.out.println("\n" + Teks.garis(52));
        System.out.println(" Pelanggan: " + pelanggan.getNama() + " | Saldo: " + Rupiah.format(pelanggan.getSaldo())
                + " | Jam: " + JamSimulasi.get() + " | Keranjang: " + keranjang.getItems().size() + " item");
        System.out.println(Teks.garis(52));
        System.out.println(" 1. Lihat restoran & menu");
        System.out.println(" 2. Tambah item ke keranjang");
        System.out.println(" 3. Lihat keranjang");
        System.out.println(" 4. Hapus item keranjang");
        System.out.println(" 5. Pasang kode promo");
        System.out.println(" 6. Undo perubahan keranjang        [Memento]");
        System.out.println(" 7. Pulihkan keranjang dari file    [Memento]");
        System.out.println(" 8. Checkout                        [Chain, Strategy, Template, Mediator]");
        System.out.println(" 9. Daftar pesanan saya");
        System.out.println("10. Majukan status pesanan          [State, Observer]");
        System.out.println("11. Batalkan pesanan                [Command]");
        System.out.println("12. Pesan ulang                     [Command]");
        System.out.println("13. Undo aksi terakhir              [Command]");
        System.out.println("14. Riwayat command                 [Command]");
        System.out.println("15. Simulasi (jam, menu habis, top up saldo)");
        System.out.println(" 0. Keluar (keranjang otomatis tersimpan)");
    }

    private void tawarkanPemulihanKeranjang() {
        Optional<KeranjangMemento> tersimpan = caretaker.muatDariFile(pelanggan.getId());
        if (tersimpan.isPresent()) {
            System.out.println("Ditemukan keranjang tersimpan: " + tersimpan.get().ringkasan());
            if (askYN("Pulihkan keranjang tersebut?")) {
                keranjang.restore(tersimpan.get());
                System.out.println("Keranjang dipulihkan.");
            }
        }
    }

    private void ubahKeranjang(Runnable aksi) {
        caretaker.catat(pelanggan.getId(), keranjang.createMemento());
        try {
            aksi.run();
        } catch (RuntimeException e) {
            caretaker.undo(pelanggan.getId());
            throw e;
        }
        simpanOtomatis();
    }

    private void simpanOtomatis() {
        caretaker.simpanKeFile(pelanggan.getId(), keranjang.createMemento(), keranjang.isEmpty());
    }

    private void tambahItem() {
        Restoran r = pilihRestoran();
        tampilMenu(r);
        int no = askInt("Nomor menu (0 = batal): ", 0, r.getMenu().size());
        if (no == 0) return;
        MenuItem m = r.getMenu().get(no - 1);
        int qty = askInt("Jumlah: ", 1, 20);
        try {
            ubahKeranjang(() -> keranjang.tambahItem(r, m, qty));
            System.out.println("Ditambahkan: " + qty + " x " + m.getNama() + ". Keranjang tersimpan otomatis.");
        } catch (IllegalStateException e) {
            System.out.println("[GAGAL] " + e.getMessage());
        }
    }

    private void lihatKeranjang() {
        if (keranjang.isEmpty()) {
            System.out.println("Keranjang kosong.");
            return;
        }
        System.out.println("Keranjang dari " + keranjang.getNamaRestoran() + ":");
        int no = 1;
        for (ItemPesanan i : keranjang.getItems()) {
            System.out.println("  " + no++ + ". " + i.getNamaMenu() + " x" + i.getQty() + " = " + Rupiah.format(i.getSubtotal()));
        }
        System.out.println("  Subtotal   : " + Rupiah.format(keranjang.getSubtotal()));
        System.out.println("  Kode promo : " + (keranjang.getKodePromo().isEmpty() ? "-" : keranjang.getKodePromo()));
    }

    private void hapusItem() {
        if (keranjang.isEmpty()) {
            System.out.println("Keranjang kosong.");
            return;
        }
        lihatKeranjang();
        int no = askInt("Nomor item yang dihapus (0 = kosongkan semua, -1 = batal): ", -1, keranjang.getItems().size());
        if (no == -1) return;
        if (no == 0) {
            ubahKeranjang(keranjang::kosongkan);
            System.out.println("Keranjang dikosongkan.");
        } else {
            ubahKeranjang(() -> keranjang.hapusItem(no - 1));
            System.out.println("Item dihapus.");
        }
    }

    private void pasangPromo() {
        System.out.println("Kode promo tersedia:");
        for (Map.Entry<String, DiscountStrategy> e : PromoCatalog.semua().entrySet()) {
            System.out.println("  " + e.getKey() + " -> " + e.getValue().deskripsi());
        }
        String kode = ask("Masukkan kode (kosong = hapus promo): ");
        ubahKeranjang(() -> keranjang.setKodePromo(kode));
        System.out.println(kode.isEmpty() ? "Promo dihapus." : "Kode '" + kode.toUpperCase() + "' dipasang (divalidasi saat checkout).");
    }

    private void undoKeranjang() {
        Optional<KeranjangMemento> m = caretaker.undo(pelanggan.getId());
        if (m.isPresent()) {
            keranjang.restore(m.get());
            simpanOtomatis();
            System.out.println("Perubahan keranjang terakhir dibatalkan.");
            lihatKeranjang();
        } else {
            System.out.println("Tidak ada perubahan keranjang yang bisa di-undo.");
        }
    }

    private void pulihkanKeranjangDariFile() {
        Optional<KeranjangMemento> m = caretaker.muatDariFile(pelanggan.getId());
        if (m.isPresent()) {
            ubahKeranjang(() -> keranjang.restore(m.get()));
            System.out.println("Keranjang dipulihkan dari file: " + m.get().ringkasan());
        } else {
            System.out.println("Tidak ada keranjang tersimpan.");
        }
    }

    private void checkout() {
        if (keranjang.isEmpty()) {
            System.out.println("Keranjang kosong.");
            return;
        }
        Restoran r = cariRestoran(keranjang.getNamaRestoran());

        System.out.println("Pilih layanan:");
        Layanan[] layananList = Layanan.values();
        for (int i = 0; i < layananList.length; i++) {
            System.out.println("  " + (i + 1) + ". " + layananList[i].getLabel());
        }
        Layanan layanan = layananList[askInt("Pilihan: ", 1, layananList.length) - 1];

        Pesanan p = new Pesanan(pelanggan, r, layanan, keranjang.getItems());

        switch (layanan) {
            case DINE_IN:
                p.setDetailLayanan("Meja " + askInt("Nomor meja (1-20): ", 1, 20));
                break;
            case DELIVERY:
                p.setDetailLayanan(ask("Alamat pengantaran: "));
                p.setJarakKm(askDouble("Jarak (km): "));
                System.out.println("Pilih skema ongkir: 1. Per km (Rp2.500/km, min Rp5.000)  2. Flat Rp8.000");
                p.setShipping(askInt("Pilihan: ", 1, 2) == 1 ? new DistanceShipping(2500, 5000) : new FlatShipping(8000));
                break;
            default:
                break;
        }

        System.out.println("Metode pembayaran: 1. Saldo JemberPay  2. Tunai  3. QRIS");
        int bayar = askInt("Pilihan: ", 1, 3);
        p.setPembayaran(bayar == 1 ? new SaldoPayment() : bayar == 2 ? new TunaiPayment() : new QrisPayment());

        p.setKodePromo(keranjang.getKodePromo());
        DiscountStrategy promo = PromoCatalog.cari(keranjang.getKodePromo());
        if (promo != null) p.setDiscount(promo);

        System.out.println("\nRingkasan: subtotal " + Rupiah.format(p.getSubtotal())
                + " | ongkir " + Rupiah.format(p.getOngkir())
                + " | diskon " + Rupiah.format(p.getDiskon())
                + " | TOTAL " + Rupiah.format(p.getTotal()));
        if (!askYN("Lanjutkan pemesanan?")) {
            System.out.println("Checkout dibatalkan.");
            return;
        }

        try {
            mediator.placeOrder(p);
            daftarPesanan.add(p);
            ubahKeranjang(keranjang::kosongkan);
            System.out.println("\nPesanan " + p.getId() + " berhasil dibuat. Keranjang dikosongkan.");
        } catch (ValidasiException e) {
            System.out.println("\n[DITOLAK] " + e.getMessage());
            System.out.println("Keranjang Anda tetap aman, silakan perbaiki lalu coba lagi.");
        }
    }

    private void daftarPesananSaya() {
        if (daftarPesanan.isEmpty()) {
            System.out.println("Belum ada pesanan.");
            return;
        }
        for (Pesanan p : daftarPesanan) {
            System.out.println(p.getId() + " | " + p.getRestoran().getNama() + " | " + p.getLayanan().getLabel()
                    + " | " + p.getState().getName() + " | " + Rupiah.format(p.getTotal())
                    + " | " + p.getPembayaran().nama());
            System.out.println("     Detail: " + (p.getDetailLayanan().isEmpty() ? "-" : p.getDetailLayanan()));
            for (ItemPesanan i : p.getItems()) {
                System.out.println("     - " + i.getNamaMenu() + " x" + i.getQty());
            }
        }
    }

    private void majukanStatus() {
        Pesanan p = pilihPesanan("Pilih pesanan yang dimajukan statusnya:");
        if (p == null) return;
        try {
            p.next();
        } catch (IllegalStateException e) {
            System.out.println("[GAGAL] " + e.getMessage());
        }
    }

    private void batalkanPesanan() {
        Pesanan p = pilihPesanan("Pilih pesanan yang dibatalkan:");
        if (p == null) return;
        try {
            invoker.run(new CancelOrderCommand(p));
        } catch (IllegalStateException e) {
            System.out.println("[GAGAL] " + e.getMessage());
        }
    }

    private void pesanUlang() {
        Pesanan asal = pilihPesanan("Pilih pesanan yang ingin dipesan ulang:");
        if (asal == null) return;
        ReorderCommand cmd = new ReorderCommand(mediator, asal);
        try {
            invoker.run(cmd);
            daftarPesanan.add(cmd.getPesananBaru());
            System.out.println("\nPesanan ulang berhasil: " + cmd.getPesananBaru().getId());
        } catch (ValidasiException | IllegalStateException e) {
            System.out.println("\n[DITOLAK] " + e.getMessage());
        }
    }

    private void undoAksi() {
        try {
            invoker.undoLast();
            System.out.println("Aksi terakhir berhasil di-undo.");
        } catch (IllegalStateException e) {
            System.out.println("[GAGAL] " + e.getMessage());
        }
    }

    private void riwayatCommand() {
        if (invoker.getLog().isEmpty()) {
            System.out.println("Belum ada command yang dijalankan.");
            return;
        }
        for (String baris : invoker.getLog()) System.out.println("  " + baris);
    }

    private Pesanan pilihPesanan(String judul) {
        if (daftarPesanan.isEmpty()) {
            System.out.println("Belum ada pesanan.");
            return null;
        }
        System.out.println(judul);
        for (int i = 0; i < daftarPesanan.size(); i++) {
            Pesanan p = daftarPesanan.get(i);
            System.out.println("  " + (i + 1) + ". " + p.getId() + " | " + p.getRestoran().getNama()
                    + " | " + p.getLayanan().getLabel() + " | " + p.getState().getName());
        }
        return daftarPesanan.get(askInt("Nomor: ", 1, daftarPesanan.size()) - 1);
    }

    private void menuSimulasi() {
        System.out.println("1. Ubah jam simulasi   2. Tandai menu habis/tersedia   3. Top up saldo   0. Kembali");
        int p = askInt("Pilihan: ", 0, 3);
        if (p == 1) {
            String s = ask("Jam baru (HH:mm, contoh 22:30): ");
            try {
                JamSimulasi.set(LocalTime.parse(s));
                System.out.println("Jam simulasi sekarang " + JamSimulasi.get());
            } catch (DateTimeParseException e) {
                System.out.println("[GAGAL] Format jam tidak valid.");
            }
        } else if (p == 2) {
            Restoran r = pilihRestoran();
            tampilMenu(r);
            int no = askInt("Nomor menu (0 = batal): ", 0, r.getMenu().size());
            if (no == 0) return;
            MenuItem m = r.getMenu().get(no - 1);
            m.setTersedia(!m.isTersedia());
            System.out.println(m.getNama() + " sekarang " + (m.isTersedia() ? "TERSEDIA" : "HABIS"));
        } else if (p == 3) {
            double jumlah = askDouble("Jumlah top up (Rp): ");
            if (jumlah > 0) {
                pelanggan.tambahSaldo(jumlah);
                System.out.println("Saldo sekarang " + Rupiah.format(pelanggan.getSaldo()));
            }
        }
    }

    private void lihatRestoranDanMenu() {
        for (Restoran r : restoranList) {
            System.out.println(r.getNama() + " | " + r.jamOperasional() + " | "
                    + (r.sedangBuka(JamSimulasi.get()) ? "BUKA" : "TUTUP"));
            tampilMenu(r);
        }
    }

    private void tampilMenu(Restoran r) {
        int no = 1;
        for (MenuItem m : r.getMenu()) {
            System.out.println("   " + no++ + ". " + m.getNama() + " - " + Rupiah.format(m.getHarga())
                    + (m.isTersedia() ? "" : "  [HABIS]"));
        }
    }

    private Restoran pilihRestoran() {
        System.out.println("Pilih restoran:");
        for (int i = 0; i < restoranList.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + restoranList.get(i).getNama());
        }
        return restoranList.get(askInt("Nomor: ", 1, restoranList.size()) - 1);
    }

    private Restoran cariRestoran(String nama) {
        for (Restoran r : restoranList) {
            if (r.getNama().equals(nama)) return r;
        }
        throw new IllegalStateException("Restoran tidak ditemukan: " + nama);
    }

    private void keluar() {
        simpanOtomatis();
        System.out.println(keranjang.isEmpty()
                ? "Sampai jumpa di JemberEats!"
                : "Keranjang Anda disimpan. Sampai jumpa di JemberEats!");
    }

    private String ask(String prompt) {
        System.out.print(prompt);
        return in.nextLine().trim();
    }

    private int askInt(String prompt, int min, int max) {
        while (true) {
            String s = ask(prompt);
            try {
                int v = Integer.parseInt(s);
                if (v >= min && v <= max) return v;
            } catch (NumberFormatException ignored) {

            }
            System.out.println("   Masukkan angka " + min + " - " + max + ".");
        }
    }

    private double askDouble(String prompt) {
        while (true) {
            String s = ask(prompt).replace(',', '.');
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                System.out.println("   Masukkan angka yang valid.");
            }
        }
    }

    private boolean askYN(String prompt) {
        while (true) {
            String s = ask(prompt + " (y/n): ").toLowerCase();
            if (s.equals("y")) return true;
            if (s.equals("n")) return false;
        }
    }

    private void tungguEnter() {
        System.out.print("\nTekan Enter untuk kembali ke menu...");
        in.nextLine();
    }
}
