import java.util.LinkedHashMap;
import java.util.Map;

interface ShippingStrategy {
    double hitungOngkir(Pesanan p);

    String nama();
}

class FlatShipping implements ShippingStrategy {
    private final double tarif;

    FlatShipping(double tarif) { this.tarif = tarif; }

    public double hitungOngkir(Pesanan p) { return tarif; }

    public String nama() { return "Ongkir Flat"; }
}

class DistanceShipping implements ShippingStrategy {
    private final double tarifPerKm;
    private final double minimal;

    DistanceShipping(double tarifPerKm, double minimal) {
        this.tarifPerKm = tarifPerKm;
        this.minimal = minimal;
    }

    public double hitungOngkir(Pesanan p) {
        return Math.max(minimal, p.getJarakKm() * tarifPerKm);
    }

    public String nama() { return "Ongkir per Km"; }
}

class FreeShipping implements ShippingStrategy {
    public double hitungOngkir(Pesanan p) { return 0; }

    public String nama() { return "Tanpa Ongkir"; }
}

interface DiscountStrategy {
    double hitungDiskon(Pesanan p);

    default boolean berlaku(Pesanan p) { return true; }

    String deskripsi();
}

class NoDiscount implements DiscountStrategy {
    public double hitungDiskon(Pesanan p) { return 0; }

    public String deskripsi() { return "Tanpa diskon"; }
}

class PercentageDiscount implements DiscountStrategy {
    private final double persen;
    private final double maksimal;

    PercentageDiscount(double persen, double maksimal) {
        this.persen = persen;
        this.maksimal = maksimal;
    }

    public double hitungDiskon(Pesanan p) {
        return Math.min(p.getSubtotal() * persen / 100.0, maksimal);
    }

    public String deskripsi() {
        return "Diskon " + (int) persen + "% (maks " + Rupiah.format(maksimal) + ")";
    }
}

class VoucherDiscount implements DiscountStrategy {
    private final double nominal;
    private final double minimalBelanja;

    VoucherDiscount(double nominal, double minimalBelanja) {
        this.nominal = nominal;
        this.minimalBelanja = minimalBelanja;
    }

    public double hitungDiskon(Pesanan p) { return berlaku(p) ? nominal : 0; }

    public boolean berlaku(Pesanan p) { return p.getSubtotal() >= minimalBelanja; }

    public String deskripsi() {
        return "Voucher " + Rupiah.format(nominal) + " (min. belanja " + Rupiah.format(minimalBelanja) + ")";
    }
}

final class PromoCatalog {
    private static final Map<String, DiscountStrategy> KATALOG = new LinkedHashMap<>();

    static {
        KATALOG.put("HEMAT10", new PercentageDiscount(10, 15000));
        KATALOG.put("JEMBER5K", new VoucherDiscount(5000, 30000));
        KATALOG.put("MAKANRAME", new PercentageDiscount(25, 20000));
    }

    private PromoCatalog() { }

    static DiscountStrategy cari(String kode) {
        if (kode == null || kode.trim().isEmpty()) return null;
        return KATALOG.get(kode.trim().toUpperCase());
    }

    static Map<String, DiscountStrategy> semua() { return KATALOG; }
}

interface PembayaranStrategy {
    String nama();

    boolean mampuBayar(Pesanan p);

    void bayar(Pesanan p);

    void refund(Pesanan p);
}

class SaldoPayment implements PembayaranStrategy {
    public String nama() { return "Saldo JemberPay"; }

    public boolean mampuBayar(Pesanan p) {
        return p.getPelanggan().getSaldo() >= p.getTotal();
    }

    public void bayar(Pesanan p) {
        p.getPelanggan().kurangiSaldo(p.getTotal());
        System.out.println("   [Pembayaran] Saldo dipotong " + Rupiah.format(p.getTotal())
                + ". Sisa saldo: " + Rupiah.format(p.getPelanggan().getSaldo()));
    }

    public void refund(Pesanan p) {
        p.getPelanggan().tambahSaldo(p.getTotal());
        System.out.println("   [Pembayaran] Refund " + Rupiah.format(p.getTotal())
                + " ke saldo. Saldo sekarang: " + Rupiah.format(p.getPelanggan().getSaldo()));
    }
}

class TunaiPayment implements PembayaranStrategy {
    public String nama() { return "Tunai"; }

    public boolean mampuBayar(Pesanan p) { return true; }

    public void bayar(Pesanan p) {
        System.out.println("   [Pembayaran] Tunai " + Rupiah.format(p.getTotal()) + " dibayar saat terima/di kasir.");
    }

    public void refund(Pesanan p) {
        System.out.println("   [Pembayaran] Pembayaran tunai belum diserahkan, tidak ada refund.");
    }
}

class QrisPayment implements PembayaranStrategy {
    public String nama() { return "QRIS"; }

    public boolean mampuBayar(Pesanan p) { return true; }

    public void bayar(Pesanan p) {
        System.out.println("   [Pembayaran] QRIS berhasil " + Rupiah.format(p.getTotal()) + ".");
    }

    public void refund(Pesanan p) {
        System.out.println("   [Pembayaran] Dana QRIS " + Rupiah.format(p.getTotal()) + " dikembalikan ke e-wallet.");
    }
}
