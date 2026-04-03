class Art extends Item {
    public Art(String id, String name, double startingPrice) {
        super(id, name, startingPrice);
    }

    @Override
    public void displayItemInfo() {
        System.out.println("Art Piece: " + getName() + " | Price: $" + getStartingPrice());
    }
}