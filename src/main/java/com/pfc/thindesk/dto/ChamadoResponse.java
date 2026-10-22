package com.pfc.thindesk.dto;

import java.time.Instant;

public record ChamadoResponse(
        String id,
        String titulo,
        String descricao,
        String status,
        String solicitante,
        Instant dataAbertura,
        String solucao,
        String tecnico,
        Instant dataResolucao) {
}
