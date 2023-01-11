// adapted from https://www.w3schools.com/howto/howto_js_autocomplete.asp

let dataPoints = ["Krankenversicherung", "Autoversicherung", "Lebensversicherung",
  "Berufsunfähigkeitsversicherung", "Familienstand", "Steuerklasse", "Wohnort",
  "Vorname", "Vermögen", "Einkommen", "Ehepartner", "Hobbys", "Haustiere",
    "Vereinsmitgliedschaft"];

function autocomplete(input, arr) {
  input.addEventListener("input", function(e) {
    let a, b, i, val = this.value;
    closeAllLists();
    if (!val) { return false;}
    a = document.createElement("div");
    a.setAttribute("id", this.id + "autocomplete-list");
    a.setAttribute("class", "autocomplete-items");
    this.parentNode.appendChild(a);
    for (i = 0; i < arr.length; i++) {
      if (arr[i].toUpperCase().includes(val.toUpperCase())) {
        b = document.createElement("div");
        let startSubstr = arr[i].toUpperCase().indexOf(val.toUpperCase());
        let endSubstr = startSubstr + val.length;
        b.innerHTML += arr[i].substring(0, startSubstr);
        b.innerHTML += "<strong>" + arr[i].substring(startSubstr, endSubstr)  + "</strong>";
        b.innerHTML += arr[i].substring(endSubstr, arr[i].length);
        b.innerHTML += "<input type='hidden' value='" + arr[i] + "'>";
        b.addEventListener("click", function(e) {
          input.value = this.getElementsByTagName("input")[0].value;
          closeAllLists();
        });
        a.appendChild(b);
      }
    }
  });
  function closeAllLists(element) {
    let x = document.getElementsByClassName("autocomplete-items");
    for (let i = 0; i < x.length; i++) {
      if (element !== x[i] && element !== input) {
        x[i].parentNode.removeChild(x[i]);
      }
    }
  }
}

autocomplete(document.getElementById("autocomplete-input"), dataPoints);
