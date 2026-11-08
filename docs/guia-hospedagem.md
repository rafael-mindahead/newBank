# Como Hospedar o FinanceHub (Guia Prático Passo a Passo)

O FinanceHub é composto por duas partes:
1. **API / Backend Java 21 (Servidor HTTP nativo + MySQL / Arquivo Persistente)**: É a parte que você hospeda na nuvem (Azure, Render, Railway, etc).
2. **Interface Desktop Java Swing**: Roda no computador do usuário (ou professor/avaliador) e se conecta à base local ou à API remota.

---

## Opção 1: Hospedagem Gratuita na Render (Mais Fácil e Rápida)

A **Render** permite subir aplicações Java/Docker gratuitamente diretamente do GitHub.

### Passo 1: Subir seu código para o GitHub
Se você ainda não colocou o projeto no seu GitHub, no terminal execute:
```bash
git init
git add .
git commit -m "feat: FinanceHub backend Java e interface Swing"
git branch -M main
git remote add origin https://github.com/SEU_USUARIO/FinanceHub.git
git push -u origin main
```

### Passo 2: Criar a aplicação na Render
1. Acesse [render.com](https://render.com) e crie uma conta (pode entrar com o próprio GitHub).
2. Clique em **"New +"** no canto superior direito e selecione **"Web Service"**.
3. Conecte o repositório do seu GitHub onde você subiu o **FinanceHub**.
4. Configure as opções:
   - **Name**: `financehub-api`
   - **Language**: `Docker` *(o Render vai detectar o `Dockerfile` que já deixamos configurado na raiz!)*
   - **Instance Type**: `Free`
5. Clique em **"Create Web Service"**.
6. Em 2 a 3 minutos o Render vai construir o container Java e te dará uma URL pública (exemplo: `https://financehub-api.onrender.com`).
7. Teste no navegador acessando:
   `https://financehub-api.onrender.com/api/v1/health`
   `https://financehub-api.onrender.com/api/v1/relatorios`

---

## Opção 2: Hospedagem no Microsoft Azure (Requisito do PjBL)

Conforme documentado em `docs/arquitetura.md` e `infra/azure/README.md`, o PjBL recomenda usar créditos de estudante do **GitHub Student Pack** no **Microsoft Azure**.

### Pelo Azure App Service (Web App):
1. Acesse o [Portal do Azure](https://portal.azure.com/).
2. Na barra de pesquisa, procure por **App Services** e clique em **Criar** -> **Aplicativo Web**.
3. Preencha as configurações básicas:
   - **Assinatura**: Sua assinatura do Azure / Azure for Students.
   - **Grupo de Recursos**: Crie um novo (ex: `rg-financehub`).
   - **Nome**: Escolha um nome único (ex: `financehub-backend`).
   - **Publicar**: `Código` ou `Contêiner Docker`.
   - **Pilha de Tempo de Execução**: `Java 21` (com Servidor Web incorporado / Java SE).
   - **Plano de Hospedagem**: Selecione o plano gratuito **F1 (Free)**.
4. Na aba **Implantação (Deployment)**:
   - Ative a implantação contínua e conecte sua conta do GitHub apontando para o seu repositório.
5. Clique em **Revisar + Criar**.
6. Quando o deploy terminar, sua API estará online no endereço `https://financehub-backend.azurewebsites.net/api/v1/health`.

### Como adicionar o banco MySQL no Azure (Opcional):
1. No Portal do Azure, crie um **Servidor Flexível do Banco de Dados do Azure para MySQL**.
2. Pegue o Host, Usuário e Senha e configure nas variáveis de ambiente da aplicação (`MYSQL_HOST`, `MYSQL_USER`, `MYSQL_PASSWORD`, `MYSQL_DATABASE`).
3. Execute o script `infra/mysql/init/01-schema.sql` para criar as tabelas.

---

## Como testar e gerar o arquivo JAR executável para entrega

Para gerar o arquivo `.jar` completo que qualquer pessoa pode executar com dois cliques:

1. No terminal, entre na pasta `backend-java`:
   ```powershell
   cd backend-java
   mvn clean package -DskipTests
   ```
2. O Maven vai gerar o arquivo na pasta `target/financehub-core-0.1.0-SNAPSHOT.jar`.
3. Para rodar:
   ```bash
   java -jar target/financehub-core-0.1.0-SNAPSHOT.jar
   ```
   No computador com tela, ele abrirá a **janela gráfica Java Swing**. Em um servidor web/cloud (onde não há monitor), ele atua automaticamente como o **servidor de API REST HTTP**.

