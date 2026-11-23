abstract class ValidationHandler {
    private ValidationHandler next;

    ValidationHandler setNext(ValidationHandler next) {
        this.next = next;
        return next;
    }

    final void validate(Pesanan p) {
        doValidate(p);
        if (next != null) next.validate(p);
    }

    protected abstract void doValidate(Pesanan p);
}

class MenuAvailabilityHandler extends ValidationHandler {
    protected void doValidate(Pesanan p) {
        if (p.getItems().isEmpty()) {
            throw new ValidasiException("Pesanan kosong.");
        }
        for (ItemPesanan item : p.getItems()) {
            MenuItem m = p.getRestoran().cariMenu(item.getNamaMenu());
            if (m == null || !m.isTersedia()) {
                throw new ValidasiException("Menu '" + item.getNamaMenu()
                        + "' sedang tidak tersedia di " + p.getRestoran().getNama() + ".");
            }
        }
    }
}

class OperatingHoursHandler extends ValidationHandler {
    protected void doValidate(Pesanan p) {
        if (!p.getRestoran().sedangBuka(JamSimulasi.get())) {
            throw new ValidasiException(p.getRestoran().getNama() + " sedang TUTUP. Jam operasional "
                    + p.getRestoran().jamOperasional() + " (jam sekarang " + JamSimulasi.get() + ").");
        }
    }
}

class PromoCodeHandler extends ValidationHandler {
    protected void doValidate(Pesanan p) {
        String kode = p.getKodePromo();
        if (kode.trim().isEmpty()) return;

        DiscountStrategy promo = PromoCatalog.cari(kode);
        if (promo == null) {
            throw new ValidasiException("Kode promo '" + kode + "' tidak dikenal.");
        }
        if (!promo.berlaku(p)) {
            throw new ValidasiException("Syarat promo '" + kode + "' belum terpenuhi: " + promo.deskripsi() + ".");
        }
    }
}

class BalanceHandler extends ValidationHandler {
    protected void doValidate(Pesanan p) {
        if (!p.getPembayaran().mampuBayar(p)) {
            throw new ValidasiException("Dana tidak mencukupi untuk metode '" + p.getPembayaran().nama()
                    + "'. Total " + Rupiah.format(p.getTotal())
                    + ", saldo " + Rupiah.format(p.getPelanggan().getSaldo()) + ".");
        }
    }
}
