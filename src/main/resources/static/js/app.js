// Envio automatico de los pasos de la reserva al cambiar una seleccion.
// Se usa un archivo externo (no atributos onchange) para cumplir la politica CSP sin scripts en linea.
document.addEventListener('DOMContentLoaded', function () {
  document.querySelectorAll('[data-autosubmit]').forEach(function (el) {
    el.addEventListener('change', function () { el.form.submit(); });
  });
  document.querySelectorAll('form[data-confirmar]').forEach(function (form) {
    form.addEventListener('submit', function (e) {
      if (!window.confirm(form.getAttribute('data-confirmar'))) { e.preventDefault(); }
    });
  });
});
