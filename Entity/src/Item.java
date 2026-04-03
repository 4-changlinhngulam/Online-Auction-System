public abstract class Item extends Entity {
    private String name;
    private double startingPrice;

    public Item(String id, String name, double startingPrice) {
        super(id);
        this.name = name;
        this.startingPrice = startingPrice;
    }

    public String getName() { return name; }
    public double getStartingPrice() { return startingPrice; }

    public abstract void displayItemInfo();
}