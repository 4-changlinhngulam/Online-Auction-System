package com.auction.shared.model.entity;

class Art extends Item {
    public Art() {}

    public Art(String id, String name, double startingPrice) {
        super(id, name, startingPrice);
    }

    @Override
    public void printInfo() {
        System.out.println("Art Item: " + getName());
    }
}