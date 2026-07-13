package uoc.edu.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class Console {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long consoleId;

    @Column(nullable = false, unique = true)
    private String serialNumber;

    private String region;
    private String color;

    @Enumerated(EnumType.STRING)
    private Status status;

    private BigDecimal estimatedValue;
    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne
    @JoinColumn(name = "console_model_id")
    private ConsoleModel consoleModel;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @Enumerated(EnumType.STRING)
    private Condition condition;

    public Console() {
    }

    public Console(Long consoleId, String serialNumber, String region, String color, Status status, BigDecimal estimatedValue, String notes, ConsoleModel consoleModel, User owner, Condition condition) {
        this.consoleId = consoleId;
        this.serialNumber = serialNumber;
        this.region = region;
        this.color = color;
        this.status = status;
        this.estimatedValue = estimatedValue;
        this.notes = notes;
        this.consoleModel = consoleModel;
        this.owner = owner;
        this.condition = condition;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public Long getConsoleId() {
        return consoleId;
    }

    public void setConsoleId(Long consoleId) {
        this.consoleId = consoleId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public BigDecimal getEstimatedValue() {
        return estimatedValue;
    }

    public void setEstimatedValue(BigDecimal estimatedValue) {
        this.estimatedValue = estimatedValue;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public ConsoleModel getConsoleModel() {
        return consoleModel;
    }

    public void setConsoleModel(ConsoleModel consoleModel) {
        this.consoleModel = consoleModel;
    }
}
