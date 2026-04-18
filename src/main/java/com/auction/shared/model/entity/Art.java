package com.auction.shared.model.entity;

class Art extends Item {
    public Art() {}

    @Override
    public void printInfo() {
        System.out.println("Art Item: " + getName());
    }
}
