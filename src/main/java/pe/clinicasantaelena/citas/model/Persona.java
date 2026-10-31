package pe.clinicasantaelena.citas.model;

import org.apache.commons.lang3.StringUtils;

/**
 * Datos comunes de identificacion y contacto de {@link Paciente} y {@link Medico}.
 *
 * <p>Es abstracta porque en el dominio no existe una "persona" generica.
 * Aplica herencia para no repetir atributos (principio DRY).</p>
 */
public abstract class Persona {

    protected Long id;
    protected String nombres;
    protected String apellidos;
    protected String telefono;
    protected String correo;

    /**
     * Nombre para mostrar en pantallas y reportes.
     * Las subclases pueden especializarlo (polimorfismo).
     *
     * @return nombres y apellidos normalizados
     */
    public String getNombreCompleto() {
        return StringUtils.normalizeSpace(StringUtils.defaultString(nombres) + " "
                + StringUtils.defaultString(apellidos));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
}
