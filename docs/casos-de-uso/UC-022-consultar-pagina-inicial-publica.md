# UC-022 - Consultar página inicial pública

## Identificação

| Campo | Valor |
| --- | --- |
| **ID** | UC-022 |
| **Nome** | Consultar página inicial pública |
| **Ator(es)** | Visitante, doador, ONG |
| **Versão** | 1.0 |
| **Status** | Ativo |

## Descrição

Conhecer o ReSource e escolher como participar, conforme RF-025.

## Pré-condições

- A aplicação está disponível.
- Para o fluxo principal, o visitante não possui sessão autenticada.

## Fluxo Principal

1. O visitante acessa `/`.
2. O sistema apresenta a marca, sua proposta e ações para doar ou cadastrar uma instituição.
3. O visitante consulta as etapas, os perfis de participação e as dúvidas frequentes.
4. Seleciona a opção correspondente ao seu perfil.
5. O sistema abre o cadastro de doador (UC-016) ou de instituição (UC-017).

## Fluxos Alternativos

### FA-01: Conta já existente

1. O visitante seleciona “Entrar” no header, rodapé ou chamada final.
2. O sistema abre `/login` e inicia UC-001.

### FA-02: Sessão autenticada

1. O doador ou a ONG acessa `/` com sessão autenticada.
2. O sistema redireciona diretamente ao painel do perfil, conforme RN-001.

### FA-03: Navegação móvel ou sem JavaScript

1. Em dispositivos móveis, o visitante abre o menu e seleciona a seção desejada.
2. Sem JavaScript, os links de navegação permanecem disponíveis e as perguntas utilizam controles nativos de expansão.
3. O acesso ao login e aos cadastros permanece disponível.

## Fluxos de Exceção

### FE-01: Animações indisponíveis

1. JavaScript está indisponível ou o visitante prefere redução de movimento.
2. O conteúdo permanece acessível e as ações funcionam sem exigir animações.

## Pós-condições

- O visitante conheceu a proposta e pode iniciar os fluxos já implementados.
- A consulta pública não cria conta, proposta ou sessão autenticada.

## Histórico de Alterações

| Versão | Data | Autor | Descrição |
| --- | --- | --- | --- |
| 1.0 | 2026-10-10 | Hiel Saraiva | Navegação pública e preservação do direcionamento ao painel |
