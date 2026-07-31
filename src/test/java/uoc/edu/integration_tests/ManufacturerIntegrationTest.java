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
import uoc.edu.dto.ManufacturerRequestDTO;
import uoc.edu.model.ConsoleModel;
import uoc.edu.model.Manufacturer;
import uoc.edu.repository.ConsoleModelRepository;
import uoc.edu.repository.ManufacturerRepository;

import static org.hamcrest.Matchers.hasSize;
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
class ManufacturerIntegrationTest {

    private static final String BASE_URL = "/api/v1/manufacturers";
    private static final String LOGIN_URL = "/api/v1/auth/login";

    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private ManufacturerRepository manufacturerRepository;
    @Autowired
    private ConsoleModelRepository consoleModelRepository;

    private Manufacturer nintendo;
    private Manufacturer sony;
    private String jwt;

    @BeforeEach
    void setUp() throws Exception {
        nintendo = createManufacturer("Nintendo", "JP");
        sony = createManufacturer("Sony", "JP");
        nintendo = manufacturerRepository.save(nintendo);
        sony = manufacturerRepository.save(sony);

        login();
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

    // add jwt to every request so its authorized

    private RequestPostProcessor authenticated() {
        return request -> {request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
            return request;
        };
    }

    // get all manufacturers integration test

    @Test
    void getAllManufacturersReturnsManufacturers() throws Exception {

        mockMvc.perform(get(BASE_URL).with(authenticated()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].manufacturerId").isNumber())
                .andExpect(jsonPath("$[0].manufacturerName")
                        .value("Nintendo"))
                .andExpect(jsonPath("$[0].countryCode")
                        .value("JP"))
                .andExpect(jsonPath("$[1].manufacturerId").isNumber())
                .andExpect(jsonPath("$[1].manufacturerName")
                        .value("Sony"))
                .andExpect(jsonPath("$[1].countryCode")
                        .value("JP"));
    }

    @Test
    void getAllManufacturersReturnsEmptyList() throws Exception {

        manufacturerRepository.deleteAll();
        mockMvc.perform(get(BASE_URL).with(authenticated()).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
    }

    // get manufact by id

    @Test
    void getManufacturerByIdReturnsManufacturer() throws Exception {

        mockMvc.perform(get(BASE_URL + "/{id}", nintendo.getManufacturerId()).with(authenticated()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturerId").value(nintendo.getManufacturerId()))
                .andExpect(jsonPath("$.manufacturerName").value("Nintendo"))
                .andExpect(jsonPath("$.countryCode").value("JP"));
    }

    @Test
    void getManufacturerByIdReturnsNotFoundWhenManufacturerDoesNotExist() throws Exception {

        mockMvc.perform(get(BASE_URL + "/{id}", 999999L).with(authenticated()).accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Manufacturer not found"))
                .andExpect(jsonPath("$.path").value("/api/v1/manufacturers/999999"));
    }

    // add manufacturer, post request

    @Test
    void addManufacturerCreatesManufacturer() throws Exception {

        ManufacturerRequestDTO request = new ManufacturerRequestDTO("Sega", "JP");

        mockMvc.perform(post(BASE_URL).with(authenticated()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.manufacturerId").isNumber())
                .andExpect(jsonPath("$.manufacturerName").value("Sega"))
                .andExpect(jsonPath("$.countryCode").value("JP"));

        Manufacturer savedManufacturer = manufacturerRepository.findByManufacturerName("Sega").orElseThrow();

        assertNotNull(savedManufacturer.getManufacturerId());
        assertEquals("Sega", savedManufacturer.getManufacturerName());
        assertEquals("JP", savedManufacturer.getCountryCode());
    }

    @Test
    void addManufacturerReturnsConflictWhenNameAlreadyExists() throws Exception {

        ManufacturerRequestDTO request = new ManufacturerRequestDTO("Nintendo", "US");
        mockMvc.perform(post(BASE_URL).with(authenticated()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("A manufacturer with this name already exists"))
                .andExpect(jsonPath("$.path").value("/api/v1/manufacturers"));
    }

    // update manufact

    @Test
    void updateManufacturerUpdatesManufacturer() throws Exception {

        ManufacturerRequestDTO request = new ManufacturerRequestDTO("Nintendo Co.", "JP");
        mockMvc.perform(
                        put(BASE_URL + "/{id}", nintendo.getManufacturerId()
                        )
                                .with(authenticated())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturerId")
                        .value(nintendo.getManufacturerId()))
                .andExpect(jsonPath("$.manufacturerName")
                        .value("Nintendo Co."))
                .andExpect(jsonPath("$.countryCode")
                        .value("JP"));

        Manufacturer updatedManufacturer = manufacturerRepository.findById(nintendo.getManufacturerId()).orElseThrow();
        assertEquals("Nintendo Co.", updatedManufacturer.getManufacturerName());
        assertEquals("JP", updatedManufacturer.getCountryCode());
    }

    @Test
    void updateManufacturerAllowsKeepingCurrentName()
            throws Exception {

        ManufacturerRequestDTO request = new ManufacturerRequestDTO("Nintendo", "US");
        mockMvc.perform(put(BASE_URL + "/{id}", nintendo.getManufacturerId())
                                .with(authenticated())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturerId").value(nintendo.getManufacturerId()))
                .andExpect(jsonPath("$.manufacturerName").value("Nintendo"))
                .andExpect(jsonPath("$.countryCode").value("US"));

        Manufacturer updatedManufacturer = manufacturerRepository.findById(nintendo.getManufacturerId()).orElseThrow();
        assertEquals("Nintendo", updatedManufacturer.getManufacturerName());
        assertEquals("US", updatedManufacturer.getCountryCode());
    }

    @Test
    void updateManufacturerReturnsConflictWhenNameBelongsToAnotherManufacturer() throws Exception {

        ManufacturerRequestDTO request = new ManufacturerRequestDTO("Sony", "JP");

        mockMvc.perform(put(BASE_URL + "/{id}", nintendo.getManufacturerId())
                                .with(authenticated())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("A manufacturer with this name already exists"))
                .andExpect(jsonPath("$.path").value(BASE_URL + "/" + nintendo.getManufacturerId()));
        Manufacturer normalManufacturer =manufacturerRepository.findById(nintendo.getManufacturerId()).orElseThrow();
        assertEquals("Nintendo", normalManufacturer.getManufacturerName());
    }

    @Test
    void updateManufacturerReturnsNotFoundWhenManufacturerDoesNotExist() throws Exception {

        ManufacturerRequestDTO request = new ManufacturerRequestDTO("Sega", "JP");
        mockMvc.perform(put(BASE_URL + "/{id}", 999999L).with(authenticated()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Manufacturer not found"))
                .andExpect(jsonPath("$.path").value("/api/v1/manufacturers/999999"));
    }

    // delete manufacturer

    @Test
    void deleteManufacturerDeletesManufacturer() throws Exception {

        Long manufacturerId = nintendo.getManufacturerId();

        mockMvc.perform(delete(BASE_URL + "/{id}", manufacturerId).with(authenticated())).andExpect(status().isOk());
        assertFalse(manufacturerRepository.existsById(manufacturerId));
    }

    @Test
    void deleteManufacturerReturnsNotFoundWhenManufacturerDoesNotExist() throws Exception {

        mockMvc.perform(delete(BASE_URL + "/{id}", 999999L).with(authenticated()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Manufacturer not found"))
                .andExpect(jsonPath("$.path").value("/api/v1/manufacturers/999999"));
    }

    @Test
    void deleteManufacturerReturnsConflictWhenManufacturerHasConsoleModels() throws Exception {
        ConsoleModel consoleModel = createConsoleModel("Game Boy", 1989, nintendo);
        consoleModelRepository.save(consoleModel);
        mockMvc.perform(
                        delete(BASE_URL + "/{id}", nintendo.getManufacturerId()).with(authenticated())
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Cannot delete a manufacturer with " + "associated console models"))
                .andExpect(jsonPath("$.path").value(BASE_URL + "/" + nintendo.getManufacturerId()));
        assertTrue(
                manufacturerRepository.existsById(nintendo.getManufacturerId()));
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
}