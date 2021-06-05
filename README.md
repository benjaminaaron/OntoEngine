# OntoEngine

Early work in progress of an ontology engine working with you towards continuous expansion and consolidation. Like an assistant to help you design ontologies. The idea is to be able to quickly and smoothly add triples about anything. The engine will work towards making the most salient, clean and consolidated version of whatever knowledge base you are creating: by correcting typos, suggesting merges of used vocabulary (noticed via thesaurus or through structurally similar use) or suggestions to use existing vocabulary if it is known to the engine or other means. At the same time you get encouraged to expand the knowledge base with cues like: "you used predicate X on resources of type Y - I have also seen usage of predicate Z alongside X when it comes to Y-resources, maybe formulating a Z-statement makes sense here too?". A collaborative mode where other users can suggest things is also in planning.

By keeping the triples in a triple store and as a directed graph at the same time, I want to harness what both of these structures enable in terms of reasoning, discovery, integrations and queries.

I have a few ideas what this might be useful for in the end. But for now I'd like to focus on that engine first. From my previous work with semantic data I know well that what you can get out of semantic data with ever so smart queries very much depends on how rich, connected and cared for the data is in the first place.

## Tech stack
Spring Boot for the server that can be talked to via REST and via WebSocket. For clients I want to build a few different options. The server itself hosts a static site that connects via WebSocket. A JavaFX-GUI that also uses Spring Boot WebSockets. A Telegram bot and a CLI are up next.
The server runs Apache Jena with TBD for persistance. Export to and import from GraphDB. JGraphT for the graph structure. 

## Setup in IntelliJ IDEA

Use *Open* to navigate to the respective project-`pom.xml` and choose *Open as Project*. This will set it up correctly as Maven project, resolve the dependencies, detect the Spring Boot run configuration etc. Note that in the `onto-client-javafx` project, the correct run configuration is not the autodetected one though. Set one up to use the `main()`-method in `OntoClientJavafxApplication` instead.
