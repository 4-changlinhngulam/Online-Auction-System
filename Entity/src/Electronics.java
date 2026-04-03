class Electronics extends Item {
    public Electronics(String id, String name, double startingPrice) {
        super(id, name, startingPrice);
    }

    @Override
    public void displayItemInfo() {
        System.out.println("Electronics: " + getName() + " | Price: $" + getStartingPrice());
    }
}