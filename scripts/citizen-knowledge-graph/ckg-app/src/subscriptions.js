let newSubClickCount = 0;

document.getElementById("new-sub").addEventListener("click", function(e) {
  e.preventDefault();
  if (newSubClickCount ++ === 0) {
    let checkboxes = document.getElementsByClassName("inq-cb");
    for (let i = 0; i < checkboxes.length; i++) {
      checkboxes[i].style.display = "block";
    }
    document.getElementById("new-sub-name").style.display = "block";
  } else {
    messageToMain('new-subscription-shortcut');
  }
});
