# RF-025 - Consultar página inicial pública

## Identificação

| Campo | Valor |
| --- | --- |
| **ID** | RF-025 |
| **Nome** | Consultar página inicial pública |
| **Prioridade** | Alta |
| **Versão** | 1.0 |
| **Status** | Implementado |

## Descrição

O sistema deve apresentar uma página pública em `/` para visitantes, explicando a proposta do ReSource, as etapas de doação, as opções de participação e as dúvidas iniciais. Uma conta já autenticada que acessar essa rota deve continuar sendo direcionada ao painel de seu perfil.

A página deve manter a identidade visual das áreas existentes, oferecer navegação adaptada a dispositivos móveis e permitir ler seu conteúdo, abrir dúvidas e acessar login e cadastro sem depender de JavaScript. Quando habilitadas, as animações devem respeitar a preferência de redução de movimento.

## Atores

- Visitante.
- Doador ou ONG com sessão existente.

## Entradas e Saídas

### Entradas

- Acesso à rota inicial.
- Seleção de seção, pergunta, login ou cadastro de um dos perfis.

### Saídas

- Apresentação da marca e explicação das etapas de proposta, aceite, entrega e recebimento.
- Ações para cadastro de doador, cadastro institucional e login.
- Informações sobre análise institucional e acompanhamento de propostas, sem métricas ou depoimentos fictícios.
- Respostas às dúvidas sobre itens, conservação, entrega e participação institucional.
- Navegação por teclado, acesso direto ao conteúdo, identificação de seções e menu móvel.
- Redirecionamento de contas autenticadas conforme RN-001, preservando `/login` como rota de autenticação.

## Histórico de Alterações

| Versão | Data | Autor | Descrição |
| --- | --- | --- | --- |
| 1.0 | 2026-10-10 | Hiel Saraiva | Página inicial de apresentação e entradas para os fluxos existentes |
