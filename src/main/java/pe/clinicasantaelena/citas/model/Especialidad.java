package pe.clinicasantaelena.citas.model;

/** Especialidad medica ofrecida por la clinica (catalogo). */
public class Especialidad {

    private Long id;
    private String nombre;
    private String descripcion;
    private boolean activo = true;

    public Especialidad() { }

    public Especialidad(Long id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
