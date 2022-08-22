package de.benjaminaaron.ontoengine.model;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles(profiles = "test")
public class ModelControllerTest {

    @Autowired private ModelController modelController;

    @Test
    void modelControllerLoadsModelsCorrectly() {
        assertNotNull(modelController.getMainModel());
    }
}
