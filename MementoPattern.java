import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class Keranjang {
    private String namaRestoran;
    private List<ItemPesanan> items = new ArrayList<>();
    private String kodePromo = "";

    void tambahItem(Restoran restoran, MenuItem menu, int qty) {
        if (!items.isEmpty() && !restoran.getNama().equals(namaRestoran)) {
            throw new IllegalStateException("Keranjang berisi menu dari " + namaRestoran
                    + ". Kosongkan dulu sebelum memesan dari restoran lain.");
        }
        namaRestoran = restoran.getNama();
        for (int i = 0; i < items.size(); i++) {
            ItemPesanan lama = items.get(i);
            if (lama.getNamaMenu().equals(menu.getNama())) {
                items.set(i, new ItemPesanan(lama.getNamaMenu(), lama.getQty() + qty, lama.getHarga()));
                return;
            }
        }
        items.add(new ItemPesanan(menu.getNama(), qty, menu.getHarga()));
    }

    void hapusItem(int index) {
        items.remove(index);
        if (items.isEmpty()) namaRestoran = null;
    }

    void kosongkan() {
        items = new ArrayList<>();
        namaRestoran = null;
        kodePromo = "";
    }

    void setKodePromo(String kode) { this.kodePromo = kode == null ? "" : kode.trim().toUpperCase(); }

    String getKodePromo() { return kodePromo; }

    String getNamaRestoran() { return namaRestoran; }

    List<ItemPesanan> getItems() { return Collections.unmodifiableList(items); }

    boolean isEmpty() { return items.isEmpty(); }

    double getSubtotal() {
        double total = 0;
        for (ItemPesanan i : items) total += i.getSubtotal();
        return total;
    }

    KeranjangMemento createMemento() {
        return new KeranjangMemento(namaRestoran, items, kodePromo);
    }

    void restore(KeranjangMemento m) {
        this.namaRestoran = m.namaRestoran;
        this.items = new ArrayList<>(m.items);
        this.kodePromo = m.kodePromo;
    }
}

class KeranjangMemento implements Serializable {
    private static final long serialVersionUID = 1L;

    final String namaRestoran;
    final List<ItemPesanan> items;
    final String kodePromo;
    private final String waktu;

    KeranjangMemento(String namaRestoran, List<ItemPesanan> items, String kodePromo) {
        this.namaRestoran = namaRestoran;
        this.items = new ArrayList<>(items);
        this.kodePromo = kodePromo;
        this.waktu = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    String ringkasan() {
        return items.size() + " jenis menu dari " + namaRestoran + " (disimpan " + waktu + ")";
    }
}

class KeranjangCaretaker {
    private final Map<String, Deque<KeranjangMemento>> riwayat = new HashMap<>();

    private File file(String userId) { return new File("jembereats_keranjang_" + userId + ".dat"); }

    void catat(String userId, KeranjangMemento m) {
        Deque<KeranjangMemento> d = riwayat.get(userId);
        if (d == null) {
            d = new ArrayDeque<>();
            riwayat.put(userId, d);
        }
        d.push(m);
    }

    Optional<KeranjangMemento> undo(String userId) {
        Deque<KeranjangMemento> d = riwayat.get(userId);
        if (d == null || d.isEmpty()) return Optional.empty();
        return Optional.of(d.pop());
    }

    void simpanKeFile(String userId, KeranjangMemento m, boolean kosong) {
        File f = file(userId);
        if (kosong) {
            f.delete();
            return;
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f))) {
            out.writeObject(m);
        } catch (IOException e) {
            System.out.println("   [Peringatan] Gagal menyimpan keranjang: " + e.getMessage());
        }
    }

    Optional<KeranjangMemento> muatDariFile(String userId) {
        File f = file(userId);
        if (!f.exists()) return Optional.empty();
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            return Optional.of((KeranjangMemento) in.readObject());
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("   [Peringatan] Gagal memuat keranjang: " + e.getMessage());
            return Optional.empty();
        }
    }
}
