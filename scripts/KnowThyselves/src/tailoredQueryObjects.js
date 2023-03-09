export const tailoredQueryObjects = {
  1: {
    name: "List employees",
    description: "Gets the names of all employees.",
    query: "PREFIX : <http://dev.de/default#> "
        + "SELECT * WHERE { "
        + "  :DigitalService :hasEmployee ?employee . "
        + "}"
  },
  2: {
    name: "Ministries",
    description: "Ministries we worked with.",
    query: "PREFIX : <http://dev.de/default#> "
        + "SELECT * WHERE { "
        + "  ?project :hasProjectPartner ?ministry . "
        + "  ?ministry :hasLogo ?logo . "
        + "}"
  },
}
