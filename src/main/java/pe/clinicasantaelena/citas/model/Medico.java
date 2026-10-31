package pe.clinicasantaelena.citas.model;

/**
 * Medico colegiado que atiende en una especialidad.
 */
public class Medico extends Persona {

    private String cmp;
    private boolean activo = true;
    private Long idEspecialidad;
    private String nombreEspecialidad;
    private Long idConsultorio;
    private String codigoConsultorio;
    private Long idUsuario;

    /**
     * Polimorfismo: el medico se presenta con el tratamiento "Dr(a).".
     *
     * @return nombre completo precedido del tratamiento
     */
    @Override
    public String getNombreCompleto() {
        return "Dr(a). " + super.getNombreCompleto();
    }

    public String getCmp() { return cmp; }
    public void setCmp(String cmp) { this.cmp = cmp; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public Long getIdEspecialidad() { return idEspecialidad; }
    public void setIdEspecialidad(Long idEspecialidad) { this.idEspecialidad = idEspecialidad; }
    public String getNombreEspecialidad() { return nombreEspecialidad; }
    public void setNombreEspecialidad(String nombreEspecialidad) { this.nombreEspecialidad = nombreEspecialidad; }
    public Long getIdConsultorio() { return idConsultorio; }
    public void setIdConsultorio(Long idConsultorio) { this.idConsultorio = idConsultorio; }
    public String getCodigoConsultorio() { return codigoConsultorio; }
    public void setCodigoConsultorio(String codigoConsultorio) { this.codigoConsultorio = codigoConsultorio; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
}
