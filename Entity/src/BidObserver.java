public interface BidObserver {
    void update(Item item, double newPrice, String lastBidderId);
}