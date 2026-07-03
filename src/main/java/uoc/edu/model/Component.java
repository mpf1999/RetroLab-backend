package uoc.edu.model;

import jakarta.persistence.*;

@Entity
public class Component {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long componentId;
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    public Component(Long componentId, String name, String description) {
        this.componentId = componentId;
        this.name = name;
        this.description = description;
    }
    public Component() {

    }

    public Long getComponentId() {
        return componentId;
    }

    public void setComponentId(Long componentId) {
        this.componentId = componentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
