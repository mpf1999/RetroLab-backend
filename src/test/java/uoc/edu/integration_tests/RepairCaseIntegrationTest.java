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
import uoc.edu.dto.RepairCaseRequestDTO;
import uoc.edu.model.Condition;
import uoc.edu.model.Console;
import uoc.edu.model.ConsoleModel;
import uoc.edu.model.Manufacturer;
import uoc.edu.model.RepairCase;
import uoc.edu.model.RepairStatus;
import uoc.edu.model.Status;
import uoc.edu.model.User;
import uoc.edu.repository.ComponentTestRepository;
import uoc.edu.repository.ConsoleModelRepository;
import uoc.edu.repository.ConsoleRepository;
import uoc.edu.repository.ManufacturerRepository;
import uoc.edu.repository.RepairCaseRepository;
import uoc.edu.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
class RepairCaseIntegrationTest {

    private static final String BASE_URL =
            "/api/v1/repair-cases";

    private static final String LOGIN_URL =
            "/api/v1/auth/login";

    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RepairCaseRepository repairCaseRepository;
    @Autowired
    private ComponentTestRepository componentTestRepository;
    @Autowired
    private ConsoleRepository consoleRepository;
    @Autowired
    private ConsoleModelRepository consoleModelRepository;
    @Autowired
    private ManufacturerRepository manufacturerRepository;
    @Autowired
    private UserRepository userRepository;

    private User owner;

    private Manufacturer nintendo;
    private Manufacturer sony;

    private ConsoleModel gameBoy;
    private ConsoleModel playStation;

    private Console gameBoyConsole;
    private Console playStationConsole;
    private RepairCase screenRepairCase;
    private RepairCase soundRepairCase;
    private RepairCase playStationRepairCase;

    private String jwt;

    @BeforeEach
    void setUp() throws Exception {

        login();

        owner = userRepository.findByEmailIgnoreCase("admin@retrolab.com").orElseThrow();
        nintendo = createManufacturer("Nintendo", "JP");
        sony = createManufacturer("Sony", "JP");
        nintendo =manufacturerRepository.save(nintendo);
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
                Status.IN_REPAIR,
                new BigDecimal("12000.00"),
                "Original Game Boy");

        playStationConsole = createConsole(
                owner,
                playStation,
                "PS-001",
                "PAL",
                "Gray",
                Condition.FAIR,
                Status.IN_REPAIR,
                new BigDecimal("8000.00"),
                "Original PlayStation");

        gameBoyConsole = consoleRepository.save(gameBoyConsole);
        playStationConsole = consoleRepository.save(playStationConsole);
        screenRepairCase = createRepairCase(
                gameBoyConsole,
                "No image",
                "The console turns on but no image is displayed",
                RepairStatus.IN_PROGRESS
        );
        soundRepairCase = createRepairCase(
                gameBoyConsole,
                "No sound",
                "The console has no audio output",
                RepairStatus.OPEN
        );
        playStationRepairCase = createRepairCase(
                playStationConsole,
                "Disc reader failure",
                "The console does not read game discs",
                RepairStatus.CLOSED
        );

        playStationRepairCase.setEndDate(LocalDateTime.now());

        screenRepairCase = repairCaseRepository.save(screenRepairCase);

        soundRepairCase = repairCaseRepository.save(soundRepairCase);

        playStationRepairCase = repairCaseRepository.save(playStationRepairCase);
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

    // get all repair cases int test

    @Test
    void getAllRepairCasesReturnsRepairCases() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .with(authenticated())
                        .accept(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].repairCaseId").value(screenRepairCase.getRepairCaseId()))
                .andExpect(jsonPath("$[0].consoleId").value(gameBoyConsole.getConsoleId()))
                .andExpect(jsonPath("$[0].consoleName").value("Game Boy"))
                .andExpect(jsonPath("$[0].title").value("No image"))
                .andExpect(jsonPath("$[0].description").value("The console turns on but no image is displayed"))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$[0].startDate").isNotEmpty())
                .andExpect(jsonPath("$[0].endDate").value(nullValue()))
                .andExpect(jsonPath("$[1].repairCaseId").value(soundRepairCase.getRepairCaseId()))
                .andExpect(jsonPath("$[1].consoleId").value(gameBoyConsole.getConsoleId()))
                .andExpect(jsonPath("$[1].consoleName").value("Game Boy"))
                .andExpect(jsonPath("$[1].title").value("No sound"))
                .andExpect(jsonPath("$[1].status").value("OPEN"))
                .andExpect(jsonPath("$[1].startDate").isNotEmpty())
                .andExpect(jsonPath("$[1].endDate").value(nullValue()))
                .andExpect(jsonPath("$[2].repairCaseId").value(playStationRepairCase.getRepairCaseId()))
                .andExpect(jsonPath("$[2].consoleId").value(playStationConsole.getConsoleId()))
                .andExpect(jsonPath("$[2].consoleName").value("PlayStation"))
                .andExpect(jsonPath("$[2].title").value("Disc reader failure"))
                .andExpect(jsonPath("$[2].status").value("CLOSED"))
                .andExpect(jsonPath("$[2].startDate").isNotEmpty())
                .andExpect(jsonPath("$[2].endDate").isNotEmpty());
    }

    @Test
    void getAllRepairCasesReturnsEmptyList() throws Exception {
        repairCaseRepository.deleteAll();
        mockMvc.perform(get(BASE_URL).with(authenticated()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // get repair case id test

    @Test
    void getRepairCaseByIdReturnsCorrectData() throws Exception {

        mockMvc.perform(get(BASE_URL + "/{id}", screenRepairCase.getRepairCaseId())
                        .with(authenticated())
                        .accept(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repairCaseId").value(screenRepairCase.getRepairCaseId()))
                .andExpect(jsonPath("$.consoleId").value(gameBoyConsole.getConsoleId()))
                .andExpect(jsonPath("$.consoleName").value("Game Boy"))
                .andExpect(jsonPath("$.title").value("No image"))
                .andExpect(jsonPath("$.description").value("The console turns on but no image is displayed"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.startDate").isNotEmpty())
                .andExpect(jsonPath("$.endDate").value(nullValue()));
    }

    @Test
    void getRepairCaseByIdReturnsNotFoundWhenRepairCaseDoesNotExist() throws Exception {

        mockMvc.perform(get(BASE_URL + "/{id}", 999999L).with(authenticated()).accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Repair case not found"))
                .andExpect(jsonPath("$.path").value(BASE_URL + "/999999"));
    }

    // get repair cases int test

    @Test
    void getRepairCasesByConsoleReturnsCorrectRepairCases() throws Exception {

        mockMvc.perform(get(BASE_URL + "/consoles/{consoleId}", gameBoyConsole.getConsoleId())
                        .with(authenticated())
                        .accept(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].repairCaseId").value(screenRepairCase.getRepairCaseId()))
                .andExpect(jsonPath("$[0].consoleId").value(gameBoyConsole.getConsoleId()))
                .andExpect(jsonPath("$[0].consoleName").value("Game Boy"))
                .andExpect(jsonPath("$[0].title").value("No image"))
                .andExpect(jsonPath("$[1].repairCaseId").value(soundRepairCase.getRepairCaseId()))
                .andExpect(jsonPath("$[1].consoleId").value(gameBoyConsole.getConsoleId()))
                .andExpect(jsonPath("$[1].consoleName").value("Game Boy"))
                .andExpect(jsonPath("$[1].title").value("No sound"));
    }

    @Test
    void getRepairCasesByConsoleReturnsEmptyListWhenConsoleHasNoRepairCases() throws Exception {
        Console consoleWithoutRepairCases =
                createConsole(
                        owner,
                        gameBoy,
                        "GB-002",
                        "JP",
                        "Yellow",
                        Condition.GOOD,
                        Status.AVAILABLE,
                        new BigDecimal("10000.00"),
                        "Console without repair cases"
                );

        consoleWithoutRepairCases = consoleRepository.save(consoleWithoutRepairCases);

        mockMvc.perform(get(
                BASE_URL + "/consoles/{consoleId}", consoleWithoutRepairCases.getConsoleId()).with(authenticated()).accept(
                        MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // add repair cases int test

    @Test
    void addRepairCaseSavesCorrectData() throws Exception {

        RepairCaseRequestDTO request = new RepairCaseRequestDTO(
                playStationConsole.getConsoleId(),
                "Power failure",
                "The console does not turn on",
                RepairStatus.OPEN
        );

        MvcResult result = mockMvc.perform(post(BASE_URL).with(authenticated())
                        .contentType(
                                MediaType.APPLICATION_JSON
                        ).content(
                                objectMapper.writeValueAsString(request)
                        )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.repairCaseId").isNumber())
                .andExpect(jsonPath("$.consoleId").value(playStationConsole.getConsoleId()))
                .andExpect(jsonPath("$.consoleName").value("PlayStation"))
                .andExpect(jsonPath("$.title").value("Power failure"))
                .andExpect(jsonPath("$.description").value("The console does not turn on"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.startDate").isNotEmpty())
                .andExpect(jsonPath("$.endDate").value(nullValue()))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Number repairCaseId = JsonPath.read(responseBody, "$.repairCaseId");

        RepairCase savedRepairCase = repairCaseRepository.findById(repairCaseId.longValue()).orElseThrow();

        assertAll(() -> assertNotNull(savedRepairCase.getRepairCaseId()),
                () -> assertEquals(playStationConsole.getConsoleId(), savedRepairCase.getConsole().getConsoleId()),
                () -> assertEquals("Power failure", savedRepairCase.getTitle()),
                () -> assertEquals("The console does not turn on", savedRepairCase.getDescription()),
                () -> assertEquals(RepairStatus.OPEN, savedRepairCase.getStatus()),
                () -> assertNotNull(savedRepairCase.getStartDate()),
                () -> assertNull(savedRepairCase.getEndDate())
        );
    }

    @Test
    void addRepairCaseReturnsNotFoundWhenConsoleDoesNotExist() throws Exception {
        RepairCaseRequestDTO request = new RepairCaseRequestDTO(
                999999L,
                "Invalid repair",
                "Repair case with missing console",
                RepairStatus.OPEN
        );

        mockMvc.perform(post(BASE_URL).with(authenticated())
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(objectMapper
                                        .writeValueAsString(
                                                request
                                        )
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value(BASE_URL));
        assertEquals(3, repairCaseRepository.count());
    }

    // update repair case int test

    @Test
    void updateRepairCaseSavesCorrectData() throws Exception {

        Long repairCaseId =screenRepairCase.getRepairCaseId();

        RepairCaseRequestDTO request = new RepairCaseRequestDTO(
                playStationConsole.getConsoleId(),
                "Updated disc reader failure",
                "The optical drive requires replacement",
                RepairStatus.CLOSED
        );

        mockMvc.perform(put(
                BASE_URL + "/{id}",
                        repairCaseId).with(authenticated())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                ).content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repairCaseId").value(repairCaseId))
                .andExpect(jsonPath("$.consoleId").value(playStationConsole.getConsoleId()))
                .andExpect(jsonPath("$.consoleName").value("PlayStation"))
                .andExpect(jsonPath("$.title").value("Updated disc reader failure"))
                .andExpect(jsonPath("$.description").value("The optical drive requires replacement"))
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.startDate").isNotEmpty())
                .andExpect(jsonPath("$.endDate").isNotEmpty());

        RepairCase updatedRepairCase =repairCaseRepository.findById(repairCaseId).orElseThrow();

        assertAll(
                () -> assertEquals(repairCaseId, updatedRepairCase.getRepairCaseId()),
                () -> assertEquals(playStationConsole.getConsoleId(), updatedRepairCase.getConsole().getConsoleId()),
                () -> assertEquals("Updated disc reader failure", updatedRepairCase.getTitle()),
                () -> assertEquals("The optical drive requires replacement", updatedRepairCase.getDescription()),
                () -> assertEquals(RepairStatus.CLOSED, updatedRepairCase.getStatus()),
                () -> assertNotNull(updatedRepairCase.getStartDate()),
                () -> assertNotNull(updatedRepairCase.getEndDate())
        );
    }

    @Test
    void updateRepairCaseKeepsStartDate() throws Exception {

        Long repairCaseId =screenRepairCase.getRepairCaseId();
        LocalDateTime originalStartDate = screenRepairCase.getStartDate();
        RepairCaseRequestDTO request = new RepairCaseRequestDTO(
                gameBoyConsole.getConsoleId(),
                "No image updated",
                "The screen connector has been inspected",
                RepairStatus.IN_PROGRESS
        );

        mockMvc.perform(put(
                BASE_URL + "/{id}", repairCaseId).with(authenticated())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repairCaseId").value(repairCaseId))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.endDate").value(nullValue()));

        RepairCase updatedRepairCase = repairCaseRepository.findById(repairCaseId).orElseThrow();

        assertAll(
                () -> assertEquals(originalStartDate, updatedRepairCase.getStartDate()),
                () -> assertNull(updatedRepairCase.getEndDate())
        );
    }

    @Test
    void updateRepairCaseSetsEndDateWhenStatusChangesToClosed() throws Exception {
        Long repairCaseId = soundRepairCase.getRepairCaseId();

        RepairCaseRequestDTO request = new RepairCaseRequestDTO(
                gameBoyConsole.getConsoleId(),
                "No sound",
                "Speaker replaced successfully",
                RepairStatus.CLOSED
        );

        mockMvc.perform(put(BASE_URL + "/{id}", repairCaseId)
                        .with(authenticated())
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.endDate").isNotEmpty());

        RepairCase updatedRepairCase = repairCaseRepository.findById(repairCaseId).orElseThrow();
        assertAll(
                () -> assertEquals(RepairStatus.CLOSED, updatedRepairCase.getStatus()),
                () -> assertNotNull(updatedRepairCase.getEndDate())
        );
    }

    @Test
    void updateRepairCaseReturnsBadRequestWhenRepairCaseIsClosed() throws Exception {

        Long repairCaseId = playStationRepairCase.getRepairCaseId();
        LocalDateTime originalEndDate = playStationRepairCase.getEndDate();
        RepairCaseRequestDTO request = new RepairCaseRequestDTO(
                playStationConsole.getConsoleId(),
                "Disc reader failure",
                "The problem occurred again",
                RepairStatus.IN_PROGRESS
        );

        mockMvc.perform(put(BASE_URL + "/{id}", repairCaseId)
                        .with(authenticated())
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Closed repair cases cannot be modified"))
                .andExpect(jsonPath("$.path").value(BASE_URL + "/" + repairCaseId));

        RepairCase unchangedRepairCase = repairCaseRepository.findById(repairCaseId).orElseThrow();
        assertAll(
                () -> assertEquals(RepairStatus.CLOSED, unchangedRepairCase.getStatus()),
                () -> assertEquals("Disc reader failure", unchangedRepairCase.getTitle()),
                () -> assertEquals("The console does not read game discs", unchangedRepairCase.getDescription()),
                () -> assertEquals(playStationConsole.getConsoleId(), unchangedRepairCase.getConsole().getConsoleId()),
                () -> assertEquals(originalEndDate, unchangedRepairCase.getEndDate())
        );
    }

    @Test
    void updateRepairCaseReturnsNotFoundWhenRepairCaseDoesNotExist() throws Exception {

        RepairCaseRequestDTO request = new RepairCaseRequestDTO(
                gameBoyConsole.getConsoleId(),
                "Missing repair case",
                "This repair case does not exist",
                RepairStatus.OPEN
        );

        mockMvc.perform(put(BASE_URL + "/{id}", 999999L).with(authenticated())
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Repair case not found"))
                .andExpect(jsonPath("$.path").value(BASE_URL + "/999999"));
    }

    @Test
    void updateRepairCaseReturnsNotFoundWhenConsoleDoesNotExist() throws Exception {
        Long repairCaseId = screenRepairCase.getRepairCaseId();
        RepairCaseRequestDTO request = new RepairCaseRequestDTO(
                999999L,
                "Invalid console",
                "The associated console does not exist",
                RepairStatus.OPEN
        );

        mockMvc.perform(put(BASE_URL + "/{id}", repairCaseId)
                        .with(authenticated())
                        .contentType(
                                MediaType.APPLICATION_JSON
                                )
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value(BASE_URL + "/" + repairCaseId));

        RepairCase unchangedRepairCase = repairCaseRepository.findById(repairCaseId).orElseThrow();

        assertAll(
                () -> assertEquals(gameBoyConsole.getConsoleId(), unchangedRepairCase.getConsole().getConsoleId()),
                () -> assertEquals("No image", unchangedRepairCase.getTitle()),
                () -> assertEquals(RepairStatus.IN_PROGRESS, unchangedRepairCase.getStatus())
        );
    }

    // delete repair case int test

    @Test
    void deleteRepairCaseRemovesCorrectData() throws Exception {
        Long repairCaseId = screenRepairCase.getRepairCaseId();
        mockMvc.perform(delete(BASE_URL + "/{id}", repairCaseId).with(authenticated())).andExpect(status().isOk());

        assertAll(
                () -> assertFalse(repairCaseRepository.existsById(repairCaseId)),
                () -> assertTrue(repairCaseRepository.existsById(soundRepairCase.getRepairCaseId())),
                () -> assertTrue(repairCaseRepository.existsById(playStationRepairCase.getRepairCaseId()))
        );
    }

    @Test
    void deleteRepairCaseReturnsNotFoundWhenRepairCaseDoesNotExist() throws Exception {

        mockMvc.perform(delete(BASE_URL + "/{id}", 999999L).with(authenticated()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Repair case not found"))
                .andExpect(jsonPath("$.path").value(BASE_URL + "/999999"));

        assertEquals(3, repairCaseRepository.count());
    }

    // helper

    private Manufacturer createManufacturer(String manufacturerName, String countryCode
    ) {
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

    private Console createConsole(User owner,ConsoleModel consoleModel, String serialNumber, String region, String color, Condition condition, Status status, BigDecimal estimatedValue,String notes) {
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

    private RepairCase createRepairCase(Console console, String title, String description, RepairStatus status) {
        RepairCase repairCase = new RepairCase();
        repairCase.setConsole(console);
        repairCase.setTitle(title);
        repairCase.setDescription(description);
        repairCase.setStatus(status);
        repairCase.setStartDate(LocalDateTime.now());

        return repairCase;
    }
}
