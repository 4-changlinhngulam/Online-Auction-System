import java.time.LocalDateTime;
// Lớp BidTransaction (Lịch sử đấu giá)
class BidTransaction {
    private String transactionId;
    private Bidder bidder;
    private double bidAmount;
    private LocalDateTime timestamp;

    public BidTransaction(String transactionId, Bidder bidder, double bidAmount) {
        this.transactionId = transactionId;
        this.bidder = bidder;
        this.bidAmount = bidAmount;
        this.timestamp = LocalDateTime.now();
    }
}