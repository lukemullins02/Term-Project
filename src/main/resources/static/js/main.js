document.addEventListener("DOMContentLoaded", function () {
  var submittables = document.getElementsByClassName("submittable");

  for (var submittable of submittables) {
    submittable.addEventListener("keydown", function (e) {
      if (e.key === "Enter" && !e.shiftKey) {
        e.preventDefault();
        this.form.submit();
        console.log(this.form + " was submitted.");
      }
    });
  }
});
