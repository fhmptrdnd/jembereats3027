import java.util.EnumMap;
import java.util.Map;

interface OrderMediator {
    void placeOrder(Pesanan pesanan);

    void validasi(Pesanan pesanan);

    void notify(Object sender, String event, Pesanan pesanan);
}

class JemberEatsMediator implements OrderMediator {
    private final ValidationHandler validationChain;
    private final Map<Layanan, OrderProcessor> processors = new EnumMap<>(Layanan.class);

    JemberEatsMediator() {

        ValidationHandler menu = new MenuAvailabilityHandler();
        menu.setNext(new OperatingHoursHandler())
            .setNext(new PromoCodeHandler())
            .setNext(new BalanceHandler());
        this.validationChain = menu;

        processors.put(Layanan.DINE_IN, new DineInProcessor(this));
        processors.put(Layanan.DELIVERY, new DeliveryProcessor(this));
        processors.put(Layanan.PICKUP, new PickupProcessor(this));
    }

    public void placeOrder(Pesanan p) {
        p.setMediator(this);
        p.attach(new CustomerObserver(p.getPelanggan().getNama()));
        p.attach(new RestaurantObserver(p.getRestoran().getNama()));
        processors.get(p.getLayanan()).processOrder(p);
    }

    public void validasi(Pesanan p) {
        validationChain.validate(p);
    }

    public void notify(Object sender, String event, Pesanan p) {
        if ("STATE_CHANGED".equals(event)) {
            System.out.println("   [Mediator] Status " + p.getId() + " berubah -> " + p.getState().getName());
            p.notifyObservers();
            if (p.getState() instanceof SelesaiState) {
                System.out.println("   [Mediator] Pesanan " + p.getId() + " selesai. Terima kasih sudah memesan di JemberEats!");
            }
        }
    }
}
