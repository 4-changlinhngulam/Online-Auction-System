class Vehicle extends Item {
    private long mileage;
    public Vehicle() {}

    public Vehicle(String id, String name, double startingPrice) {
        super(id, name, startingPrice);
    }

    public long getMileage() { return mileage; }
    public void setMileage(long mileage) { this.mileage = mileage; }

    @Override
    public void printInfo() {
        System.out.println("Vehicle Item: " + getName() + " - Mileage: " + mileage + " km");
    }
}