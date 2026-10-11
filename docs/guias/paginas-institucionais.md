# Páginas institucionais

**Autor:** Hiel Saraiva  
**Atualizado em:** 10 de outubro de 2026

## Acesso

| Rota | Conteúdo |
| --- | --- |
| `/sustentabilidade` | Compromissos com reaproveitamento, direcionamento, cuidado e acompanhamento das doações. |
| `/politica-de-privacidade` | Dados utilizados, finalidades, compartilhamento, proteção, armazenamento, direitos e contato. |
| `/sobre` | Apresentação pública da plataforma, disponível também para contas autenticadas. |

As rotas são públicas no Spring Security. A raiz `/` continua direcionando contas autenticadas ao painel. Os links do header das páginas institucionais apontam para os tópicos de `/sobre`, evitando esse redirecionamento.

## Contato de privacidade

O endereço é configurado pela variável abaixo, tanto no `.env` local quanto no ambiente de produção:

```properties
PRIVACY_CONTACT_EMAIL=resource.company.rc@gmail.com
```

Esse valor também é o padrão em `application.properties`. O endereço deve receber solicitações; ele é independente do remetente SMTP e não pode ser uma caixa “não responda”. Alterações de configuração exigem reiniciar a aplicação. A seção de contato apresenta somente o e-mail de atendimento, sem nome ou perfil pessoal.

O link de contato abre o aplicativo de e-mail do visitante. Ele não envia mensagens pelo backend. Solicitações de acesso, correção e exclusão são atendidas pelo responsável; não foi criada uma funcionalidade automática de exclusão de contas.

## Conteúdo e manutenção

- Templates: `templates/public/sustainability.html` e `templates/public/privacy.html`.
- Header, rodapé e links institucionais: fragmentos compartilhados em `templates/fragments/layout` e `templates/fragments/public`.
- Visual comum: `static/css/public/common.css`; estrutura editorial compartilhada: `static/css/public/information.css`; detalhes de sustentabilidade: `static/css/public/sustainability.css`.
- Animações de entrada, navegação por âncoras e indicação do tópico atual: `static/js/public-page.js`.

O login, o cadastro e a recuperação de senha reutilizam os links institucionais em desktop e mobile. O aviso das telas de doação também aponta para sustentabilidade.

A política descreve a implementação atual: cadastro local e Google, documentos privados das instituições, consulta de CNPJ, cookies de sessão, carregamento de fontes e envio de mensagens por provedores externos. Seus direitos têm referências à [LGPD](https://www.planalto.gov.br/ccivil_03/_ato2015-2018/2018/lei/l13709compilado.htm) e às [orientações da ANPD](https://www.gov.br/anpd/pt-br/assuntos/titular-de-dados/direito-dos-titulares).

Ao mudar coleta de dados, provedores, retenção ou formas de atendimento, revise o texto e a data da versão publicada. A página de sustentabilidade não apresenta estimativas de carbono, resíduos ou certificados: o sistema registra o fluxo das doações, sem medir esses indicadores.

As páginas funcionam sem JavaScript, respeitam a preferência por movimento reduzido e possuem estilos para impressão. A validação visual deve conferir leitura e ausência de rolagem horizontal em telas pequenas, links públicos sem autenticação, navegação por teclado e alinhamento das âncoras abaixo do header.

## Estrutura editorial de sustentabilidade

O acesso institucional está no rodapé da página inicial, sem ocupar o menu principal. A página apresenta o compromisso do projeto, um índice de quatro temas e capítulos de leitura, com convite discreto ao final. As referências de organização editorial são a [agenda circular da IKEA](https://www.ikea.com/global/en/our-business/sustainability/our-circular-agenda/) e os [compromissos da Patagonia](https://www.patagonia.com/our-footprint/), adaptadas à identidade visual do ReSource.
