import React, { useEffect, useState, useRef } from 'react';
import './App.css';
import $ from 'jquery';
import { getRandomArraySubset } from './utils';

const repositoryURL = 'http://localhost:7200/repositories/fin4kb';

function App() {

    const data = useRef(null);
    const [currentQuestionObj, setCurrentQuestionObj] = useState(null);

    useEffect(() => {
        if (data.current == null) {
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

            data.current = {
                boolTokenProps: boolTokenProps,
                humanTraits: traits['HumanTrait'],
                tokenTraits: traits['TokenTrait'],
                organizationalTraits: traits['OrganizationalTrait'],
            }

            generateQuestion();
        });
    };

    const concatStrings = arr => {
        switch (arr.length) {
            case 0:
                return '';
            case 1:
                return arr[0];
            default:
                let str = arr[0];
                for (let i = 1; i < arr.length - 1; i ++) {
                    str += ', ' + arr[i];
                }
                return str + ' and ' + arr[arr.length - 1];
        }
    };

    const generateQuestion = () => {
        let boolTokenProps = getRandomArraySubset(data.current.boolTokenProps, 1, data.current.boolTokenProps.length - 1);
        let tokenCreatorTraits = getRandomArraySubset(data.current.humanTraits, 0, 2);
        let claimerTraits = getRandomArraySubset(data.current.humanTraits, 0, 2);
        let tokenTraits = getRandomArraySubset(data.current.tokenTraits, 0, 2);

        let question =
            'A token creator ' +
            (tokenCreatorTraits.length > 0 ? 'who is ' + concatStrings(tokenCreatorTraits) : '') +
            ' makes a new token' +
            (boolTokenProps.length > 0 ? ' with the properties of ' + concatStrings(boolTokenProps) : '') +
            (tokenTraits.length > 0 ? ' and an overall design that is ' + concatStrings(tokenTraits) : '') +
            '.';

        setCurrentQuestionObj({
            question: question,
            values: {
                boolTokenProps: boolTokenProps,
                tokenCreatorTraits: tokenCreatorTraits,
                claimerTraits: claimerTraits,
                tokenTraits: tokenTraits
            }
        });
    };

    return (
        <div className="App">
            <header className="App-header">
                {currentQuestionObj && currentQuestionObj.question}
            </header>
        </div>
    );
}

export default App;
