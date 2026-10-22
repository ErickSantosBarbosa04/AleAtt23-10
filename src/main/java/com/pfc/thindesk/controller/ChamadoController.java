package com.pfc.thindesk.controller;

import com.pfc.thindesk.dto.AbrirChamadoRequest;
import com.pfc.thindesk.dto.ChamadoResponse;
import com.pfc.thindesk.dto.ResolucaoChamadoRequest;
import com.pfc.thindesk.entity.Chamado;
import com.pfc.thindesk.service.ChamadoService;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/chamados")
public class ChamadoController {

    private final ChamadoService chamadoService;

    public ChamadoController(ChamadoService chamadoService) {
        this.chamadoService = chamadoService;
    }

    @PostMapping
    public ResponseEntity<ChamadoResponse> abrirChamado(
            @RequestBody AbrirChamadoRequest request,
            Authentication authentication) {
        Chamado chamado = chamadoService.abrirChamado(request, authentication.getName());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(chamado.getId())
                .toUri();
        return ResponseEntity.created(location).body(toResponse(chamado));
    }

    @GetMapping("/usuario/{id}")
    public List<ChamadoResponse> listarChamadosDoUsuario(@PathVariable("id") String id) {
        return chamadoService.listarChamadosDoUsuario(id).stream()
                .map(ChamadoController::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ChamadoResponse consultarChamado(@PathVariable("id") String id) {
        return toResponse(chamadoService.consultarChamado(id));
    }

    @PutMapping("/{id}/resolver")
    public ChamadoResponse resolverChamado(
            @PathVariable("id") String id,
            @RequestBody ResolucaoChamadoRequest request,
            Authentication authentication) {
        return toResponse(chamadoService.resolverChamado(id, request, authentication.getName()));
    }

    private static ChamadoResponse toResponse(Chamado chamado) {
        return new ChamadoResponse(
                chamado.getId(),
                chamado.getTitulo(),
                chamado.getDescricao(),
                chamado.getStatus(),
                chamado.getUsuario(),
                chamado.getDataAbertura(),
                chamado.getSolucao(),
                chamado.getTecnico(),
                chamado.getDataResolucao());
    }
}
