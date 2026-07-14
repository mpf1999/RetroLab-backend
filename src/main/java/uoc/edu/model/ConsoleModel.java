package uoc.edu.model;

import jakarta.persistence.*;
import java.util.List;
@Entity
public class ConsoleModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long consoleModelId;
    @Column(nullable = false)
    private String consoleModelName;
    @Column(nullable = false)
    private Integer releaseYear;

    @ManyToOne
    @JoinColumn(name = "manufacturer_id")
    private Manufacturer manufacturer;

    @OneToMany(mappedBy = "consoleModel")
    private List<Component> components;

    public ConsoleModel(Long consoleModelId, String consoleModelName, Integer releaseYear, Manufacturer manufacturer) {
        this.consoleModelId = consoleModelId;
        this.consoleModelName = consoleModelName;
        this.releaseYear = releaseYear;
        this.manufacturer = manufacturer;
    }

    public ConsoleModel() {

    }

    public Long getConsoleModelId() {
        return consoleModelId;
    }

    public void setConsoleModelId(Long consoleModelId) {
        this.consoleModelId = consoleModelId;
    }

    public String getConsoleModelName() {
        return consoleModelName;
    }

    public void setConsoleModelName(String consoleModelName) {
        this.consoleModelName = consoleModelName;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public Manufacturer getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(Manufacturer manufacturer) {
        this.manufacturer = manufacturer;
    }
}
