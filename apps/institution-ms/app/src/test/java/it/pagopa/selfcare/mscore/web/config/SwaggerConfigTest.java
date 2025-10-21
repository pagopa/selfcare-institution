package it.pagopa.selfcare.mscore.web.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.pagopa.selfcare.mscore.api.UserRegistryConnector;
import it.pagopa.selfcare.mscore.core.DelegationService;
import it.pagopa.selfcare.mscore.core.ExternalService;
import it.pagopa.selfcare.mscore.core.InstitutionService;
import it.pagopa.selfcare.mscore.core.OnboardingService;
import it.pagopa.selfcare.mscore.web.util.EncryptedTaxCodeParamResolver;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.oas.annotations.EnableOpenApi;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {
        SwaggerConfig.class,
        WebConfig.class
})
@EnableOpenApi
@EnableWebMvc
@Slf4j
@ComponentScan(basePackages = {"it.pagopa.selfcare.mscore.web.controller","it.pagopa.selfcare.mscore.web.model"})
@TestPropertySource(locations = "classpath:config/application.yml")
class SwaggerConfigTest {

    @MockBean ExternalService externalService;
    @MockBean InstitutionService institutionService;
    @MockBean OnboardingService onboardingService;
    @MockBean DelegationService delegationService;
    @MockBean private UserRegistryConnector userRegistryConnector;
    @MockBean private EncryptedTaxCodeParamResolver encryptedTaxCodeParamResolver;

    @Autowired private WebApplicationContext context;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void swaggerSpringPlugin() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        mockMvc.perform(MockMvcRequestBuilders.get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andDo(result -> {
                    assertNotNull(result);
                    assertNotNull(result.getResponse());

                    String content = result.getResponse().getContentAsString();
                    checkPlaceholders(content);
                    assertFalse(content.isBlank());
                    assertFalse(content.contains("${"), "Generated swagger contains placeholders");

                    // Leggi JSON
                    JsonNode rootNode = objectMapper.readTree(content);

                    // Trasforma tutti i parametri con //@EncryptedPathVariable in path param
                    JsonNode pathsNode = rootNode.path("paths");
                    Iterator<Map.Entry<String, JsonNode>> pathEntries = pathsNode.fields();
                    while (pathEntries.hasNext()) {
                        Map.Entry<String, JsonNode> pathEntry = pathEntries.next();
                        JsonNode operationsNode = pathEntry.getValue();
                        for (Iterator<Map.Entry<String, JsonNode>> itOp = operationsNode.fields(); itOp.hasNext(); ) {
                            Map.Entry<String, JsonNode> opEntry = itOp.next();
                            JsonNode parameters = opEntry.getValue().path("parameters");
                            if (parameters.isArray()) {
                                ArrayNode paramArray = (ArrayNode) parameters;
                                for (JsonNode paramNode : paramArray) {
                                    if (paramNode.has("description") && paramNode.get("description").asText().contains("//@EncryptedPathVariable")) {
                                        ObjectNode paramObj = (ObjectNode) paramNode;
                                        paramObj.put("in", "path");
                                        paramObj.put("required", true);
                                        paramObj.put("style", "simple"); // <- aggiunto
                                        // pulizia del commento
                                        String cleanedDesc = paramNode.get("description").asText().replace("//@EncryptedPathVariable", "").trim();
                                        paramObj.put("description", cleanedDesc);
                                    }
                                }
                            }
                        }
                    }

                    // Salva JSON modificato
                    String formatted = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
                    Path basePath = Paths.get("src/main/docs/");
                    Files.createDirectories(basePath);
                    Files.write(basePath.resolve("openapi.json"), formatted.getBytes());
                });
    }

    private static void checkPlaceholders(String content) {
        Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            log.error("\033[31m An error occurred with placeholder: {}", matcher.group(1));
        }
    }
}
