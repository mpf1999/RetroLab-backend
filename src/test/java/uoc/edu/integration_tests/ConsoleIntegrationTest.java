package uoc.edu.integration_tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import uoc.edu.dto.ConsoleRequestDTO;
import uoc.edu.model.Console;
import uoc.edu.model.ConsoleModel;
import uoc.edu.model.Manufacturer;
import uoc.edu.model.User;
import uoc.edu.model.Condition;
import uoc.edu.model.Status;
import uoc.edu.repository.ConsoleModelRepository;
import uoc.edu.repository.ConsoleRepository;
import uoc.edu.repository.ManufacturerRepository;
import uoc.edu.repository.RepairCaseRepository;
import uoc.edu.repository.UserRepository;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ConsoleIntegrationTest {

    private static final String BASE_URL = "/api/v1/consoles";
    private static final String LOGIN_URL = "/api/v1/auth/login";

    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private ConsoleRepository consoleRepository;
    @Autowired
    private ConsoleModelRepository consoleModelRepository;
    @Autowired
    private ManufacturerRepository manufacturerRepository;
    @Autowired
    private RepairCaseRepository repairCaseRepository;
    @Autowired
    private UserRepository userRepository;

    private Manufacturer nintendo;
    private Manufacturer sony;
    private ConsoleModel gameBoy;
    private ConsoleModel playStation;
    private Console gameBoyConsole;
    private Console playStationConsole;

    private User owner;
    private String jwt;

    @BeforeEach
    void setUp() throws Exception {
        login();
        owner = userRepository.findByEmailIgnoreCase("admin@retrolab.com").orElseThrow();

        nintendo = createManufacturer("Nintendo", "JP");
        sony = createManufacturer("Sony", "JP");
        nintendo = manufacturerRepository.save(nintendo);
        sony = manufacturerRepository.save(sony);
        gameBoy = createConsoleModel("Game Boy", 1989, nintendo);
        playStation = createConsoleModel("PlayStation", 1994, sony);

        gameBoy = consoleModelRepository.save(gameBoy);
        playStation = consoleModelRepository.save(playStation);

        gameBoyConsole = createConsole(
                owner,
                gameBoy,
                "GB-001",
                "JP",
                "Gray",
                Condition.GOOD,
                Status.AVAILABLE,
                new BigDecimal("12000.00"),
                "Original Game Boy");

        playStationConsole = createConsole(
                owner,
                playStation,
                "PS-001",
                "PAL",
                "Gray",
                Condition.FAIR,
                Status.AVAILABLE,
                new BigDecimal("8000.00"),
                "Original PlayStation"
        );

        gameBoyConsole = consoleRepository.save(gameBoyConsole);
        playStationConsole = consoleRepository.save(playStationConsole);
    }

    private void login() throws Exception {

        String loginRequest = """
                {
                    "email": "admin@retrolab.com",
                    "password": "ChangeMe123!"
                }
                """;

        MvcResult result = mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        jwt = JsonPath.read(responseBody, "$.token");
        assertNotNull(jwt);
    }

    private RequestPostProcessor authenticated() {

        return request -> {request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
            return request;
        };
    }

    // get all consoles integration test

    @Test
    void getAllConsolesReturnsConsoles() throws Exception {

        mockMvc.perform(
                        get(BASE_URL)
                                .with(authenticated())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].consoleId").isNumber())
                .andExpect(jsonPath("$[0].consoleModelId").value(gameBoy.getConsoleModelId()))
                .andExpect(jsonPath("$[0].consoleModelName").value("Game Boy"))
                .andExpect(jsonPath("$[0].manufacturerName").value("Nintendo"))
                .andExpect(jsonPath("$[0].serialNumber").value("GB-001"))
                .andExpect(jsonPath("$[0].region").value("JP"))
                .andExpect(jsonPath("$[0].color").value("Gray"))
                .andExpect(jsonPath("$[0].condition").value("GOOD"))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$[1].consoleId").isNumber())
                .andExpect(jsonPath("$[1].consoleModelId").value(playStation.getConsoleModelId()))
                .andExpect(jsonPath("$[1].consoleModelName").value("PlayStation"))
                .andExpect(jsonPath("$[1].manufacturerName").value("Sony"))
                .andExpect(jsonPath("$[1].serialNumber").value("PS-001"))
                .andExpect(jsonPath("$[1].region").value("PAL"));
    }

    @Test
    void getAllConsolesReturnsEmptyList() throws Exception {

        consoleRepository.deleteAll();
        mockMvc.perform(get(BASE_URL).with(authenticated()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // get console by id integration test

    @Test
    void getConsoleByIdReturnsCorrectData() throws Exception {

        mockMvc.perform(get(BASE_URL + "/{id}", gameBoyConsole.getConsoleId())
                        .with(authenticated())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consoleId").value(gameBoyConsole.getConsoleId()))
                .andExpect(jsonPath("$.consoleModelId").value(gameBoy.getConsoleModelId()))
                .andExpect(jsonPath("$.consoleModelName").value("Game Boy"))
                .andExpect(jsonPath("$.manufacturerName").value("Nintendo"))
                .andExpect(jsonPath("$.serialNumber").value("GB-001"))
                .andExpect(jsonPath("$.region").value("JP"))
                .andExpect(jsonPath("$.color").value("Gray"))
                .andExpect(jsonPath("$.condition").value("GOOD"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.notes").value("Original Game Boy"));
    }

    @Test
    void getConsoleByIdReturnsNotFoundWhenConsoleDoesNotExist()
            throws Exception {

        mockMvc.perform(
                        get(BASE_URL + "/{id}", 999999L)
                                .with(authenticated())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Console not found"))
                .andExpect(jsonPath("$.path").value(BASE_URL + "/999999"));
    }

    // add console integration tests

    @Test
    void addConsoleSavesCorrectData() throws Exception {

        ConsoleRequestDTO request = new ConsoleRequestDTO(
                owner.getId(),
                gameBoy.getConsoleModelId(),
                "GB-002",
                "JP",
                "Yellow",
                new BigDecimal("15000.00"),
                Condition.GOOD,
                Status.AVAILABLE,
                "Game Boy submitted for repair"
        );

        mockMvc.perform(post(BASE_URL).with(authenticated()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.consoleId").isNumber())
                .andExpect(jsonPath("$.consoleModelId").value(gameBoy.getConsoleModelId()))
                .andExpect(jsonPath("$.consoleModelName").value("Game Boy"))
                .andExpect(jsonPath("$.manufacturerName").value("Nintendo"))
                .andExpect(jsonPath("$.serialNumber").value("GB-002"))
                .andExpect(jsonPath("$.region").value("JP"))
                .andExpect(jsonPath("$.color").value("Yellow"))
                .andExpect(jsonPath("$.condition").value("GOOD"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.notes").value("Game Boy submitted for repair"));
        Console savedConsole =consoleRepository.findBySerialNumber("GB-002").orElseThrow();

        assertNotNull(savedConsole);
        assertAll(
                () -> assertNotNull(savedConsole.getConsoleId()),
                () -> assertEquals(owner.getId(), savedConsole.getOwner().getId()),
                () -> assertEquals(gameBoy.getConsoleModelId(), savedConsole.getConsoleModel().getConsoleModelId()),
                () -> assertEquals("GB-002", savedConsole.getSerialNumber()),
                () -> assertEquals("JP", savedConsole.getRegion()),
                () -> assertEquals("Yellow", savedConsole.getColor()),
                () -> assertEquals(Condition.GOOD, savedConsole.getCondition()),
                () -> assertEquals(Status.AVAILABLE, savedConsole.getStatus()),
                () -> assertEquals(new BigDecimal("15000.00"), savedConsole.getEstimatedValue()),
                () -> assertEquals("Game Boy submitted for repair", savedConsole.getNotes()
                )
        );
    }

    @Test
    void addConsoleReturnsConflictWhenSerialNumberAlreadyExists() throws Exception {

        ConsoleRequestDTO request = new ConsoleRequestDTO(
                        owner.getId(),
                        playStation.getConsoleModelId(),
                        "GB-001",
                        "PAL",
                        "Black",
                        new BigDecimal("9000.00"),
                        Condition.FAIR,
                        Status.AVAILABLE,
                        "Duplicate serial number"
                );

        mockMvc.perform(post(BASE_URL).with(authenticated()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.path").value(BASE_URL));
    }

    @Test
    void addConsoleReturnsNotFoundWhenConsoleModelDoesNotExist() throws Exception {

        ConsoleRequestDTO request = new ConsoleRequestDTO(
                owner.getId(),
                999999L,
                "TEST-001",
                "JP",
                "Gray",
                new BigDecimal("5000.00"),
                Condition.GOOD,
                Status.AVAILABLE,
                "Invalid console model"
        );

        mockMvc.perform(post(BASE_URL).with(authenticated()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Console model not found"))
                .andExpect(jsonPath("$.path").value(BASE_URL));
    }

    @Test
    void addConsoleReturnsNotFoundWhenOwnerDoesNotExist() throws Exception {

        ConsoleRequestDTO request = new ConsoleRequestDTO(
                999999L,
                gameBoy.getConsoleModelId(),
                "TEST-002",
                "JP",
                "Gray",
                new BigDecimal("5000.00"),
                Condition.GOOD,
                Status.AVAILABLE,
                "Invalid owner"
        );

        mockMvc.perform(post(BASE_URL).with(authenticated()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value(BASE_URL));
    }

    // update console integration test

    @Test
    void updateConsoleSavesCorrectData() throws Exception {

        Long consoleId = gameBoyConsole.getConsoleId();
        ConsoleRequestDTO request = new ConsoleRequestDTO(
                owner.getId(),
                playStation.getConsoleModelId(),
                "GB-UPDATED-001",
                "PAL",
                "Black",
                new BigDecimal("5000.00"),
                Condition.EXCELLENT,
                Status.AVAILABLE,
                "Updated console information"
        );

        mockMvc.perform(put(BASE_URL + "/{id}", consoleId).with(authenticated()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consoleId").value(consoleId))
                .andExpect(jsonPath("$.consoleModelId").value(playStation.getConsoleModelId()))
                .andExpect(jsonPath("$.consoleModelName").value("PlayStation"))
                .andExpect(jsonPath("$.manufacturerName").value("Sony"))
                .andExpect(jsonPath("$.serialNumber").value("GB-UPDATED-001"))
                .andExpect(jsonPath("$.region").value("PAL"))
                .andExpect(jsonPath("$.color").value("Black"))
                .andExpect(jsonPath("$.condition").value("EXCELLENT"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.notes").value("Updated console information"));

        Console updatedConsole = consoleRepository.findById(consoleId).orElseThrow();
        assertNotNull(updatedConsole);
        assertAll(
                () -> assertEquals(consoleId, updatedConsole.getConsoleId()),
                () -> assertEquals(owner.getId(), updatedConsole.getOwner().getId()),
                () -> assertEquals(playStation.getConsoleModelId(), updatedConsole.getConsoleModel().getConsoleModelId()),
                () -> assertEquals("GB-UPDATED-001", updatedConsole.getSerialNumber()),
                () -> assertEquals("PAL", updatedConsole.getRegion()),
                () -> assertEquals("Black", updatedConsole.getColor()),
                () -> assertEquals(Condition.EXCELLENT, updatedConsole.getCondition()),
                () -> assertEquals(Status.AVAILABLE, updatedConsole.getStatus()),
                () -> assertEquals(new BigDecimal("5000.00"), updatedConsole.getEstimatedValue()),
                () -> assertEquals("Updated console information", updatedConsole.getNotes())
        );
    }

    @Test
    void updateConsoleAllowsKeepingCurrentSerialNumber() throws Exception {

        Long consoleId = gameBoyConsole.getConsoleId();

        ConsoleRequestDTO request = new ConsoleRequestDTO(
                owner.getId(),
                gameBoy.getConsoleModelId(),
                "GB-001",
                "JP",
                "White",
                new BigDecimal("10000.00"),
                Condition.FAIR,
                Status.AVAILABLE,
                "Updated while keeping serial number"
        );

        mockMvc.perform(put(BASE_URL + "/{id}", consoleId).with(authenticated()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consoleId").value(consoleId))
                .andExpect(jsonPath("$.serialNumber").value("GB-001"))
                .andExpect(jsonPath("$.region").value("JP"))
                .andExpect(jsonPath("$.color").value("White"))
                .andExpect(jsonPath("$.condition").value("FAIR"));
        Console updatedConsole = consoleRepository.findById(consoleId).orElseThrow();
        assertAll(
                () -> assertEquals("GB-001", updatedConsole.getSerialNumber()),
                () -> assertEquals("JP", updatedConsole.getRegion()),
                () -> assertEquals("White", updatedConsole.getColor()),
                () -> assertEquals(Condition.FAIR, updatedConsole.getCondition()
                )
        );
    }

    @Test
    void updateConsoleReturnsConflictWhenSerialBelongsToAnotherConsole() throws Exception {

        ConsoleRequestDTO request = new ConsoleRequestDTO(
                owner.getId(),
                gameBoy.getConsoleModelId(),
                "PS-001",
                "JP",
                "Gray",
                new BigDecimal("12000.00"),
                Condition.GOOD,
                Status.AVAILABLE,
                "Duplicate serial"
        );

        mockMvc.perform(put(BASE_URL + "/{id}", gameBoyConsole.getConsoleId())
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.path").value(BASE_URL + "/" + gameBoyConsole.getConsoleId()));

        Console unchangedConsole = consoleRepository.findById(gameBoyConsole.getConsoleId()).orElseThrow();

        assertAll(() -> assertEquals("GB-001", unchangedConsole.getSerialNumber()),
                () -> assertEquals("JP", unchangedConsole.getRegion()),
                () -> assertEquals(gameBoy.getConsoleModelId(), unchangedConsole.getConsoleModel().getConsoleModelId())
        );
    }

    @Test
    void updateConsoleReturnsNotFoundWhenConsoleDoesNotExist() throws Exception {

        ConsoleRequestDTO request = new ConsoleRequestDTO(
                owner.getId(),
                gameBoy.getConsoleModelId(),
                "TEST-003",
                "JP",
                "Gray",
                new BigDecimal("5000.00"),
                Condition.GOOD,
                Status.AVAILABLE,
                "Missing console"
        );

        mockMvc.perform(put(BASE_URL + "/{id}", 999999L)
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Console not found"))
                .andExpect(jsonPath("$.path").value(BASE_URL + "/999999"));
    }

    // delete console integration test

    @Test
    void deleteConsoleRemovesCorrectData() throws Exception {

        Long consoleId = gameBoyConsole.getConsoleId();
        mockMvc.perform(delete(BASE_URL + "/{id}", consoleId).with(authenticated())).andExpect(status().isOk());

        assertAll(
                () -> assertFalse(consoleRepository.existsById(consoleId)),
                () -> assertTrue(consoleRepository.existsById(playStationConsole.getConsoleId()))
        );
    }

    @Test
    void deleteConsoleReturnsNotFoundWhenConsoleDoesNotExist() throws Exception {

        mockMvc.perform(delete(BASE_URL + "/{id}", 999999L).with(authenticated()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Console not found"))
                .andExpect(jsonPath("$.path").value(BASE_URL + "/999999"));
    }

    // helper

    private Manufacturer createManufacturer(String manufacturerName, String countryCode) {
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setManufacturerName(manufacturerName);
        manufacturer.setCountryCode(countryCode);

        return manufacturer;
    }

    private ConsoleModel createConsoleModel(String consoleModelName, Integer releaseYear, Manufacturer manufacturer) {
        ConsoleModel consoleModel = new ConsoleModel();

        consoleModel.setConsoleModelName(consoleModelName);
        consoleModel.setReleaseYear(releaseYear);
        consoleModel.setManufacturer(manufacturer);
        return consoleModel;
    }

    private Console createConsole(User owner, ConsoleModel consoleModel, String serialNumber, String region, String color, Condition condition, Status status, BigDecimal estimatedValue, String notes) {
        Console console = new Console();

        console.setOwner(owner);
        console.setConsoleModel(consoleModel);
        console.setSerialNumber(serialNumber);
        console.setRegion(region);
        console.setColor(color);
        console.setCondition(condition);
        console.setStatus(status);
        console.setEstimatedValue(estimatedValue);
        console.setNotes(notes);
        return console;
    }
}
