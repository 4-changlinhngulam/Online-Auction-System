package com.auction.shared.model.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BidTransactionTest {

    @Test
    void testBidTransactionIdGeneration() {
        BidTransaction t1 = new BidTransaction();
        BidTransaction t2 = new BidTransaction();

        assertNotNull(t1.getId(), "ID phải tự động được sinh ra");
        assertNotEquals(t1.getId(), t2.getId(), "Hai đối tượng khác nhau phải có ID khác nhau");
    }
}
