package de.benjaminaaron.ontoengine.domain.exporter;

import static de.benjaminaaron.ontoengine.domain.Utils.DEFAULT_URI_NAMESPACE;
import static de.benjaminaaron.ontoengine.domain.Utils.determineShortestUriRepresentation;
import static de.benjaminaaron.ontoengine.domain.Utils.writeLine;
import static de.benjaminaaron.ontoengine.domain.Utils.writeQueryLine;
import static de.benjaminaaron.ontoengine.domain.Utils.writeSectionHeadline;

import de.benjaminaaron.ontoengine.domain.ModelController;
import de.benjaminaaron.ontoengine.domain.Utils;
import de.benjaminaaron.ontoengine.domain.graph.Edge;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MarkdownExporter {
    private static final Logger logger = LogManager.getLogger(MarkdownExporter.class);

    private static Path MARKDOWN_DEFAULT_DIRECTORY;

    @Value("${markdown.export.directory}")
    public void setMarkdownDefaultDirectory(Path dir) {
        MarkdownExporter.MARKDOWN_DEFAULT_DIRECTORY = dir;
    }

    @SneakyThrows
    public static void export(ModelController modelController, String folderName) {
        Path markdownDir = Utils.getObsidianICloudDir(folderName); // MARKDOWN_DEFAULT_DIRECTORY
        // sync mechanism? TODO

        try {
            // Add a warning-step first or make a backup copy? This could lead to accidental data loss
            FileUtils.deleteDirectory(markdownDir.toFile());
        } catch (IOException e) {
            logger.error("Could not delete directory " + markdownDir + ", aborting markdown export");
            return;
        }

        if (!markdownDir.toFile().mkdirs()) {
            logger.error("Could not (re)create directory " + markdownDir + ", aborting markdown export");
            return;
        }

        // Write PREFIXES.md
        Map<String, String> prefixes = modelController.getMainModel().getNsPrefixMap();
        try(FileWriter fw = new FileWriter(markdownDir.resolve("PREFIXES.md").toFile())) {
            for (String key : prefixes.keySet()) {
                writeLine(fw, key + ":" + prefixes.get(key));
            }
        }

        // Write QUERIES.md
        String query = "PREFIX : <http://onto.de/default#> "
            + "SELECT * WHERE { "
            +   "?queryName ?queryType ?queryString . "
            +   "VALUES ?queryType { :hasPeriodicQueryTemplate :hasPeriodicQuery } "
            +   "OPTIONAL { "
            +       "?queryName :wasInstantiatedFromTemplate ?instTemplate . "
            +       "?queryName :hasInstantiationParameters ?instParams . "
            +   "}"
            +   "OPTIONAL { "
            +       "?queryName :hasOriginalIFTTTstring ?iftttString . "
            +   "} "
            +   "BIND(IF(BOUND(?instTemplate), :instantiated, IF(BOUND(?iftttString), :z_ifttt, ?queryType)) AS ?auxiliaryTypeIndicator) . "
            + "} ORDER BY ?auxiliaryTypeIndicator";
        try(QueryExecution queryExecution = QueryExecutionFactory.create(query, modelController.getMetaHandler().getMetaDataModel());
            FileWriter fw = new FileWriter(markdownDir.resolve("QUERIES.md").toFile())) {
            ResultSet resultSet = queryExecution.execSelect();
            // ResultSetFormatter.out(resultSet);
            String previousSectionName = "", sectionName = "";
            while(resultSet.hasNext()) {
                QuerySolution qs = resultSet.next();
                String queryName = qs.get("queryName").asResource().getLocalName();
                String queryType = qs.get("queryType").asResource().getLocalName();
                String queryString = qs.get("queryString").asLiteral().getString();
                String auxiliaryTypeIndicator = qs.get("auxiliaryTypeIndicator").asResource().getLocalName();
                // remove the need for the same switch-statement twice somehow?
                switch (auxiliaryTypeIndicator) {
                    case "hasPeriodicQuery":
                        sectionName = "periodic queries";
                        break;
                    case "hasPeriodicQueryTemplate":
                        sectionName = "periodic query templates";
                        break;
                    case "instantiated":
                        sectionName = "periodic queries instantiated from templates";
                        break;
                    case "z_ifttt":
                        sectionName = "IFTTT definitions";
                        break;
                }
                if (!previousSectionName.equals(sectionName)) {
                    writeSectionHeadline(fw, sectionName);
                    previousSectionName = sectionName;
                }
                switch (auxiliaryTypeIndicator) {
                    case "hasPeriodicQuery":
                    case "hasPeriodicQueryTemplate":
                        writeQueryLine(fw, queryName, queryType, queryString);
                        break;
                    case "instantiated":
                        String instTemplate = qs.get("instTemplate").asLiteral().getString();
                        String instParams = qs.get("instParams").asLiteral().getString();
                        writeLine(fw, instTemplate + " instantiatePeriodicQueryTemplateFor \""
                            + instParams + "\"");
                        break;
                    case "z_ifttt":
                        String iftttString = qs.get("iftttString").asLiteral().getString();
                        writeLine(fw, queryName + " ifttt \"" + iftttString + "\"");
                        break;
                }
            }
        }

        // Write an .md file for each vertex
        Graph<RDFNode, Edge> graph = modelController.getGraphManager().getGraph();
        for (RDFNode node : graph.vertexSet()) {
            if (node.isLiteral() || graph.outgoingEdgesOf(node).isEmpty()) {
                continue;
            }
            Resource source = node.asResource();
            Path newFile = markdownDir.resolve(source.getLocalName() + ".md");
            try(FileWriter fw = new FileWriter(newFile.toFile())) {
                if (!source.getNameSpace().equals(DEFAULT_URI_NAMESPACE)) {
                    writeLine(fw, determineShortestUriRepresentation(prefixes, source));
                    writeLine(fw, "");
                }
                for (Edge edge : graph.outgoingEdgesOf(node)) {
                    RDFNode target = graph.getEdgeTarget(edge);
                    writeLine(fw, determineShortestUriRepresentation(prefixes, edge.property)
                        + " " + getObjectString(modelController, target));
                }
            }
        }

        String text = "Export to markdown files completed";
        logger.info(text);
        modelController.broadcastToChangeListeners(text);
    }

    private static String getObjectString(ModelController modelController, RDFNode target) {
        if (target.isLiteral()) {
            return "\"" + target.asLiteral().getString() + "\"";
        }
        if (modelController.getGraphManager().getGraph().outgoingEdgesOf(target).isEmpty()) {
            // this probably has to be removed when we want to be serious about namespaces TODO
            return target.asResource().getLocalName();
        }
        return "[[" + target.asResource().getLocalName() + "]]";
    }
}
