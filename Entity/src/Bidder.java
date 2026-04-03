class Bidder extends User {
    public Bidder(String id, String username) {
        super(id, username);
    }

    @Override
    public void displayRole() {
        System.out.println("Role: Bidder - " + getUsername());
    }
}