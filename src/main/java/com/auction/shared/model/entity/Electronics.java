package com.auction.shared.model.entity;

public class Electronics extends Item {
    private int warrantyMonths;
    public Electronics(String id, String name, double startingPrice) {
        super(id, name, startingPrice);
    }

    public Electronics() {
        super();
    }

    public int getWarrantyMonths() { return warrantyMonths; }
    public void setWarrantyMonths(int warrantyMonths) { this.warrantyMonths = warrantyMonths; }

    @Override
    public void printInfo() {
        System.out.println("Electronics Item: " + getName() + " - Warranty: " + warrantyMonths + " months");
    }
}
