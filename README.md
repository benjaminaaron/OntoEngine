# DIY Knowledge Graph
_I'll update this Readme soon. Some new ideas have come up since making the video a year ago..._

An idea conglomerate for continuous ontologization: an ontology design assistant working with you towards continuous expansion and consolidation.

### 📺 Video presentation of the project
👉 **[youtu.be/-AwxUrsVxsI](https://youtu.be/-AwxUrsVxsI)**

Direct links to chapters in the presentation:
- [4:56](https://youtu.be/-AwxUrsVxsI?t=296) Basic ideas
- [7:23](https://youtu.be/-AwxUrsVxsI?t=443) Prototype
  - [18:24](https://youtu.be/-AwxUrsVxsI?t=1104) graph-query-visualizer
- [25:15](https://youtu.be/-AwxUrsVxsI?t=1515) Wizards
- [33:37](https://youtu.be/-AwxUrsVxsI?t=2017) Graph operations overview
- [37:31](https://youtu.be/-AwxUrsVxsI?t=2251) Use cases
- [39:51](https://youtu.be/-AwxUrsVxsI?t=2391) What's next

<a href="https://youtu.be/-AwxUrsVxsI"><img src="https://user-images.githubusercontent.com/5141792/142049173-06e2213a-634f-4935-8418-6273ad8534d8.png"></a>
 
[One-pager](https://docs.google.com/document/d/1oFeoyaKU1pk_UaqobcYeuxGRNrhjDrPll8R-_co4xR8/edit?usp=sharing) | [Slides](https://docs.google.com/presentation/d/1-2TvOkOQuzK_5s9oh00t7KaHmwk38R_YrgscAnXNpqA/edit?usp=sharing) | [Video Script](https://docs.google.com/document/d/1rC8gi-aBkMEKHY9_RfVWvlpltHNxyaVO3MhI8JdPZ-I/edit?usp=sharing) 
 
### Readme from before making the video presentation
 
Early work in progress of an ontology engine working with you towards continuous expansion and consolidation. Like an assistant to help you design ontologies. The idea is to be able to quickly and smoothly add triples about anything. The engine will work towards making the most salient, clean and consolidated version of whatever knowledge base you are creating: by correcting typos, suggesting merges of used vocabulary (noticed via thesaurus or through structurally similar use) or suggestions to use existing vocabulary if it is known to the engine or other means. At the same time you get encouraged to expand the knowledge base with cues like: "you used predicate X on resources of type Y - I have also seen usage of predicate Z alongside X when it comes to Y-resources, maybe formulating a Z-statement makes sense here too?". A collaborative mode where other users can suggest things is also in planning. By keeping the triples in a triple store and as a directed graph at the same time, I want to harness what both of these structures enable in terms of reasoning, discovery, integrations and queries. I have a few ideas what this might be useful for in the end. But for now I'd like to focus on that engine first. From my previous work with semantic data I know well that what you can get out of semantic data with ever so smart queries very much depends on how rich, connected and cared for the data is in the first place.

## Tech stack

- The server is a [Spring Boot](https://spring.io/projects/spring-boot) application that can be talked to via REST and STOMP WebSocket
- [Apache Jena](https://jena.apache.org/) with [TBD](https://jena.apache.org/documentation/tdb/) for persistance
- Import from and export to [GraphDB](https://graphdb.ontotext.com/) repositories
- [JGraphT](https://jgrapht.org/) for having all triples available in a graph structure
- [Wikidata Toolkit](https://github.com/Wikidata/Wikidata-Toolkit) (just the wdtk-wikibaseapi module) to find matching entities on @Wikidata.

For clients I want to prototype a few different options eventually: a neat web app, a command line utility, etc. For now I am using a static site that Spring serves when running.

## Setup in IntelliJ IDEA

Use *Open* to navigate to the respective project-`build.gradle` and choose *Open as Project*. This will set it up correctly as Gradle project and download the dependencies.
