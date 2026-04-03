import java.util.ArrayList;
import java.util.List;

// Lớp Auction (Phiên đấu giá)
class Auction {
    private String auctionId;
    private Item item;
    private Seller seller;
    private List<BidTransaction> transactions;

    public Auction(String auctionId, Item item, Seller seller) {
        this.auctionId = auctionId;
        this.item = item;
        this.seller = seller;
        this.transactions = new ArrayList<>();
    }

    public void addBid(BidTransaction bid) {
        this.transactions.add(bid);
    }
}