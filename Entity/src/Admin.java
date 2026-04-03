class Admin extends User {
    public Admin(String id, String username) {
        super(id, username);
    }

    @Override
    public void displayRole() {
        System.out.println("Role: Admin - " + getUsername());
    }
}