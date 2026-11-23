import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pesanan {
    private static int counter = 1000;

    private final String id;
    private final Pelanggan pelanggan;
    private final Restoran restoran;
    private final Layanan layanan;
    private final List<ItemPesanan> items;

    private OrderState state = new DraftState();
    private final List<OrderObserver> observers = new ArrayList<>();
    private OrderMediator mediator;

    private ShippingStrategy shipping = new FreeShipping();
    private DiscountStrategy discount = new NoDiscount();
    private PembayaranStrategy pembayaran = new SaldoPayment();

    private String kodePromo = "";
    private double jarakKm;
    private String detailLayanan = "";
    private boolean sudahDibayar;

    public Pesanan(Pelanggan pelanggan, Restoran restoran, Layanan layanan, List<ItemPesanan> items) {
        this.id = "ORD-" + (++counter);
        this.pelanggan = pelanggan;
        this.restoran = restoran;
        this.layanan = layanan;
        this.items = new ArrayList<>(items);
    }

    public void attach(OrderObserver o) { observers.add(o); }

    public void detach(OrderObserver o) { observers.remove(o); }

    public void notifyObservers() {
        for (OrderObserver o : new ArrayList<>(observers)) {
            o.update(this, state.getName());
        }
    }

    public void setState(OrderState baru) {
        this.state = baru;
        if (mediator != null) {
            mediator.notify(this, "STATE_CHANGED", this);
        } else {
            notifyObservers();
        }
    }

    public void next() { state.next(this); }

    public void cancel() { state.cancel(this); }

    public OrderState getState() { return state; }

    void bayar() {
        pembayaran.bayar(this);
        sudahDibayar = true;
    }

    void refundJikaSudahDibayar() {
        if (sudahDibayar) {
            pembayaran.refund(this);
            sudahDibayar = false;
        }
    }

    void pulihkan(OrderState sebelumnya) {
        if (!(state instanceof DibatalkanState)) {
            throw new IllegalStateException("Pesanan tidak sedang berstatus DIBATALKAN.");
        }
        if (!sudahDibayar) {
            if (!pembayaran.mampuBayar(this)) {
                throw new IllegalStateException("Dana tidak cukup untuk memulihkan pesanan.");
            }
            bayar();
        }
        setState(sebelumnya);
    }

    Pesanan salinUntukPesanUlang() {
        Pesanan c = new Pesanan(pelanggan, restoran, layanan, items);
        c.shipping = shipping;
        c.discount = discount;
        c.pembayaran = pembayaran;
        c.kodePromo = kodePromo;
        c.jarakKm = jarakKm;
        c.detailLayanan = detailLayanan;
        return c;
    }

    public double getSubtotal() {
        double total = 0;
        for (ItemPesanan i : items) total += i.getSubtotal();
        return total;
    }

    public double getOngkir() { return shipping.hitungOngkir(this); }

    public double getDiskon() { return discount.hitungDiskon(this); }

    public double getTotal() {
        return Math.max(0, getSubtotal() + getOngkir() - getDiskon());
    }

    public String getId() { return id; }

    public Pelanggan getPelanggan() { return pelanggan; }

    public Restoran getRestoran() { return restoran; }

    public Layanan getLayanan() { return layanan; }

    public List<ItemPesanan> getItems() { return Collections.unmodifiableList(items); }

    public ShippingStrategy getShipping() { return shipping; }

    public void setShipping(ShippingStrategy s) { this.shipping = s; }

    public DiscountStrategy getDiscount() { return discount; }

    public void setDiscount(DiscountStrategy d) { this.discount = d; }

    public PembayaranStrategy getPembayaran() { return pembayaran; }

    public void setPembayaran(PembayaranStrategy p) { this.pembayaran = p; }

    public String getKodePromo() { return kodePromo; }

    public void setKodePromo(String kode) { this.kodePromo = kode == null ? "" : kode; }

    public double getJarakKm() { return jarakKm; }

    public void setJarakKm(double jarakKm) { this.jarakKm = jarakKm; }

    public String getDetailLayanan() { return detailLayanan; }

    public void setDetailLayanan(String detail) { this.detailLayanan = detail == null ? "" : detail; }

    public void setMediator(OrderMediator m) { this.mediator = m; }
}
