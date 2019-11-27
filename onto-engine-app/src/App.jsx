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
        const boolTokenPropsQuery =
            "PREFIX kb: <http://www.finfour.net/kb#> " +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
            "SELECT * WHERE { " +
            "   ?tokenProp rdfs:subPropertyOf kb:BooleanTokenProperty . " +
            "   FILTER (?tokenProp != kb:BooleanTokenProperty) . " +
            "}";
        const traitsQuery =
            "PREFIX kb: <http://www.finfour.net/kb#> " +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
            "SELECT * WHERE { " +
            "VALUES (?traitType) { " +
            "   (kb:HumanTrait) " +
            "   (kb:TokenTrait) " +
            "   (kb:OrganizationalTrait) " +
            "} " +
            "?trait rdfs:subPropertyOf ?traitType . " +
            "FILTER (?trait != ?traitType) . " +
            "}";

        $.when(
            $.getJSON({url: repositoryURL, data: {query: boolTokenPropsQuery, infer: false}}),
            $.getJSON({url: repositoryURL, data: {query: traitsQuery, infer: false}})
        ).done((boolTokenPropsQueryResponse, traitsQueryResponse) => {
            let boolTokenPropsRows = boolTokenPropsQueryResponse[0].results.bindings;
            let traitsRows = traitsQueryResponse[0].results.bindings;

            let boolTokenProps = [];
            for (let i = 0; i < boolTokenPropsRows.length; ++i) {
                boolTokenProps.push(boolTokenPropsRows[i].tokenProp.value.split('#')[1]);
            }

            let traits = {};
            for (let i = 0; i < traitsRows.length; ++i) {
                let traitType = traitsRows[i].traitType.value.split('#')[1];
                if (!traits[traitType]) {
                    traits[traitType] = [];
                }
                traits[traitType].push(traitsRows[i].trait.value.split('#')[1]);                
            }

            // TODO
        });
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
