package uoc.edu.model;

import jakarta.persistence.*;

@Entity
public class Component {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long componentId;
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "console_model_id", nullable = false)
    private ConsoleModel consoleModel;

    public Component(Long componentId, String name, String description, ConsoleModel consoleModel) {
        this.componentId = componentId;
        this.name = name;
        this.description = description;
        this.consoleModel = consoleModel;
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

    public ConsoleModel getConsoleModel() {
        return consoleModel;
    }

    public void setConsoleModel(ConsoleModel consoleModel) {
        this.consoleModel = consoleModel;
    }
}
