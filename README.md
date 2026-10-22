# ThinDesk

Projeto de atendimento técnico com uma API REST para abertura, consulta e resolução de chamados. A API de chamados está integrada à aplicação Spring Boot do ThinDesk.

## Tecnologias

- Java 23
- Spring Boot 3.4.0
- Spring Web e Spring Security
- Spring Data MongoDB
- MongoDB
- Thymeleaf para as páginas existentes do sistema
- Maven Wrapper

## Executar localmente

Pré-requisitos: JDK 23 e uma instância MongoDB acessível.

1. Inicie o MongoDB local na porta padrão `27017`.
2. No terminal, na raiz do projeto, execute:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

Por padrão, a aplicação usa `mongodb://localhost:27017/thindesk`. Para configurar outra URI, defina a variável de ambiente `MONGODB_URI` antes de iniciar a aplicação.

A aplicação fica disponível em `http://localhost:8080`. A tela de login fica em `http://localhost:8080/login`.

### Acesso de demonstração

O `application.properties` define o usuário de demonstração `admin` e a senha `thindesk`. Troque essas credenciais antes de usar o projeto em um ambiente compartilhado ou de produção.

## API de chamados

As rotas da API exigem autenticação pela sessão do Spring Security. Ao abrir ou resolver um chamado, o sistema usa o usuário autenticado para preencher o solicitante ou o técnico.

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/api/chamados` | Abre um chamado |
| `GET` | `/api/chamados/usuario/{id}` | Lista chamados do usuário |
| `GET` | `/api/chamados/{id}` | Consulta um chamado pelo ID |
| `PUT` | `/api/chamados/{id}/resolver` | Registra a solução e resolve um chamado |

### Abrir chamado

`POST /api/chamados`

```json
{
  "titulo": "Computador sem acesso à rede",
  "descricao": "A estação não consegue acessar a rede."
}
```

O servidor registra o solicitante autenticado, a data de abertura e o status inicial `ABERTO`. Em caso de sucesso, retorna `201 Created`.

### Listar chamados do usuário

`GET /api/chamados/usuario/admin`

No código atual, o valor `{id}` dessa rota corresponde ao nome do usuário salvo no chamado (por exemplo, `admin`).

### Consultar chamado

`GET /api/chamados/{id}`

Retorna os dados do chamado, incluindo status, solicitante, datas, solução e técnico, quando informados.

### Resolver chamado

`PUT /api/chamados/{id}/resolver`

```json
{
  "solucao": "Reiniciada a interface de rede e validado o acesso."
}
```

Somente chamados `ABERTO` podem ser resolvidos. A resolução registra o usuário autenticado como técnico, a solução, a data e o status `RESOLVIDO`. Uma nova tentativa de resolver o mesmo chamado retorna `409 Conflict`.

### Validações e respostas comuns

- Campos obrigatórios vazios: `400 Bad Request`.
- Requisição sem autenticação: `401 Unauthorized` nas rotas da API.
- Chamado inexistente: `404 Not Found`.
- Tentativa de resolver chamado que não está aberto: `409 Conflict`.

Os chamados são persistidos na coleção `chamados` do banco `thindesk`.

## Testar com Postman

1. Inicie o MongoDB e a aplicação.
2. No Postman Desktop, importe `postman/ThinDesk-Chamados.postman_collection.json`.
3. Execute a coleção na ordem apresentada. A primeira requisição autentica e as seguintes usam a sessão.

A coleção percorre o login, a abertura, as consultas, a resolução, uma consulta posterior para confirmar os dados persistidos e uma tentativa de resolução duplicada. O `409 Conflict` da última requisição é esperado e confirma a regra de bloqueio.
