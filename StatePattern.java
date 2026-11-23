interface OrderState {
    String getName();

    default void next(Pesanan p) {
        throw new IllegalStateException("Status '" + getName() + "' tidak memiliki tahap berikutnya.");
    }

    default void cancel(Pesanan p) {
        throw new IllegalStateException("Pesanan berstatus '" + getName() + "' tidak dapat dibatalkan.");
    }
}

class DraftState implements OrderState {
    public String getName() { return "DRAFT"; }
}

class DiterimaState implements OrderState {
    public String getName() { return "DITERIMA"; }

    public void next(Pesanan p) { p.setState(new DiprosesState()); }

    public void cancel(Pesanan p) { DibatalkanState.batalkan(p); }
}

class DiprosesState implements OrderState {
    public String getName() { return "DIPROSES"; }

    public void next(Pesanan p) { p.setState(new SiapState()); }

    public void cancel(Pesanan p) { DibatalkanState.batalkan(p); }
}

class SiapState implements OrderState {
    public String getName() { return "SIAP"; }

    public void next(Pesanan p) {
        if (p.getLayanan() == Layanan.DELIVERY) {
            p.setState(new DiantarState());
        } else {
            p.setState(new SelesaiState());
        }
    }
}

class DiantarState implements OrderState {
    public String getName() { return "DIANTAR"; }

    public void next(Pesanan p) { p.setState(new SelesaiState()); }
}

class SelesaiState implements OrderState {
    public String getName() { return "SELESAI"; }

    public void next(Pesanan p) {
        throw new IllegalStateException("Pesanan sudah selesai.");
    }
}

class DibatalkanState implements OrderState {
    public String getName() { return "DIBATALKAN"; }

    public void next(Pesanan p) {
        throw new IllegalStateException("Pesanan sudah dibatalkan.");
    }

    static void batalkan(Pesanan p) {
        p.setState(new DibatalkanState());
        p.refundJikaSudahDibayar();
    }
}
