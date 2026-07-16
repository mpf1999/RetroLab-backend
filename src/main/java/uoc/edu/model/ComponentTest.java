package uoc.edu.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class ComponentTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long componentTestId;

    private BigDecimal measuredVoltage;
    private BigDecimal measuredCurrent;
    private BigDecimal measuredResistance;
    private BigDecimal temperature;
    @Column(nullable = false)
    private TestResult result;
    private String notes;
    @Column(nullable = false)
    private LocalDateTime testDate;

    @PrePersist
    public void prePersist() {
        this.testDate = LocalDateTime.now();
    }

    @ManyToOne
    @JoinColumn(name = "repair_case_id")
    private RepairCase repairCase;

    @ManyToOne
    @JoinColumn(name = "component_id")
    private Component component;

    public ComponentTest(Long componentTestId, BigDecimal measuredVoltage, BigDecimal measuredCurrent, BigDecimal measuredResistance, BigDecimal temperature, RepairCase repairCase, Component component) {
        this.componentTestId = componentTestId;
        this.measuredVoltage = measuredVoltage;
        this.measuredCurrent = measuredCurrent;
        this.measuredResistance = measuredResistance;
        this.temperature = temperature;
        this.repairCase = repairCase;
        this.component = component;
        this.result = TestResult.NOT_TESTED;
        this.notes = "";
        this.testDate = LocalDateTime.now();
    }
    public ComponentTest() {

    }

    public Long getComponentTestId() {
        return componentTestId;
    }

    public void setComponentTestId(Long componentTestId) {
        this.componentTestId = componentTestId;
    }

    public BigDecimal getMeasuredVoltage() {
        return measuredVoltage;
    }

    public void setMeasuredVoltage(BigDecimal measuredVoltage) {
        this.measuredVoltage = measuredVoltage;
    }

    public BigDecimal getMeasuredCurrent() {
        return measuredCurrent;
    }

    public void setMeasuredCurrent(BigDecimal measuredCurrent) {
        this.measuredCurrent = measuredCurrent;
    }

    public BigDecimal getMeasuredResistance() {
        return measuredResistance;
    }

    public void setMeasuredResistance(BigDecimal measuredResistance) {
        this.measuredResistance = measuredResistance;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
    }

    public RepairCase getRepairCase() {
        return repairCase;
    }

    public void setRepairCase(RepairCase repairCase) {
        this.repairCase = repairCase;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public TestResult getResult() {
        return result;
    }

    public void setResult(TestResult result) {
        this.result = result;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getTestDate() {
        return testDate;
    }

    public void setTestDate(LocalDateTime testDate) {
        this.testDate = testDate;
    }
}
