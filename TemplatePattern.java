abstract class OrderProcessor {
    protected final OrderMediator mediator;

    OrderProcessor(OrderMediator mediator) { this.mediator = mediator; }

    public final void processOrder(Pesanan p) {
        System.out.println("\n>> Memproses " + p.getId() + " [" + p.getLayanan().getLabel() + "]");
        validate(p);
        hitungTotal(p);
        prepareOrder(p);
        fulfill(p);
        konfirmasi(p);
    }

    protected void validate(Pesanan p) {
        mediator.validasi(p);
        System.out.println("   [Validasi] Semua pemeriksaan lolos.");
    }

    protected double hitungTotal(Pesanan p) {
        System.out.println("   [Harga] Subtotal : " + Rupiah.format(p.getSubtotal()));
        System.out.println("   [Harga] Ongkir   : " + Rupiah.format(p.getOngkir()) + " (" + p.getShipping().nama() + ")");
        System.out.println("   [Harga] Diskon   : -" + Rupiah.format(p.getDiskon()) + " (" + p.getDiscount().deskripsi() + ")");
        System.out.println("   [Harga] TOTAL    : " + Rupiah.format(p.getTotal()) + " via " + p.getPembayaran().nama());
        return p.getTotal();
    }

    protected void konfirmasi(Pesanan p) {
        p.bayar();
        p.setState(new DiterimaState());
        System.out.println("   [Konfirmasi] Pesanan " + p.getId() + " dikonfirmasi. Detail: " + p.getDetailLayanan());
    }

    protected abstract void prepareOrder(Pesanan p);

    protected abstract void fulfill(Pesanan p);
}

class DineInProcessor extends OrderProcessor {
    DineInProcessor(OrderMediator m) { super(m); }

    protected void prepareOrder(Pesanan p) {
        if (p.getDetailLayanan().trim().isEmpty()) {
            throw new ValidasiException("Nomor meja wajib diisi untuk makan di tempat.");
        }
        System.out.println("   [Dine-In] " + p.getDetailLayanan() + " dicadangkan di " + p.getRestoran().getNama() + ".");
    }

    protected void fulfill(Pesanan p) {
        System.out.println("   [Dine-In] Pesanan akan disajikan langsung di " + p.getDetailLayanan() + ".");
    }
}

class DeliveryProcessor extends OrderProcessor {
    private static final String[] KURIR = {"Budi", "Siti", "Rahmat"};
    private int urutan = 0;

    DeliveryProcessor(OrderMediator m) { super(m); }

    protected void prepareOrder(Pesanan p) {
        if (p.getDetailLayanan().trim().isEmpty()) {
            throw new ValidasiException("Alamat pengantaran wajib diisi.");
        }
        if (p.getJarakKm() <= 0) {
            throw new ValidasiException("Jarak pengantaran harus lebih dari 0 km.");
        }
        System.out.println("   [Delivery] Alamat: " + p.getDetailLayanan() + " (" + p.getJarakKm() + " km).");
    }

    protected void fulfill(Pesanan p) {
        String kurir = KURIR[urutan++ % KURIR.length];
        p.attach(new CourierObserver(kurir));
        System.out.println("   [Delivery] Kurir " + kurir + " ditugaskan.");
    }
}

class PickupProcessor extends OrderProcessor {
    PickupProcessor(OrderMediator m) { super(m); }

    protected void prepareOrder(Pesanan p) {
        System.out.println("   [Pickup] Pesanan dijadwalkan untuk diambil di " + p.getRestoran().getNama() + ".");
    }

    protected void fulfill(Pesanan p) {
        String kode = "AMB-" + p.getId().substring(4);
        p.setDetailLayanan("Kode ambil " + kode);
        System.out.println("   [Pickup] Tunjukkan kode " + kode + " ke kasir.");
    }
}
