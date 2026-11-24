package com.esportsarena.api.service;

import com.esportsarena.api.dto.JuegoDto;
import com.esportsarena.api.exception.BusinessException;
import com.esportsarena.api.exception.ResourceNotFoundException;
import com.esportsarena.api.model.Juego;
import com.esportsarena.api.repository.EquipoRepository;
import com.esportsarena.api.repository.JuegoRepository;
import com.esportsarena.api.repository.TorneoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JuegoService {

    private final JuegoRepository juegoRepository;
    private final TorneoRepository torneoRepository;
    private final EquipoRepository equipoRepository;

    public JuegoService(JuegoRepository juegoRepository, TorneoRepository torneoRepository,
                        EquipoRepository equipoRepository) {
        this.juegoRepository = juegoRepository;
        this.torneoRepository = torneoRepository;
        this.equipoRepository = equipoRepository;
    }

    private static JuegoDto aDto(Juego j) {
        return new JuegoDto(j.getId(), j.getNombre(), j.getMinIntegrantes());
    }

    @Transactional(readOnly = true)
    public List<JuegoDto> listar() {
        return juegoRepository.findAll().stream().map(JuegoService::aDto).toList();
    }

    @Transactional(readOnly = true)
    public JuegoDto obtener(Long id) {
        return aDto(buscar(id));
    }

    @Transactional
    public JuegoDto crear(JuegoDto dto) {
        if (juegoRepository.existsByNombreIgnoreCase(dto.nombre().trim())) {
            throw new BusinessException("Ya existe un juego llamado " + dto.nombre());
        }
        return aDto(juegoRepository.save(new Juego(dto.nombre().trim(), dto.minIntegrantes())));
    }

    @Transactional
    public JuegoDto actualizar(Long id, JuegoDto dto) {
        Juego j = buscar(id);
        if (!j.getNombre().equalsIgnoreCase(dto.nombre().trim())
                && juegoRepository.existsByNombreIgnoreCase(dto.nombre().trim())) {
            throw new BusinessException("Ya existe un juego llamado " + dto.nombre());
        }
        j.setNombre(dto.nombre().trim());
        j.setMinIntegrantes(dto.minIntegrantes());
        return aDto(juegoRepository.save(j));
    }

    @Transactional
    public void eliminar(Long id) {
        Juego j = buscar(id);
        if (torneoRepository.existsByJuegoId(id) || equipoRepository.existsByJuegoId(id)) {
            throw new BusinessException("No se puede eliminar un juego que tiene torneos o equipos asociados");
        }
        juegoRepository.delete(j);
    }

    private Juego buscar(Long id) {
        return juegoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Juego", id));
    }
}
