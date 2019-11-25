import React, { useEffect, useState } from 'react';
import './App.css';
import $ from 'jquery';

const repositoryURL = 'http://localhost:7200/repositories/fin4kb';

function App() {

    const [data, setData] = useState(null);

    useEffect(() => {
        if (data == null) {
            fetchData();        
        }
    });

    const fetchData = () => {
        const query =
            "PREFIX kb: <http://www.finfour.net/kb#> " +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
            "SELECT * WHERE { " +
	        "    kb:Token rdfs:subClassOf ?node . " +
            "    ?node owl:onProperty ?property . " +
            "    ?property rdfs:range ?range . " +
            "    FILTER (?range IN (xsd:boolean, xsd:double)) . " +
            "}";

        $.getJSON(repositoryURL, { query: query, infer: false })
            .done(response => {
                console.log("Response", response);
                let rows = response.results.bindings;

                // TODO setData();
            });

        /*
        const axios = require('axios');
        axios
			.get(repositoryURL, {
                params: {
                    data: {
                        'query': query,
                        'infer': false
                    }
                }
            })
			.then(response => {
				console.log('Success', response);
			})
			.catch(error => {
				console.log('Error', error);
			})
            .finally(() => {});
        */
    };

    return (
        <div className="App">
            <header className="App-header">
                TODO
            </header>
        </div>
    );
}

export default App;
