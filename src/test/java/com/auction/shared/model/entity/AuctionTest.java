package com.auction.shared.model.entity;

import com.auction.shared.model.enums.AuctionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuctionTest {

    private Auction auction;
    private Bidder bidder1;
    private Bidder bidder2;
    private Item testItem;

    @BeforeEach
    void setUp() {
        testItem = new Art();
        auction = new Auction();
        auction.setItem(testItem);
        auction.setCurrentPrice(100.0);
        auction.setStatus(AuctionStatus.RUNNING);

        bidder1 = new Bidder("bidder1", "pass", "b1@e.com");
        bidder1.setId("BIDDER_01");
        bidder2 = new Bidder("bidder2", "pass", "b2@e.com");
        bidder2.setId("BIDDER_02");
    }

    @Test
    void handleNewBid_withValidBid_shouldUpdatePriceAndWinner() {
        // When
        boolean result = auction.handleNewBid(bidder1, 120.0);

        // Then
        assertTrue(result);
        assertEquals(120.0, auction.getCurrentPrice());
        assertEquals(bidder1, auction.getCurrentWinner());
        assertEquals(1, auction.getBidHistory().size());
        assertEquals(bidder1.getId(), auction.getBidHistory().get(0).getBidderId());
    }

    @Test
    void handleNewBid_withLowerBid_shouldNotUpdate() {
        // When
        boolean result = auction.handleNewBid(bidder1, 90.0);

        // Then
        assertFalse(result);
        assertEquals(100.0, auction.getCurrentPrice());
        assertNull(auction.getCurrentWinner());
        assertTrue(auction.getBidHistory().isEmpty());
    }

    @Test
    void handleNewBid_whenAuctionNotRunning_shouldNotUpdate() {
        // Given
        auction.setStatus(AuctionStatus.FINISHED);

        // When
        boolean result = auction.handleNewBid(bidder1, 120.0);

        // Then
        assertFalse(result);
        assertEquals(100.0, auction.getCurrentPrice());
    }
}
