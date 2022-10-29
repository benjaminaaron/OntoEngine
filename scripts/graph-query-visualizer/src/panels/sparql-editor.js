import Yasgui from "@triply/yasgui";

let yasgui, yasqe;

const initSparqlEditor = config => {
    yasgui = new Yasgui(config.div, {}); // TODO config --> much more lightweight
    // make sure only one tab is open
    let tab;
    while (tab = yasgui.getTab()) { tab.close(); }
    yasgui.addTab(true, {});
    yasqe = yasgui.getTab().getYasqe();
};

const setSparqlQuery = query => {
    yasgui.getTab().setQuery(query);
};

const getQuery = () => {
    return yasgui.getTab().getQuery();
};

const onValidSparqlChange = onChange => {
    yasqe.on("change", () => yasqe.queryValid && onChange(getQuery()));
};

export { initSparqlEditor, setSparqlQuery, onValidSparqlChange, getQuery }
