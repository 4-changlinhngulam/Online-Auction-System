package com.auction.shared.model.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BidTransactionTest {

    @Test
    void testGenerateId() {
        String id1 = BidTransaction.generateId();
        String id2 = BidTransaction.generateId();
        assertNotNull(id1);
        assertNotNull(id2);
        assertNotEquals(id1, id2);
    }
}
