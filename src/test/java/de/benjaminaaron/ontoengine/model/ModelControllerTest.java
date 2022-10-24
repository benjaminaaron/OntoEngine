package de.benjaminaaron.ontoengine.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.benjaminaaron.ontoengine.adapter.primary.messages.AddStatementMessage;
import de.benjaminaaron.ontoengine.domain.ModelController;
import java.util.List;
import org.apache.jena.rdf.model.Statement;
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
        AddStatementMessage msg = new AddStatementMessage();
        msg.setSubject("http://onto.de/default#sub");
        msg.setPredicate("http://onto.de/default#pred");
        msg.setObject("http://onto.de/default#obj");
        msg.setObjectIsLiteral(false);
        modelController.addStatement(msg, false);

        List<Statement> statements = TestUtils.statementIteratorToList(
            modelController.getMainModel().listStatements());

        assertEquals(1, statements.size());
        assertEquals(msg.getSubject(), statements.get(0).getSubject().toString());
    }
}
