package pe.clinicasantaelena.citas.service;

import pe.clinicasantaelena.citas.dto.RegistroPacienteForm;
import pe.clinicasantaelena.citas.model.Paciente;

/** Casos de uso de cuentas de usuario (RF01, RN04). */
public interface UsuarioService {

    /**
     * Registra un paciente nuevo con su cuenta de acceso.
     *
     * @param form datos del formulario P02
     * @return el paciente creado
     */
    Paciente registrarPaciente(RegistroPacienteForm form);

    /** @param correo correo con el que se intento ingresar */
    void registrarIntentoFallido(String correo);

    /** @param correo correo con el que se ingreso correctamente */
    void registrarIngresoExitoso(String correo);
}
