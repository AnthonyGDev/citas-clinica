# Sistema Web de Gestión de Citas Médicas — Clínica Santa Elena

Proyecto del curso **Integrador I: Sistemas – Software** (UTP, 2026).
Autor: Huaraca Huarca Anthony Gean Franco — U21309999.
Versión **0.3.0 — Avance 3** (construcción de la arquitectura inicial y primeras funcionalidades).

## Tecnologías
| Capa | Herramienta |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.3 (Web MVC, Security, Validation, JDBC) |
| Vistas | Thymeleaf + CSS propio (guía de estilo del Avance 2) |
| Base de datos | H2 en memoria (perfil `dev`) · MySQL 8 (perfil `prod`) |
| Librerías de apoyo | Google Guava · Apache Commons Lang 3 · Apache POI · Logback |
| Pruebas | JUnit 5 (TDD) + JaCoCo |
| Versiones | Git + GitHub |

## Cómo ejecutarlo
Requisitos: JDK 17 o superior y Maven 3.9 (o el IDE IntelliJ / NetBeans / VS Code).

```bash
mvn spring-boot:run          # abre http://localhost:8080
mvn test                     # ejecuta las pruebas unitarias
mvn javadoc:javadoc          # documentación en target/site/apidocs
```

Usuarios de demostración (contraseña de todos: `Clinica2026`):

| Rol | Correo |
|---|---|
| Paciente | maria.torres@correo.pe |
| Médico | lramos@clinicasantaelena.pe |
| Administrador | admin@clinicasantaelena.pe |

## Arquitectura (MVC en capas)
```
pe.clinicasantaelena.citas
├── controller   (C) recibe la petición HTTP y elige la vista
├── service      reglas de negocio (interfaces) · service.impl (implementaciones)
├── dao          patrón DAO: interfaces · dao.jdbc: implementación con JdbcTemplate
├── model        (M) entidades del dominio: Persona, Paciente, Medico, Cita, Horario...
├── dto          objetos de formulario y de reporte
├── security     Spring Security: login, roles, BCrypt, bloqueo por intentos
├── report       exportación de reportes (Apache POI)
├── exception    excepciones de negocio y manejador global
├── util         utilidades (enmascarado de datos, política de contraseñas, código de cita)
└── config       beans generales y datos de demostración
resources/templates  (V) vistas Thymeleaf
```

## Funcionalidades del Avance 3
| RF | Funcionalidad | Estado |
|---|---|---|
| RF01 | Registro e inicio de sesión (bloqueo tras 3 intentos) | ✔ |
| RF02 | Consulta de especialidades y médicos | ✔ |
| RF03 | Disponibilidad en calendario | ✔ |
| RF04 | Registro de citas (web) | ✔ |
| RF05 | Cancelación (hasta 24 h antes) | ✔ (reprogramar: entrega final) |
| RF07 | Control de duplicidad (Java + UNIQUE en BD) | ✔ |
| RF08 | Agenda del médico | ✔ |
| RF10 | Reporte R01 exportable a Excel | ✔ parcial (R02, R03 y PDF: entrega final) |
| RF06 | Notificaciones por correo | Registro en log (SMTP: entrega final) |
| RF09 | Mantenimiento de catálogos | Entrega final |

## Flujo de trabajo con Git
- `main`: versión estable (se etiqueta cada avance: `v0.1-avance1`, `v0.2-avance2`, `v0.3-avance3`).
- `develop`: integración.
- `feature/<modulo>`: una rama por funcionalidad, se integra a `develop` con *merge*.
- Mensajes de commit en formato `tipo: descripción` (`feat`, `fix`, `test`, `docs`, `refactor`).
