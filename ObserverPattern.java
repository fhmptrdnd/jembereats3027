interface OrderObserver {
    void update(Pesanan pesanan, String status);
}

class CustomerObserver implements OrderObserver {
    private final String namaPelanggan;

    CustomerObserver(String namaPelanggan) { this.namaPelanggan = namaPelanggan; }

    public void update(Pesanan p, String status) {
        System.out.println("   [Notif Pelanggan " + namaPelanggan + "] Pesanan " + p.getId()
                + " sekarang: " + status);
    }
}

class RestaurantObserver implements OrderObserver {
    private final String namaRestoran;

    RestaurantObserver(String namaRestoran) { this.namaRestoran = namaRestoran; }

    public void update(Pesanan p, String status) {
        System.out.println("   [Notif Restoran " + namaRestoran + "] Pesanan " + p.getId()
                + " (" + p.getLayanan().getLabel() + ") -> " + status);
    }
}

class CourierObserver implements OrderObserver {
    private final String namaKurir;

    CourierObserver(String namaKurir) { this.namaKurir = namaKurir; }

    public void update(Pesanan p, String status) {
        System.out.println("   [Notif Kurir " + namaKurir + "] Pesanan " + p.getId()
                + " -> " + status);
    }
}
