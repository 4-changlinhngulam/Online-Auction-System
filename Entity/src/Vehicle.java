class Vehicle extends Item {
    public Vehicle(String id, String name, double startingPrice) {
        super(id, name, startingPrice);
    }

    @Override
    public void displayItemInfo() {
        System.out.println("Vehicle: " + getName() + " | Price: $" + getStartingPrice());
    }
}