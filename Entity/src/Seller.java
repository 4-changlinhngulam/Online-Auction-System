class Seller extends User {
    public Seller(String id, String username) {
        super(id, username);
    }

    @Override
    public void displayRole() {
        System.out.println("Role: Seller - " + getUsername());
    }
}