import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

interface OrderCommand {
    void execute();

    void undo();

    String describe();
}

class CancelOrderCommand implements OrderCommand {
    private final Pesanan pesanan;
    private OrderState stateSebelumnya;

    CancelOrderCommand(Pesanan pesanan) { this.pesanan = pesanan; }

    public void execute() {
        OrderState sebelum = pesanan.getState();
        pesanan.cancel();
        stateSebelumnya = sebelum;
    }

    public void undo() { pesanan.pulihkan(stateSebelumnya); }

    public String describe() { return "Batalkan pesanan " + pesanan.getId(); }
}

class ReorderCommand implements OrderCommand {
    private final OrderMediator mediator;
    private final Pesanan asal;
    private Pesanan baru;

    ReorderCommand(OrderMediator mediator, Pesanan asal) {
        this.mediator = mediator;
        this.asal = asal;
    }

    public void execute() {
        Pesanan salinan = asal.salinUntukPesanUlang();
        mediator.placeOrder(salinan);
        baru = salinan;
    }

    public void undo() {
        if (baru == null) throw new IllegalStateException("Belum ada pesanan baru.");
        baru.cancel();
    }

    public String describe() {
        return "Pesan ulang dari " + asal.getId() + (baru != null ? " -> " + baru.getId() : "");
    }

    Pesanan getPesananBaru() { return baru; }
}

class CommandInvoker {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Deque<OrderCommand> history = new ArrayDeque<>();
    private final List<String> log = new ArrayList<>();

    void run(OrderCommand cmd) {
        cmd.execute();
        history.push(cmd);
        log.add(LocalTime.now().format(FMT) + "  EXEC  " + cmd.describe());
    }

    void undoLast() {
        if (history.isEmpty()) {
            throw new IllegalStateException("Tidak ada aksi yang bisa di-undo.");
        }
        OrderCommand cmd = history.peek();
        cmd.undo();
        history.pop();
        log.add(LocalTime.now().format(FMT) + "  UNDO  " + cmd.describe());
    }

    List<String> getLog() { return log; }

    boolean kosong() { return history.isEmpty(); }
}
