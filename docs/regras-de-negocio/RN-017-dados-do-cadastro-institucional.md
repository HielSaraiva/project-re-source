# RN-017 - Dados do cadastro institucional

## Identificação

| Campo | Valor |
|-------|-------|
| **ID** | RN-017 |
| **Nome** | Dados do cadastro institucional |
| **Versão** | 1.0 |
| **Status** | Ativa |

## Contexto

Cadastro público de instituições que desejam receber recursos.

## Regra

- CNPJ e CPF aceitam valores completos com ou sem máscara; são persistidos sem pontuação. Letras do CNPJ são convertidas para maiúsculas. Os dígitos verificadores são conferidos pelo servidor; sequências numéricas repetidas são rejeitadas.
- O CNPJ tem 12 caracteres numéricos ou alfanuméricos e dois dígitos verificadores. O CPF tem 11 dígitos.
- A razão social vem da BrasilAPI. O cadastro exige que o CNPJ retornado corresponda ao solicitado e tenha situação cadastral ativa (código 2). Dados incompletos ou falha na consulta impedem o cadastro.
- CNPJ e CPF do representante são únicos entre instituições. O e-mail, normalizado em minúsculas e sem espaços externos, é único entre usuários e instituições. Criações locais e Google compartilham bloqueio transacional por e-mail.
- Nome do representante, e-mail, senha, confirmação e os dois anexos são obrigatórios. As credenciais seguem os mesmos limites e proteção de RN-016.
- A situação institucional é definida pelo servidor como `pending_approval`. Não é possível atribuir aprovação ou permissões pelo formulário.
- Cada documento deve ter conteúdo não vazio, até 5 MB, com assinatura de PDF ou JPG. PDFs devem conter marcador de encerramento; JPGs devem possuir dimensões válidas de até 25 milhões de pixels. Essa conferência de formato não substitui a análise administrativa do conteúdo.
- Arquivos são gravados em diretório privado com nomes aleatórios. Nome original saneado, tipo, tamanho e chave de armazenamento são registrados em `institution_documents`, reutilizando a estrutura existente.
- Os dois documentos começam como `pending`. A conta e seus metadados são criados na mesma transação. Arquivos recém-gravados são removidos se essa transação não confirmar.
- Após erro, os dados textuais podem ser preservados; senhas e anexos devem ser informados novamente. Requisições que excedam o limite do parser voltam a um formulário vazio com orientação.
- O sucesso redireciona ao login institucional e não cria sessão autenticada. O acesso ao painel exige aprovação conforme RN-001.

## Histórico de Alterações

| Versão | Data | Autor | Descrição |
|--------|------|-------|-----------|
| 1.0 | 2026-10-09 | Codex | Dados oficiais, unicidade, credenciais e documentos institucionais |
