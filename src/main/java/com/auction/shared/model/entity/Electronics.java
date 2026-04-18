package com.auction.shared.model.entity;

class Electronics extends Item {
    private int warrantyMonths;
    public Electronics() {}
    public int getWarrantyMonths() { return warrantyMonths; }
    public void setWarrantyMonths(int warrantyMonths) { this.warrantyMonths = warrantyMonths; }

    @Override
    public void printInfo() {
        System.out.println("Electronics Item: " + getName() + " - Warranty: " + warrantyMonths + " months");
    }
}
