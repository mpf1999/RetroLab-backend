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
import uoc.edu.dto.ComponentRequestDTO;
import uoc.edu.model.Component;
import uoc.edu.model.ConsoleModel;
import uoc.edu.model.Manufacturer;
import uoc.edu.repository.ComponentRepository;
import uoc.edu.repository.ConsoleModelRepository;
import uoc.edu.repository.ManufacturerRepository;

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
class ComponentIntegrationTest {

    private static final String BASE_URL = "/api/v1/components";
    private static final String LOGIN_URL = "/api/v1/auth/login";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ComponentRepository componentRepository;

    @Autowired
    private ConsoleModelRepository consoleModelRepository;

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    private Manufacturer nintendo;
    private Manufacturer sony;

    private ConsoleModel gameBoy;
    private ConsoleModel playStation;

    private Component gameBoyScreen;
    private Component gameBoySpeaker;

    private String jwt;

    @BeforeEach
    void setUp() throws Exception {

        componentRepository.deleteAll();
        consoleModelRepository.deleteAll();
        manufacturerRepository.deleteAll();

        nintendo = createManufacturer(
                "Nintendo",
                "JP"
        );

        sony = createManufacturer(
                "Sony",
                "JP"
        );

        nintendo = manufacturerRepository.save(nintendo);
        sony = manufacturerRepository.save(sony);

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

        gameBoy = consoleModelRepository.save(gameBoy);
        playStation = consoleModelRepository.save(playStation);

        gameBoyScreen = createComponent(
                gameBoy,
                "Screen",
                "Original Game Boy LCD screen"
        );

        gameBoySpeaker = createComponent(
                gameBoy,
                "Speaker",
                "Original Game Boy speaker"
        );

        gameBoyScreen = componentRepository.save(gameBoyScreen);
        gameBoySpeaker = componentRepository.save(gameBoySpeaker);

        login();
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

        jwt = JsonPath.read(responseBody, "$.token");

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
     GET all components
     */

    @Test
    void getAllComponentsReturnsComponents() throws Exception {

        mockMvc.perform(
                        get(BASE_URL)
                                .with(authenticated())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))

                .andExpect(jsonPath("$[0].componentId")
                        .value(gameBoyScreen.getComponentId()))
                .andExpect(jsonPath("$[0].consoleModelId")
                        .value(gameBoy.getConsoleModelId()))
                .andExpect(jsonPath("$[0].name")
                        .value("Screen"))
                .andExpect(jsonPath("$[0].description")
                        .value("Original Game Boy LCD screen"))

                .andExpect(jsonPath("$[1].componentId")
                        .value(gameBoySpeaker.getComponentId()))
                .andExpect(jsonPath("$[1].consoleModelId")
                        .value(gameBoy.getConsoleModelId()))
                .andExpect(jsonPath("$[1].name")
                        .value("Speaker"))
                .andExpect(jsonPath("$[1].description")
                        .value("Original Game Boy speaker"));
    }

    @Test
    void getAllComponentsReturnsEmptyList() throws Exception {

        componentRepository.deleteAll();

        mockMvc.perform(
                        get(BASE_URL)
                                .with(authenticated())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /*
     GET component by ID
     */

    @Test
    void getComponentByIdReturnsCorrectData() throws Exception {

        mockMvc.perform(
                        get(
                                BASE_URL + "/{id}",
                                gameBoyScreen.getComponentId()
                        )
                                .with(authenticated())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.componentId")
                        .value(gameBoyScreen.getComponentId()))
                .andExpect(jsonPath("$.consoleModelId")
                        .value(gameBoy.getConsoleModelId()))
                .andExpect(jsonPath("$.name")
                        .value("Screen"))
                .andExpect(jsonPath("$.description")
                        .value("Original Game Boy LCD screen"));
    }

    @Test
    void getComponentByIdReturnsNotFoundWhenComponentDoesNotExist()
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
                        .value("Component not found"))
                .andExpect(jsonPath("$.path")
                        .value(BASE_URL + "/999999"));
    }

    /*
     POST component
     */

    @Test
    void addComponentSavesCorrectData() throws Exception {

        ComponentRequestDTO request =
                new ComponentRequestDTO(
                        playStation.getConsoleModelId(),
                        "Power supply",
                        "Internal PlayStation power supply"
                );

        mockMvc.perform(
                        post(BASE_URL)
                                .with(authenticated())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.componentId")
                        .isNumber())
                .andExpect(jsonPath("$.consoleModelId")
                        .value(playStation.getConsoleModelId()))
                .andExpect(jsonPath("$.name")
                        .value("Power supply"))
                .andExpect(jsonPath("$.description")
                        .value("Internal PlayStation power supply"));

        Component savedComponent =
                componentRepository.findAll()
                        .stream()
                        .filter(component ->
                                component.getName()
                                        .equals("Power supply"))
                        .findFirst()
                        .orElseThrow();

        assertNotNull(savedComponent);

        assertAll(
                () -> assertNotNull(
                        savedComponent.getComponentId()
                ),
                () -> assertEquals(
                        playStation.getConsoleModelId(),
                        savedComponent
                                .getConsoleModel()
                                .getConsoleModelId()
                ),
                () -> assertEquals(
                        "PlayStation",
                        savedComponent
                                .getConsoleModel()
                                .getConsoleModelName()
                ),
                () -> assertEquals(
                        "Power supply",
                        savedComponent.getName()
                ),
                () -> assertEquals(
                        "Internal PlayStation power supply",
                        savedComponent.getDescription()
                )
        );
    }

    @Test
    void addComponentReturnsConflictWhenNameAlreadyExistsForConsoleModel()
            throws Exception {

        ComponentRequestDTO request =
                new ComponentRequestDTO(
                        gameBoy.getConsoleModelId(),
                        "Screen",
                        "Another screen for the same console model"
                );

        mockMvc.perform(
                        post(BASE_URL)
                                .with(authenticated())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status")
                        .value(409))
                .andExpect(jsonPath("$.error")
                        .value("Conflict"))
                .andExpect(jsonPath("$.path")
                        .value(BASE_URL));

        assertEquals(
                2,
                componentRepository.count()
        );
    }

    @Test
    void addComponentAllowsSameNameForDifferentConsoleModel()
            throws Exception {

        ComponentRequestDTO request =
                new ComponentRequestDTO(
                        playStation.getConsoleModelId(),
                        "Screen",
                        "PlayStation video output component"
                );

        mockMvc.perform(
                        post(BASE_URL)
                                .with(authenticated())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.consoleModelId")
                        .value(playStation.getConsoleModelId()))
                .andExpect(jsonPath("$.name")
                        .value("Screen"))
                .andExpect(jsonPath("$.description")
                        .value("PlayStation video output component"));

        Component savedComponent =
                componentRepository.findAll()
                        .stream()
                        .filter(component ->
                                component.getName().equals("Screen")
                                        && component.getConsoleModel()
                                        .getConsoleModelId()
                                        .equals(
                                                playStation
                                                        .getConsoleModelId()
                                        ))
                        .findFirst()
                        .orElseThrow();

        assertAll(
                () -> assertNotNull(
                        savedComponent.getComponentId()
                ),
                () -> assertEquals(
                        "Screen",
                        savedComponent.getName()
                ),
                () -> assertEquals(
                        playStation.getConsoleModelId(),
                        savedComponent
                                .getConsoleModel()
                                .getConsoleModelId()
                ),
                () -> assertEquals(
                        3,
                        componentRepository.count()
                )
        );
    }

    @Test
    void addComponentReturnsNotFoundWhenConsoleModelDoesNotExist()
            throws Exception {

        ComponentRequestDTO request =
                new ComponentRequestDTO(
                        999999L,
                        "CPU",
                        "Component with invalid console model"
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
                .andExpect(jsonPath("$.message")
                        .value("Console model not found"))
                .andExpect(jsonPath("$.path")
                        .value(BASE_URL));
    }

    /*
     PUT component
     */

    @Test
    void updateComponentSavesCorrectData() throws Exception {

        Long componentId =
                gameBoyScreen.getComponentId();

        ComponentRequestDTO request =
                new ComponentRequestDTO(
                        playStation.getConsoleModelId(),
                        "Video output",
                        "Updated video component"
                );

        mockMvc.perform(
                        put(BASE_URL + "/{id}", componentId)
                                .with(authenticated())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.componentId")
                        .value(componentId))
                .andExpect(jsonPath("$.consoleModelId")
                        .value(playStation.getConsoleModelId()))
                .andExpect(jsonPath("$.name")
                        .value("Video output"))
                .andExpect(jsonPath("$.description")
                        .value("Updated video component"));

        Component updatedComponent =
                componentRepository.findById(componentId)
                        .orElseThrow();

        assertNotNull(updatedComponent);

        assertAll(
                () -> assertEquals(
                        componentId,
                        updatedComponent.getComponentId()
                ),
                () -> assertEquals(
                        playStation.getConsoleModelId(),
                        updatedComponent
                                .getConsoleModel()
                                .getConsoleModelId()
                ),
                () -> assertEquals(
                        "PlayStation",
                        updatedComponent
                                .getConsoleModel()
                                .getConsoleModelName()
                ),
                () -> assertEquals(
                        "Video output",
                        updatedComponent.getName()
                ),
                () -> assertEquals(
                        "Updated video component",
                        updatedComponent.getDescription()
                )
        );
    }

    @Test
    void updateComponentAllowsKeepingCurrentName()
            throws Exception {

        Long componentId =
                gameBoyScreen.getComponentId();

        ComponentRequestDTO request =
                new ComponentRequestDTO(
                        gameBoy.getConsoleModelId(),
                        "Screen",
                        "Updated screen description"
                );

        mockMvc.perform(
                        put(BASE_URL + "/{id}", componentId)
                                .with(authenticated())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.componentId")
                        .value(componentId))
                .andExpect(jsonPath("$.consoleModelId")
                        .value(gameBoy.getConsoleModelId()))
                .andExpect(jsonPath("$.name")
                        .value("Screen"))
                .andExpect(jsonPath("$.description")
                        .value("Updated screen description"));

        Component updatedComponent =
                componentRepository.findById(componentId)
                        .orElseThrow();

        assertAll(
                () -> assertEquals(
                        "Screen",
                        updatedComponent.getName()
                ),
                () -> assertEquals(
                        "Updated screen description",
                        updatedComponent.getDescription()
                ),
                () -> assertEquals(
                        gameBoy.getConsoleModelId(),
                        updatedComponent
                                .getConsoleModel()
                                .getConsoleModelId()
                )
        );
    }

    @Test
    void updateComponentReturnsConflictWhenNameBelongsToAnotherComponent()
            throws Exception {

        Long componentId =
                gameBoyScreen.getComponentId();

        ComponentRequestDTO request =
                new ComponentRequestDTO(
                        gameBoy.getConsoleModelId(),
                        "Speaker",
                        "Duplicate speaker component"
                );

        mockMvc.perform(
                        put(BASE_URL + "/{id}", componentId)
                                .with(authenticated())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status")
                        .value(409))
                .andExpect(jsonPath("$.error")
                        .value("Conflict"))
                .andExpect(jsonPath("$.path")
                        .value(BASE_URL + "/" + componentId));

        Component unchangedComponent =
                componentRepository.findById(componentId)
                        .orElseThrow();

        assertAll(
                () -> assertEquals(
                        "Screen",
                        unchangedComponent.getName()
                ),
                () -> assertEquals(
                        "Original Game Boy LCD screen",
                        unchangedComponent.getDescription()
                ),
                () -> assertEquals(
                        gameBoy.getConsoleModelId(),
                        unchangedComponent
                                .getConsoleModel()
                                .getConsoleModelId()
                )
        );
    }

    @Test
    void updateComponentAllowsSameNameWhenConsoleModelChanges()
            throws Exception {

        Component playStationScreen = createComponent(
                playStation,
                "Screen",
                "PlayStation screen component"
        );

        playStationScreen =
                componentRepository.save(playStationScreen);

        Long componentId =
                gameBoySpeaker.getComponentId();

        ComponentRequestDTO request =
                new ComponentRequestDTO(
                        playStation.getConsoleModelId(),
                        "Speaker",
                        "PlayStation speaker"
                );

        mockMvc.perform(
                        put(BASE_URL + "/{id}", componentId)
                                .with(authenticated())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.componentId")
                        .value(componentId))
                .andExpect(jsonPath("$.consoleModelId")
                        .value(playStation.getConsoleModelId()))
                .andExpect(jsonPath("$.name")
                        .value("Speaker"));

        Component updatedComponent =
                componentRepository.findById(componentId)
                        .orElseThrow();

        assertAll(
                () -> assertEquals(
                        "Speaker",
                        updatedComponent.getName()
                ),
                () -> assertEquals(
                        playStation.getConsoleModelId(),
                        updatedComponent
                                .getConsoleModel()
                                .getConsoleModelId()
                ),
                () -> assertEquals(
                        "PlayStation speaker",
                        updatedComponent.getDescription()
                )
        );
    }

    @Test
    void updateComponentReturnsNotFoundWhenComponentDoesNotExist()
            throws Exception {

        ComponentRequestDTO request =
                new ComponentRequestDTO(
                        gameBoy.getConsoleModelId(),
                        "CPU",
                        "Updated CPU"
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
                        .value("Component not found"))
                .andExpect(jsonPath("$.path")
                        .value(BASE_URL + "/999999"));
    }

    @Test
    void updateComponentReturnsNotFoundWhenConsoleModelDoesNotExist()
            throws Exception {

        ComponentRequestDTO request =
                new ComponentRequestDTO(
                        999999L,
                        "Screen",
                        "Invalid console model"
                );

        mockMvc.perform(
                        put(
                                BASE_URL + "/{id}",
                                gameBoyScreen.getComponentId()
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
                        .value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Console model not found"))
                .andExpect(jsonPath("$.path")
                        .value(
                                BASE_URL + "/"
                                        + gameBoyScreen.getComponentId()
                        ));
    }

    /*
     DELETE component
     */

    @Test
    void deleteComponentRemovesCorrectData() throws Exception {

        Long componentId =
                gameBoyScreen.getComponentId();

        mockMvc.perform(
                        delete(BASE_URL + "/{id}", componentId)
                                .with(authenticated())
                )
                .andExpect(status().isOk());

        assertAll(
                () -> assertFalse(
                        componentRepository.existsById(componentId)
                ),
                () -> assertTrue(
                        componentRepository.existsById(
                                gameBoySpeaker.getComponentId()
                        )
                )
        );
    }

    @Test
    void deleteComponentReturnsNotFoundWhenComponentDoesNotExist()
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
                        .value("Component not found"))
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
}
