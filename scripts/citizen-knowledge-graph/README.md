# Citizen Knowledge Graph

This is the prototype for demoing the idea of a "Citizen Knowledge Graph" app and ecosystem. 

Parts of it are implemented and others are only mocked to serve the purpose of the demo. There is also a paragraph-by-paragraph animation on some pages of the app to go well together with my narration in [the video](https://youtube.com/playlist?list=PLyt46q60EbD9-xm2_0MjYisG2OcVBqhjI). Once I pursue this further beyond a demo-prototype, I will move it to it's own repository, add a proper Readme etc.

# How to run

Start the Java Spring Boot app from the root of the repository:

```sh
./gradlew bootRun
```

If you want to empty your triple store, delete the `jena-tdb` folder.

Start the electron app from the `ckg-app` directory:

```sh
cd scripts/citizen-knowledge-graph/ckg-app/
npm install
npm start
```

