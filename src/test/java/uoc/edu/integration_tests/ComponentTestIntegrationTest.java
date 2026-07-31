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
import uoc.edu.dto.ComponentTestRequestDTO;
import uoc.edu.model.Component;
import uoc.edu.model.ComponentTest;
import uoc.edu.model.Console;
import uoc.edu.model.ConsoleModel;
import uoc.edu.model.Manufacturer;
import uoc.edu.model.RepairCase;
import uoc.edu.model.User;
import uoc.edu.model.Condition;
import uoc.edu.model.RepairStatus;
import uoc.edu.model.Status;
import uoc.edu.model.TestResult;
import uoc.edu.repository.ComponentRepository;
import uoc.edu.repository.ComponentTestRepository;
import uoc.edu.repository.ConsoleModelRepository;
import uoc.edu.repository.ConsoleRepository;
import uoc.edu.repository.ManufacturerRepository;
import uoc.edu.repository.RepairCaseRepository;
import uoc.edu.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
class ComponentTestIntegrationTest {

    private static final String BASE_URL =
            "/api/v1/component-tests";

    private static final String LOGIN_URL =
            "/api/v1/auth/login";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper  objectMapper = new ObjectMapper();

    @Autowired
    private ComponentTestRepository componentTestRepository;

    @Autowired
    private RepairCaseRepository repairCaseRepository;

    @Autowired
    private ComponentRepository componentRepository;

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

    private RepairCase repairCase;

    private Component screen;
    private Component speaker;
    private Component playStationPowerSupply;

    private ComponentTest screenTest;
    private ComponentTest speakerTest;

    private String jwt;

    @BeforeEach
    void setUp() throws Exception {

        componentTestRepository.deleteAll();
        repairCaseRepository.deleteAll();
        componentRepository.deleteAll();
        consoleRepository.deleteAll();
        consoleModelRepository.deleteAll();
        manufacturerRepository.deleteAll();

        login();

        owner = userRepository
                .findByEmailIgnoreCase("admin@retrolab.com")
                .orElseThrow();

        nintendo = createManufacturer(
                "Nintendo",
                "JP"
        );

        sony = createManufacturer(
                "Sony",
                "JP"
        );

        nintendo =
                manufacturerRepository.save(nintendo);

        sony =
                manufacturerRepository.save(sony);

        gameBoy = createConsoleModel(
                "Game Boy",
                1989,
                nintendo
        );

        playStation = createConsoleModel(
                "PlayStation",
                1994,
                sony
        );

        gameBoy =
                consoleModelRepository.save(gameBoy);

        playStation =
                consoleModelRepository.save(playStation);

        gameBoyConsole = createConsole(
                owner,
                gameBoy,
                "GB-001",
                "JP",
                "Gray",
                Condition.GOOD,
                Status.IN_REPAIR,
                new BigDecimal("12000.00"),
                "Game Boy under repair"
        );

        gameBoyConsole =
                consoleRepository.save(gameBoyConsole);

        repairCase = createRepairCase(
                gameBoyConsole,
                "No image",
                "The console turns on but no image is displayed",
                RepairStatus.IN_PROGRESS
        );

        repairCase =
                repairCaseRepository.save(repairCase);

        screen = createComponent(
                gameBoy,
                "Screen",
                "Original Game Boy LCD screen"
        );

        speaker = createComponent(
                gameBoy,
                "Speaker",
                "Original Game Boy speaker"
        );

        playStationPowerSupply = createComponent(
                playStation,
                "Power supply",
                "Internal PlayStation power supply"
        );

        screen = componentRepository.save(screen);
        speaker = componentRepository.save(speaker);
        playStationPowerSupply =
                componentRepository.save(playStationPowerSupply);

        screenTest = createComponentTest(
                repairCase,
                screen,
                new BigDecimal("4.80"),
                new BigDecimal("0.25"),
                new BigDecimal("150.00"),
                new BigDecimal("31.50"),
                TestResult.WARNING,
                "Voltage slightly below expected value"
        );

        speakerTest = createComponentTest(
                repairCase,
                speaker,
                new BigDecimal("5.00"),
                new BigDecimal("0.15"),
                new BigDecimal("8.00"),
                new BigDecimal("28.00"),
                TestResult.PASS,
                "Speaker works correctly"
        );

        screenTest =
                componentTestRepository.save(screenTest);

        speakerTest =
                componentTestRepository.save(speakerTest);
    }

    private void login() throws Exception {

        String loginRequest = """
                {
                    "email": "admin@retrolab.com",
                    "password": "ChangeMe123!"
                }
                """;

        MvcResult result = mockMvc.perform(
                        post(LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginRequest)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andReturn();

        String responseBody =
                result.getResponse().getContentAsString();

        jwt = JsonPath.read(
                responseBody,
                "$.token"
        );

        assertNotNull(jwt);
    }

    private RequestPostProcessor authenticated() {

        return request -> {

            request.addHeader(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer " + jwt
            );

            return request;
        };
    }

    /*
     GET all component tests
     */

    @Test
    void getAllComponentTestsReturnsComponentTests()
            throws Exception {

        mockMvc.perform(
                        get(BASE_URL)
                                .with(authenticated())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))

                .andExpect(jsonPath("$[0].componentTestId")
                        .value(screenTest.getComponentTestId()))
                .andExpect(jsonPath("$[0].repairCaseId")
                        .value(repairCase.getRepairCaseId()))
                .andExpect(jsonPath("$[0].componentId")
                        .value(screen.getComponentId()))
                .andExpect(jsonPath("$[0].componentName")
                        .value("Screen"))
                .andExpect(jsonPath("$[0].measuredVoltage")
                        .value(4.80))
                .andExpect(jsonPath("$[0].measuredCurrent")
                        .value(0.25))
                .andExpect(jsonPath("$[0].measuredResistance")
                        .value(150.00))
                .andExpect(jsonPath("$[0].temperature")
                        .value(31.50))
                .andExpect(jsonPath("$[0].result")
                        .value("WARNING"))
                .andExpect(jsonPath("$[0].testDate")
                        .isNotEmpty())
                .andExpect(jsonPath("$[0].notes")
                        .value(
                                "Voltage slightly below expected value"
                        ))

                .andExpect(jsonPath("$[1].componentTestId")
                        .value(speakerTest.getComponentTestId()))
                .andExpect(jsonPath("$[1].componentId")
                        .value(speaker.getComponentId()))
                .andExpect(jsonPath("$[1].componentName")
                        .value("Speaker"))
                .andExpect(jsonPath("$[1].result")
                        .value("PASS"));
    }

    @Test
    void getAllComponentTestsReturnsEmptyList()
            throws Exception {

        componentTestRepository.deleteAll();

        mockMvc.perform(
                        get(BASE_URL)
                                .with(authenticated())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /*
     GET component test by ID
     */

    @Test
    void getComponentTestByIdReturnsCorrectData()
            throws Exception {

        mockMvc.perform(
                        get(
                                BASE_URL + "/{id}",
                                screenTest.getComponentTestId()
                        )
                                .with(authenticated())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.componentTestId")
                        .value(screenTest.getComponentTestId()))
                .andExpect(jsonPath("$.repairCaseId")
                        .value(repairCase.getRepairCaseId()))
                .andExpect(jsonPath("$.componentId")
                        .value(screen.getComponentId()))
                .andExpect(jsonPath("$.componentName")
                        .value("Screen"))
                .andExpect(jsonPath("$.measuredVoltage")
                        .value(4.80))
                .andExpect(jsonPath("$.measuredCurrent")
                        .value(0.25))
                .andExpect(jsonPath("$.measuredResistance")
                        .value(150.00))
                .andExpect(jsonPath("$.temperature")
                        .value(31.50))
                .andExpect(jsonPath("$.result")
                        .value("WARNING"))
                .andExpect(jsonPath("$.testDate")
                        .isNotEmpty())
                .andExpect(jsonPath("$.notes")
                        .value(
                                "Voltage slightly below expected value"
                        ));
    }

    @Test
    void getComponentTestByIdReturnsNotFoundWhenItDoesNotExist()
            throws Exception {

        mockMvc.perform(
                        get(BASE_URL + "/{id}", 999999L)
                                .with(authenticated())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Component test not found"))
                .andExpect(jsonPath("$.path")
                        .value(BASE_URL + "/999999"));
    }

    /*
     POST component test
     */

    @Test
    void addComponentTestSavesCorrectData()
            throws Exception {

        ComponentTestRequestDTO request =
                new ComponentTestRequestDTO(
                        repairCase.getRepairCaseId(),
                        screen.getComponentId(),
                        new BigDecimal("5.00"),
                        new BigDecimal("0.30"),
                        new BigDecimal("175.00"),
                        new BigDecimal("30.50"),
                        TestResult.PASS,
                        "Screen values are correct"
                );

        MvcResult result = mockMvc.perform(
                        post(BASE_URL)
                                .with(authenticated())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.componentTestId")
                        .isNumber())
                .andExpect(jsonPath("$.repairCaseId")
                        .value(repairCase.getRepairCaseId()))
                .andExpect(jsonPath("$.componentId")
                        .value(screen.getComponentId()))
                .andExpect(jsonPath("$.componentName")
                        .value("Screen"))
                .andExpect(jsonPath("$.measuredVoltage")
                        .value(5.00))
                .andExpect(jsonPath("$.measuredCurrent")
                        .value(0.30))
                .andExpect(jsonPath("$.measuredResistance")
                        .value(175.00))
                .andExpect(jsonPath("$.temperature")
                        .value(30.50))
                .andExpect(jsonPath("$.result")
                        .value("PASS"))
                .andExpect(jsonPath("$.testDate")
                        .isNotEmpty())
                .andExpect(jsonPath("$.notes")
                        .value("Screen values are correct"))
                .andReturn();

        String responseBody =
                result.getResponse().getContentAsString();

        Number componentTestId =
                JsonPath.read(
                        responseBody,
                        "$.componentTestId"
                );

        ComponentTest savedComponentTest =
                componentTestRepository.findById(
                        componentTestId.longValue()
                ).orElseThrow();

        assertAll(
                () -> assertNotNull(
                        savedComponentTest.getComponentTestId()
                ),
                () -> assertEquals(
                        repairCase.getRepairCaseId(),
                        savedComponentTest
                                .getRepairCase()
                                .getRepairCaseId()
                ),
                () -> assertEquals(
                        screen.getComponentId(),
                        savedComponentTest
                                .getComponent()
                                .getComponentId()
                ),
                () -> assertEquals(
                        new BigDecimal("5.00"),
                        savedComponentTest.getMeasuredVoltage()
                ),
                () -> assertEquals(
                        new BigDecimal("0.30"),
                        savedComponentTest.getMeasuredCurrent()
                ),
                () -> assertEquals(
                        new BigDecimal("175.00"),
                        savedComponentTest.getMeasuredResistance()
                ),
                () -> assertEquals(
                        new BigDecimal("30.50"),
                        savedComponentTest.getTemperature()
                ),
                () -> assertEquals(
                        TestResult.PASS,
                        savedComponentTest.getResult()
                ),
                () -> assertNotNull(
                        savedComponentTest.getTestDate()
                ),
                () -> assertEquals(
                        "Screen values are correct",
                        savedComponentTest.getNotes()
                )
        );
    }

    @Test
    void addComponentTestReturnsNotFoundWhenRepairCaseDoesNotExist()
            throws Exception {

        ComponentTestRequestDTO request =
                new ComponentTestRequestDTO(
                        999999L,
                        screen.getComponentId(),
                        new BigDecimal("5.00"),
                        new BigDecimal("0.30"),
                        new BigDecimal("175.00"),
                        new BigDecimal("30.50"),
                        TestResult.PASS,
                        "Invalid repair case"
                );

        mockMvc.perform(
                        post(BASE_URL)
                                .with(authenticated())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"))
                .andExpect(jsonPath("$.path")
                        .value(BASE_URL));
    }

    @Test
    void addComponentTestReturnsNotFoundWhenComponentDoesNotExist()
            throws Exception {

        ComponentTestRequestDTO request =
                new ComponentTestRequestDTO(
                        repairCase.getRepairCaseId(),
                        999999L,
                        new BigDecimal("5.00"),
                        new BigDecimal("0.30"),
                        new BigDecimal("175.00"),
                        new BigDecimal("30.50"),
                        TestResult.PASS,
                        "Invalid component"
                );

        mockMvc.perform(
                        post(BASE_URL)
                                .with(authenticated())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"))
                .andExpect(jsonPath("$.path")
                        .value(BASE_URL));
    }

    @Test
    void addComponentTestReturnsBadRequestWhenComponentBelongsToDifferentModel()
            throws Exception {

        ComponentTestRequestDTO request =
                new ComponentTestRequestDTO(
                        repairCase.getRepairCaseId(),
                        playStationPowerSupply.getComponentId(),
                        new BigDecimal("5.00"),
                        new BigDecimal("0.30"),
                        new BigDecimal("175.00"),
                        new BigDecimal("30.50"),
                        TestResult.PASS,
                        "Component belongs to another model"
                );

        mockMvc.perform(
                        post(BASE_URL)
                                .with(authenticated())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.error")
                        .value("Bad Request"))
                .andExpect(jsonPath("$.path")
                        .value(BASE_URL));
    }

    /*
     PUT component test
     */

    @Test
    void updateComponentTestSavesCorrectData()
            throws Exception {

        Long componentTestId =
                screenTest.getComponentTestId();

        ComponentTestRequestDTO request =
                new ComponentTestRequestDTO(
                        repairCase.getRepairCaseId(),
                        speaker.getComponentId(),
                        new BigDecimal("5.10"),
                        new BigDecimal("0.20"),
                        new BigDecimal("8.50"),
                        new BigDecimal("29.00"),
                        TestResult.PASS,
                        "Updated speaker test"
                );

        mockMvc.perform(
                        put(
                                BASE_URL + "/{id}",
                                componentTestId
                        )
                                .with(authenticated())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.componentTestId")
                        .value(componentTestId))
                .andExpect(jsonPath("$.repairCaseId")
                        .value(repairCase.getRepairCaseId()))
                .andExpect(jsonPath("$.componentId")
                        .value(speaker.getComponentId()))
                .andExpect(jsonPath("$.componentName")
                        .value("Speaker"))
                .andExpect(jsonPath("$.measuredVoltage")
                        .value(5.10))
                .andExpect(jsonPath("$.measuredCurrent")
                        .value(0.20))
                .andExpect(jsonPath("$.measuredResistance")
                        .value(8.50))
                .andExpect(jsonPath("$.temperature")
                        .value(29.00))
                .andExpect(jsonPath("$.result")
                        .value("PASS"))
                .andExpect(jsonPath("$.notes")
                        .value("Updated speaker test"));

        ComponentTest updatedComponentTest =
                componentTestRepository
                        .findById(componentTestId)
                        .orElseThrow();

        assertAll(
                () -> assertEquals(
                        componentTestId,
                        updatedComponentTest.getComponentTestId()
                ),
                () -> assertEquals(
                        repairCase.getRepairCaseId(),
                        updatedComponentTest
                                .getRepairCase()
                                .getRepairCaseId()
                ),
                () -> assertEquals(
                        speaker.getComponentId(),
                        updatedComponentTest
                                .getComponent()
                                .getComponentId()
                ),
                () -> assertEquals(
                        new BigDecimal("5.10"),
                        updatedComponentTest.getMeasuredVoltage()
                ),
                () -> assertEquals(
                        new BigDecimal("0.20"),
                        updatedComponentTest.getMeasuredCurrent()
                ),
                () -> assertEquals(
                        new BigDecimal("8.50"),
                        updatedComponentTest.getMeasuredResistance()
                ),
                () -> assertEquals(
                        new BigDecimal("29.00"),
                        updatedComponentTest.getTemperature()
                ),
                () -> assertEquals(
                        TestResult.PASS,
                        updatedComponentTest.getResult()
                ),
                () -> assertEquals(
                        "Updated speaker test",
                        updatedComponentTest.getNotes()
                )
        );
    }

    @Test
    void updateComponentTestReturnsNotFoundWhenTestDoesNotExist()
            throws Exception {

        ComponentTestRequestDTO request =
                new ComponentTestRequestDTO(
                        repairCase.getRepairCaseId(),
                        screen.getComponentId(),
                        new BigDecimal("5.00"),
                        new BigDecimal("0.30"),
                        new BigDecimal("175.00"),
                        new BigDecimal("30.50"),
                        TestResult.PASS,
                        "Missing component test"
                );

        mockMvc.perform(
                        put(BASE_URL + "/{id}", 999999L)
                                .with(authenticated())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Component test not found"))
                .andExpect(jsonPath("$.path")
                        .value(BASE_URL + "/999999"));
    }

    @Test
    void updateComponentTestReturnsNotFoundWhenRepairCaseDoesNotExist()
            throws Exception {

        ComponentTestRequestDTO request =
                new ComponentTestRequestDTO(
                        999999L,
                        screen.getComponentId(),
                        new BigDecimal("5.00"),
                        new BigDecimal("0.30"),
                        new BigDecimal("175.00"),
                        new BigDecimal("30.50"),
                        TestResult.PASS,
                        "Invalid repair case"
                );

        mockMvc.perform(
                        put(
                                BASE_URL + "/{id}",
                                screenTest.getComponentTestId()
                        )
                                .with(authenticated())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"));
    }

    @Test
    void updateComponentTestReturnsNotFoundWhenComponentDoesNotExist()
            throws Exception {

        ComponentTestRequestDTO request =
                new ComponentTestRequestDTO(
                        repairCase.getRepairCaseId(),
                        999999L,
                        new BigDecimal("5.00"),
                        new BigDecimal("0.30"),
                        new BigDecimal("175.00"),
                        new BigDecimal("30.50"),
                        TestResult.PASS,
                        "Invalid component"
                );

        mockMvc.perform(
                        put(
                                BASE_URL + "/{id}",
                                screenTest.getComponentTestId()
                        )
                                .with(authenticated())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"));
    }

    /*
     DELETE component test
     */

    @Test
    void deleteComponentTestRemovesCorrectData()
            throws Exception {

        Long componentTestId =
                screenTest.getComponentTestId();

        mockMvc.perform(
                        delete(
                                BASE_URL + "/{id}",
                                componentTestId
                        )
                                .with(authenticated())
                )
                .andExpect(status().isOk());

        assertAll(
                () -> assertFalse(
                        componentTestRepository
                                .existsById(componentTestId)
                ),
                () -> assertTrue(
                        componentTestRepository.existsById(
                                speakerTest.getComponentTestId()
                        )
                )
        );
    }

    @Test
    void deleteComponentTestReturnsNotFoundWhenItDoesNotExist()
            throws Exception {

        mockMvc.perform(
                        delete(BASE_URL + "/{id}", 999999L)
                                .with(authenticated())
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Component test not found"))
                .andExpect(jsonPath("$.path")
                        .value(BASE_URL + "/999999"));
    }

    /*
     Helper methods
     */

    private Manufacturer createManufacturer(
            String manufacturerName,
            String countryCode
    ) {
        Manufacturer manufacturer = new Manufacturer();

        manufacturer.setManufacturerName(manufacturerName);
        manufacturer.setCountryCode(countryCode);

        return manufacturer;
    }

    private ConsoleModel createConsoleModel(
            String consoleModelName,
            Integer releaseYear,
            Manufacturer manufacturer
    ) {
        ConsoleModel consoleModel = new ConsoleModel();

        consoleModel.setConsoleModelName(consoleModelName);
        consoleModel.setReleaseYear(releaseYear);
        consoleModel.setManufacturer(manufacturer);

        return consoleModel;
    }

    private Console createConsole(
            User owner,
            ConsoleModel consoleModel,
            String serialNumber,
            String region,
            String color,
            Condition condition,
            Status status,
            BigDecimal estimatedValue,
            String notes
    ) {
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

    private RepairCase createRepairCase(
            Console console,
            String title,
            String description,
            RepairStatus status
    ) {
        RepairCase repairCase = new RepairCase();

        repairCase.setConsole(console);
        repairCase.setTitle(title);
        repairCase.setDescription(description);
        repairCase.setStatus(status);
        repairCase.setStartDate(LocalDateTime.now());

        return repairCase;
    }

    private Component createComponent(
            ConsoleModel consoleModel,
            String name,
            String description
    ) {
        Component component = new Component();

        component.setConsoleModel(consoleModel);
        component.setName(name);
        component.setDescription(description);

        return component;
    }

    private ComponentTest createComponentTest(
            RepairCase repairCase,
            Component component,
            BigDecimal measuredVoltage,
            BigDecimal measuredCurrent,
            BigDecimal measuredResistance,
            BigDecimal temperature,
            TestResult result,
            String notes
    ) {
        ComponentTest componentTest = new ComponentTest();

        componentTest.setRepairCase(repairCase);
        componentTest.setComponent(component);
        componentTest.setMeasuredVoltage(measuredVoltage);
        componentTest.setMeasuredCurrent(measuredCurrent);
        componentTest.setMeasuredResistance(measuredResistance);
        componentTest.setTemperature(temperature);
        componentTest.setResult(result);
        componentTest.setNotes(notes);

        return componentTest;
    }
}
