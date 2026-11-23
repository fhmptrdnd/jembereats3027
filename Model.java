import java.io.Serializable;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

enum Layanan {
    DINE_IN("Makan di Tempat"),
    DELIVERY("Antar ke Alamat"),
    PICKUP("Ambil Sendiri");

    private final String label;

    Layanan(String label) { this.label = label; }

    public String getLabel() { return label; }
}

class ValidasiException extends RuntimeException {
    ValidasiException(String pesan) { super(pesan); }
}

final class Rupiah {
    private Rupiah() { }

    static String format(double nilai) {
        return "Rp" + String.format(new Locale("id", "ID"), "%,.0f", nilai);
    }
}

final class Teks {
    private Teks() { }

    static String garis(int panjang) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < panjang; i++) sb.append('=');
        return sb.toString();
    }
}

final class JamSimulasi {
    private static LocalTime sekarang = LocalTime.of(12, 0);

    private JamSimulasi() { }

    static LocalTime get() { return sekarang; }

    static void set(LocalTime waktu) { sekarang = waktu; }
}

class MenuItem {
    private final String nama;
    private final double harga;
    private boolean tersedia = true;

    MenuItem(String nama, double harga) {
        this.nama = nama;
        this.harga = harga;
    }

    String getNama() { return nama; }

    double getHarga() { return harga; }

    boolean isTersedia() { return tersedia; }

    void setTersedia(boolean tersedia) { this.tersedia = tersedia; }
}

class ItemPesanan implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String namaMenu;
    private final int qty;
    private final double harga;

    ItemPesanan(String namaMenu, int qty, double harga) {
        this.namaMenu = namaMenu;
        this.qty = qty;
        this.harga = harga;
    }

    String getNamaMenu() { return namaMenu; }

    int getQty() { return qty; }

    double getHarga() { return harga; }

    double getSubtotal() { return qty * harga; }
}

class Restoran {
    private final String nama;
    private final LocalTime buka;
    private final LocalTime tutup;
    private final List<MenuItem> menu = new ArrayList<>();

    Restoran(String nama, LocalTime buka, LocalTime tutup) {
        this.nama = nama;
        this.buka = buka;
        this.tutup = tutup;
    }

    Restoran tambahMenu(String namaMenu, double harga) {
        menu.add(new MenuItem(namaMenu, harga));
        return this;
    }

    String getNama() { return nama; }

    List<MenuItem> getMenu() { return menu; }

    MenuItem cariMenu(String namaMenu) {
        for (MenuItem m : menu) {
            if (m.getNama().equalsIgnoreCase(namaMenu)) return m;
        }
        return null;
    }

    boolean sedangBuka(LocalTime t) {
        return !t.isBefore(buka) && t.isBefore(tutup);
    }

    String jamOperasional() { return buka + " - " + tutup; }
}

class Pelanggan {
    private final String id;
    private final String nama;
    private double saldo;

    Pelanggan(String id, String nama, double saldo) {
        this.id = id;
        this.nama = nama;
        this.saldo = saldo;
    }

    String getId() { return id; }

    String getNama() { return nama; }

    double getSaldo() { return saldo; }

    void tambahSaldo(double jumlah) { saldo += jumlah; }

    void kurangiSaldo(double jumlah) { saldo -= jumlah; }
}
