package pe.clinicasantaelena.citas.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.clinicasantaelena.citas.dao.EspecialidadDao;
import pe.clinicasantaelena.citas.dao.MedicoDao;
import pe.clinicasantaelena.citas.exception.RecursoNoEncontradoException;
import pe.clinicasantaelena.citas.model.Especialidad;
import pe.clinicasantaelena.citas.model.Medico;
import pe.clinicasantaelena.citas.service.CatalogoService;

import java.util.List;

/** Implementacion de {@link CatalogoService}. */
@Service
@Transactional(readOnly = true)
public class CatalogoServiceImpl implements CatalogoService {

    private final EspecialidadDao especialidadDao;
    private final MedicoDao medicoDao;

    /**
     * @param especialidadDao DAO de especialidades
     * @param medicoDao       DAO de medicos
     */
    public CatalogoServiceImpl(EspecialidadDao especialidadDao, MedicoDao medicoDao) {
        this.especialidadDao = especialidadDao;
        this.medicoDao = medicoDao;
    }

    @Override
    public List<Especialidad> especialidadesActivas() {
        return especialidadDao.listarActivas();
    }

    @Override
    public List<Medico> medicosPorEspecialidad(Long idEspecialidad) {
        return idEspecialidad == null ? List.of() : medicoDao.listarActivosPorEspecialidad(idEspecialidad);
    }

    @Override
    public Medico medicoActivo(Long idMedico) {
        return medicoDao.buscarPorId(idMedico)
                .filter(Medico::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("El médico no existe o no está activo"));
    }
}
