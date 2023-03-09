export const tailoredQueryObjects = {
  1: {
    name: "List employees",
    description: "Gets the names of all employees.",
    query: "PREFIX : <http://dev.de/default#> "
        + "SELECT * WHERE { "
        + "  :DigitalService :hasEmployee ?employee . "
        + "}"
  }
}
