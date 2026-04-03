// Lớp Abstract Entity
public abstract class Entity {
    private String id; // Encapsulation

    public Entity(String id) {
        this.id = id;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}