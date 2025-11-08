## 0.0.2-SNAPSHOT

### Breaking changes
- 1 - Refatora a resposta de sucesso dos controllers de autenticação.
- 2 - Atualiza mensagem de erro das rotas que necessitam de token de acesso

Agr todas as requisições seguirão esse padrão:

-- Padrao de sucesso
```json
{
    "status": "success",
    "message": "login realizado com sucesso",
    "data": {
      // Conteudo
    }
}
```
-- Padrao de sucesso com paginação
```json
{
"status": "success",
"message": "Notas fiscais encontradas com sucesso",
"data": [
[]
],
"page": {
"pageNumber": 2,
"pageSize": 1,
"totalElements": 0,
"totalPages": 0,
"last": true
}
}

```
-- Padrão de erro
```json
{
"status": "error",
"type": "defaultError",
"message": "Usuário autenticado não encontrado no banco de dados."
}

```
-- Padrão de erro por campo
```json
{
  "status": "error",
  "type": "fieldError",
  "errorFields": {
    "password": "Passwords don't match",
    "confirmPassword": "Passwords don't match"
  }
}
```

### 2- Atualização do retorno das rotas que necessitam de JWT token

agr essas rotas seguem esse padrão quando fizer uma request sem token:

ANTES:
```text
Token inválido, expirado ou revogado.
```

AGORA:
```json
{
  "status": "error",
  "type": "defaultError",
  "message": "Token inválido, expirado ou revogado."
}
```
