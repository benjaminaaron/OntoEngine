package de.benjaminaaron.ontoengine.model.io;

import de.benjaminaaron.ontoengine.model.MetaHandler;
import de.benjaminaaron.ontoengine.model.ModelController;
import de.benjaminaaron.ontoengine.model.Utils;
import de.benjaminaaron.ontoengine.model.graph.Edge;
import java.io.File;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.system.Txn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static de.benjaminaaron.ontoengine.model.Utils.*;

@Component
public class Exporter {

    private final Logger logger = LogManager.getLogger(Exporter.class);

    @Value("${model.export.directory}")
    private Path EXPORT_DIRECTORY;
    @Value("${markdown.export.directory}")
    private Path MARKDOWN_DEFAULT_DIRECTORY;
    @Value("${graphdb.insert-url}")
    private String GRAPHDB_INSERT_URL;
    @Value("classpath:graphdb_repo_template.json")
    private Path GRAPHDB_REPO_TEMPLATE;
    @Value("${graphdb.rest-url}")
    private String GRAPHDB_REST_URL;
    @Value("${graphdb.default-repository}")
    private String GRAPHDB_DEFAULT_REPOSITORY;

    @Autowired
    private ModelController modelController;

    @Autowired
    private MetaHandler metaHandler;

    public Path exportRDF(String modelName) {
        Model model = null;
        String extension = "ttl";
        switch (modelName) {
            case "main":
                model = modelController.getMainModel();
                break;
            case "meta":
                model = modelController.getMetaHandler().getMetaDataModel();
                extension = "owl";
                break;
        }
        File exportFile = getExportFile(EXPORT_DIRECTORY, modelName, extension);
        try (FileOutputStream fos = new FileOutputStream(exportFile)) {
            assert model != null;
            model.write(fos, "TURTLE"); // RDF/XML, via https://jena.apache.org/documentation/io/rdf-output.html
        } catch (IOException e) {
            e.printStackTrace();
        }
        return exportFile.toPath();
    }

    public void exportGraphml(boolean fullUri) {
        modelController.getGraphManager().exportGraphml(getExportFile(EXPORT_DIRECTORY, "model", "graphml"), fullUri);
    }

    @SneakyThrows
    public void exportMarkdown(String folderName) {
        Path markdownDir = Utils.getObsidianICloudDir(folderName);
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
        try(QueryExecution queryExecution = QueryExecutionFactory.create(query, metaHandler.getMetaDataModel());
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
                        + " " + getObjectString(target));
                }
            }
        }

        String text = "Export to markdown files completed";
        logger.info(text);
        modelController.broadcastToChangeListeners(text);
    }

    private String getObjectString(RDFNode target) {
        if (target.isLiteral()) {
            return "\"" + target.asLiteral().getString() + "\"";
        }
        if (modelController.getGraphManager().getGraph().outgoingEdgesOf(target).isEmpty()) {
            // this probably has to be removed when we want to be serious about namespaces TODO
            return target.asResource().getLocalName();
        }
        return "[[" + target.asResource().getLocalName() + "]]";
    }

    @SneakyThrows
    public void exportToGraphDB(String modelName, String ruleset) {
        // allow passing of a "clear" flag TODO
        String repoName = GRAPHDB_DEFAULT_REPOSITORY + "_" + modelName + "_" + ruleset;
        String insertURL = GRAPHDB_INSERT_URL.replace("<repository>", repoName);

        // delete old repo

        URL url = new URL(insertURL);
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("DELETE");
        http.setRequestProperty("Accept", "application/json");
        if (http.getResponseCode() != 204) {
            System.out.println("Could not delete the GraphDB repository. Response code: " + http.getResponseCode());
        }
        http.disconnect();

        // create new repo

        url = new URL(GRAPHDB_REST_URL);
        http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setRequestProperty("Content-Type", "application/json");
        http.setRequestProperty("Accept", "application/json");
        String jsonStr = Files.readString(GRAPHDB_REPO_TEMPLATE, StandardCharsets.UTF_8)
                .replace("<id>", repoName)
                .replace("<ruleset>", ruleset);
        http.getOutputStream().write(jsonStr.getBytes(StandardCharsets.UTF_8));
        if (http.getResponseCode() != 201) {
            System.out.println("Could not create new GraphDB repository. Response code: " + http.getResponseCode());
        }
        http.disconnect();

        // add triples to new repo

        Model model = modelName.equals("main") ? modelController.getMainModel() : modelController.getMetaModel();
        try (RDFConnection conn = RDFConnectionFactory.connect(insertURL)) {
            Txn.executeWrite(conn, () -> {
                StmtIterator iterator = model.listStatements();
                while (iterator.hasNext()) {
                    Statement statement = iterator.nextStatement();
                    String sUri = statement.getSubject().getURI();
                    String pUri = statement.getPredicate().getURI();
                    String objStr = rdfNodeToGraphDatabaseEntryString(statement.getObject());
                    conn.update("INSERT DATA { <" + sUri + "> <" + pUri + "> " + objStr + " }");
                }
            });
        }
    }
}
