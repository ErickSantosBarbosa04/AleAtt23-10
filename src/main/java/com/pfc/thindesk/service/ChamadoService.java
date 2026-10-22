package com.pfc.thindesk.service;

import com.pfc.thindesk.dto.AbrirChamadoRequest;
import com.pfc.thindesk.dto.ResolucaoChamadoRequest;
import com.pfc.thindesk.entity.Chamado;
import com.pfc.thindesk.repository.ChamadoRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ChamadoService {

    private static final String STATUS_ABERTO = "ABERTO";
    private static final String STATUS_RESOLVIDO = "RESOLVIDO";

    private final ChamadoRepository chamadoRepository;
    private final MongoTemplate mongoTemplate;

    public ChamadoService(ChamadoRepository chamadoRepository, MongoTemplate mongoTemplate) {
        this.chamadoRepository = chamadoRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public List<Chamado> listarChamados() {
        return chamadoRepository.findAll();
    }

    public Chamado abrirChamado(AbrirChamadoRequest request, String solicitante) {
        if (request == null || request.titulo() == null || request.titulo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O título é obrigatório.");
        }
        if (request.descricao() == null || request.descricao().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A descrição é obrigatória.");
        }

        Chamado chamado = new Chamado();
        chamado.setTitulo(request.titulo().trim());
        chamado.setDescricao(request.descricao().trim());
        chamado.setUsuario(solicitante);
        chamado.setStatus(STATUS_ABERTO);
        chamado.setDataAbertura(Instant.now());
        return chamadoRepository.save(chamado);
    }

    public List<Chamado> listarChamadosDoUsuario(String usuario) {
        return chamadoRepository.findByUsuario(usuario);
    }

    public Chamado consultarChamado(String id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chamado não encontrado."));
    }

    public Chamado resolverChamado(String id, ResolucaoChamadoRequest request, String tecnico) {
        if (request == null || request.solucao() == null || request.solucao().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A solução aplicada é obrigatória.");
        }

        Instant dataResolucao = Instant.now();
        Query chamadoAberto = Query.query(Criteria.where("id").is(id).and("status").is(STATUS_ABERTO));
        Update resolucao = new Update()
                .set("status", STATUS_RESOLVIDO)
                .set("solucao", request.solucao().trim())
                .set("tecnico", tecnico)
                .set("dataResolucao", dataResolucao);

        Chamado chamado = mongoTemplate.findAndModify(
                chamadoAberto,
                resolucao,
                FindAndModifyOptions.options().returnNew(true),
                Chamado.class);

        if (chamado != null) {
            return chamado;
        }

        if (chamadoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Somente chamados ABERTOS podem ser resolvidos.");
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Chamado não encontrado.");
    }
}
