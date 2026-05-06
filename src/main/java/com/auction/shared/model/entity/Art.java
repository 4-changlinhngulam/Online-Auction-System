package com.auction.shared.model.entity;

public class Art extends Item {
    public Art(String id, String name, double startingPrice) {
        super(id, name, startingPrice);
    }

    public Art() {
        super();
    }

    @Override
    public void printInfo() {
        System.out.println("Art Item: " + getName());
    }
}
