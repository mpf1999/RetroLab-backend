package uoc.edu.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class Console {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long consoleId;

    private String serialNumber;
    private String region;
    private String color;
    private String status;
    private BigDecimal estimatedValue;
    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne
    @JoinColumn(name = "console_model_id")
    private ConsoleModel consoleModel;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    private String condition;

    public Console() {
    }

    public Console(Long consoleId, String serialNumber, String region, String color, String status, BigDecimal estimatedValue, String notes, ConsoleModel consoleModel, User owner, String condition) {
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

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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
