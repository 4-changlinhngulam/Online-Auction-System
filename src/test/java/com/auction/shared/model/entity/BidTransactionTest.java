package com.auction.shared.model.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BidTransactionTest {

    @Test
    void testBidTransactionFields() {
        BidTransaction t1 = new BidTransaction();
        t1.setId("TXN_001");
        t1.setBidderId("BIDDER_01");
        t1.setAmount(100.0);

        assertEquals("TXN_001", t1.getId(), "ID phải khớp với giá trị được set");
        assertEquals("BIDDER_01", t1.getBidderId());
        assertEquals(100.0, t1.getAmount());
        assertNotNull(t1.getTimestamp(), "Timestamp phải tự động được sinh ra");
    }
}
