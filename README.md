mysql -u root -p < "database\schema.sql"# Sistema de Gestao para Empresas de Abastecimento de Agua

Esqueleto inicial de uma aplicacao web Java para centralizar clientes, consumo, facturacao e pagamentos.

## Arquitectura

Monolito modular organizado por responsabilidades:

- `presentation`: API REST, DTOs, validacao de pedidos e tratamento de erros.
- `application`: casos de uso e transaccoes.
- `domain`: entidades, contratos de repositorio e regras/excepcoes do negocio.
- `infrastructure`: adaptadores de persistencia com Spring Data JPA e Hibernate.

O fluxo actual de registo de cliente percorre API REST -> servico -> contrato de repositorio -> adaptador JPA -> base de dados. A interface `ClienteRepository` separa o dominio da tecnologia de persistencia. Os proximos modulos podem seguir o mesmo padrao para leituras de consumo, facturas, pagamentos e utilizadores.

## Requisitos

- JDK 21 ou superior.
- MySQL 8.0.16 ou superior, iniciado localmente ou acessivel pela rede.
- Acesso a internet na primeira execucao para descarregar Maven e dependencias.

O Maven Wrapper esta incluido. No Windows, a partir da pasta do projecto:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

O primeiro comando descarrega localmente (para a cache do Maven) as dependencias definidas em `pom.xml` e executa os testes. Os testes usam H2 isoladamente; a aplicacao usa MySQL por omissao.

## Configurar MySQL

O esquema inicial completo esta em [`database/schema.sql`](./database/schema.sql). O ficheiro cria a base de dados `abastecimento` e as tabelas de clientes, utilizadores e perfis, ligacoes, contadores, leituras, consumos, tarifas, facturas e pagamentos.

Ao iniciar a aplicacao com a configuracao MySQL padrao, o Spring Boot executa [`src/main/resources/data.sql`](./src/main/resources/data.sql) e insere os dados ficticios de apresentacao. Importe primeiro `database/schema.sql`; o Hibernate valida as tabelas e o ficheiro de dados nao cria o esquema. Os `INSERT IGNORE` e as chaves unicas permitem voltar a iniciar sem duplicar os registos de exemplo. Os perfis de teste H2 e PostgreSQL nao executam esse ficheiro.

Todos os utilizadores de demonstracao usam a palavra-passe `password` (hash BCrypt de exemplo). Sao contas exclusivamente ficticias para apresentacao local; altere/remova esses dados antes de qualquer utilizacao real e nunca reutilize essa palavra-passe.

Se a base `abastecimento` ja existia antes da associacao entre contas e clientes, aplique a migracao [`database/migrations/V2__cliente_utilizadores.sql`](./database/migrations/V2__cliente_utilizadores.sql) uma vez, com uma conta administradora, antes de iniciar a aplicacao actualizada. No terminal, a partir da pasta do projecto:

```cmd
mysql -u root -p abastecimento < "database\migrations\V2__cliente_utilizadores.sql"
```

Uma base nova ja recebe esta tabela pelo `database/schema.sql`; nao e necessario executar a migracao novamente.

### Importar pelo MySQL Workbench

1. Abra o MySQL Workbench e ligue-se ao seu servidor MySQL local.
2. No menu **File > Open SQL Script**, abra `database/schema.sql` dentro da pasta do projecto.
3. Execute o script pelo botao do raio (**Execute**). O script cria a base `abastecimento` se ainda nao existir e selecciona-a.
4. No painel **Schemas**, actualize a lista e confirme que `abastecimento` contem as novas tabelas.

### Importar pelo terminal

Abra o **Prompt de Comando** (cmd.exe) na pasta do projecto e execute:

```cmd
mysql -u root -p < "database\schema.sql"
```

Introduza a palavra-passe do MySQL quando for solicitada. Se `mysql` nao for reconhecido, use o caminho completo para `mysql.exe`, por exemplo:

```cmd
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < "E:\Dev Projects\pfunaMati\database\schema.sql"
```

O caminho de instalacao do MySQL pode variar. Este script prepara a estrutura; nao cria utilizadores MySQL nem define palavras-passe. E necessario MySQL 8.0.16 ou superior para aplicar as restricoes `CHECK` do esquema.

Depois de importar o esquema, ligue-se ao servidor como administrador no Workbench ou no cliente `mysql` e crie uma conta separada para a aplicacao:

```sql
CREATE USER 'abastecimento_app'@'localhost'
    IDENTIFIED BY 'substitua_por_uma_senha_forte';

GRANT SELECT, INSERT, UPDATE, DELETE
    ON abastecimento.*
    TO 'abastecimento_app'@'localhost';
```

Escolha a sua propria palavra-passe e nao a partilhe. Se este utilizador ja existir, use `ALTER USER 'abastecimento_app'@'localhost' IDENTIFIED BY 'uma_nova_senha_forte';` para actualizar a palavra-passe. A aplicacao so precisa de acesso aos dados; a conta administradora importa/cria o esquema.

Configure as credenciais no terminal PowerShell antes de iniciar a aplicacao:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/abastecimento?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Africa/Maputo"
$env:DB_USERNAME = "abastecimento_app"
$securePassword = Read-Host "Palavra-passe do MySQL" -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new("", $securePassword).Password
.\mvnw.cmd spring-boot:run
```

`DB_URL` e `DB_USERNAME` ja possuem valores locais por omissao. A palavra-passe e obrigatoria e e pedida sem aparecer no terminal. Importe o SQL antes de iniciar a aplicacao: o Hibernate valida o esquema existente, mas nao cria nem altera tabelas (`ddl-auto: validate`). Os testes usam uma base H2 isolada e criam o seu proprio esquema. As variaveis definidas no PowerShell ficam activas apenas nessa janela.

### Modelo e regras assumidas

- Um cliente pode ter varias ligacoes de agua; uma ligacao pode ter um ou mais contadores ao longo do tempo.
- Cada consumo pertence a uma ligacao e a um contador e regista as leituras inicial/final e o periodo facturado.
- Cada consumo pode originar no maximo uma factura. Uma factura pode receber varios pagamentos.
- As tarifas guardam taxa fixa e preco por metro cubico com datas de vigencia; os valores aplicados ficam copiados na factura.
- Os utilizadores podem ter varios perfis. `senha_hash` deve guardar apenas o hash seguro da palavra-passe, nunca o texto original.
- A base guarda valores monetarios com duas casas decimais e consumos em m3 com tres casas decimais.

O documento da proposta ainda nao especifica todas estas regras; confirme-as com o docente antes de tratar o esquema como modelo final. O SQL e um esquema inicial, nao um sistema de migracoes: `CREATE TABLE IF NOT EXISTS` nao altera tabelas ja existentes.

Para executar os testes, nao e necessario iniciar MySQL:

```powershell
.\mvnw.cmd test
```

## Autenticacao e perfis

A interface em `http://localhost:8080` redirecciona para o inicio de sessao quando nao existe uma sessao valida. A autenticacao usa sessao HTTP, senha BCrypt e proteccao CSRF nos pedidos que alteram dados.

| Perfil | Permissoes principais |
| --- | --- |
| `ADMIN` | Consulta e gere clientes, utilizadores e operacoes. |
| `LEITOR` | Consulta dados operacionais e regista leituras; nao gere contas. |
| `CLIENTE` | Consulta apenas clientes, ligacoes e leituras associados a sua conta. |

Contas ficticias semeadas por `data.sql` (todas com senha `password`): `admin`, `maria.leitora`, `helena.cliente` e `joao.cliente`. Use-as apenas localmente para demonstracao.

Rotas REST principais:

- `POST /api/v1/auth/login`, `GET /api/v1/auth/me` e `POST /api/v1/auth/logout` - iniciar, consultar e terminar sessao.
- `GET /api/v1/dashboard/resumo` - indicadores operacionais.
- `GET /api/v1/clientes` e `POST /api/v1/clientes` - consulta para `ADMIN`/`LEITOR`; criacao apenas para `ADMIN`.
- `GET /api/v1/ligacoes`, `/api/v1/contadores`, `/api/v1/tarifas` e `/api/v1/leituras`; `POST /api/v1/leituras` regista uma leitura para `ADMIN`/`LEITOR`.
- `GET /api/v1/admin/utilizadores` e `POST /api/v1/admin/utilizadores` - gestao exclusiva de contas pelo `ADMIN`.
- `GET /api/v1/portal/clientes` e `/api/v1/portal/leituras` - dados limitados as associacoes do utilizador `CLIENTE`.

Exemplo de corpo para `POST /api/v1/clientes`:

```json
{
  "nome": "Ana Mussa",
  "documento": "123456789",
  "email": "ana@example.com",
  "telefone": "+258 84 000 0000"
}
```

## Interface web

Com a aplicacao iniciada e a base de dados acessivel, abra `http://localhost:8080` e inicie sessao com uma conta de demonstracao. O frontend usa HTML, CSS e JavaScript nativo, sem dependencias externas, e comunica com a API Spring Boot ligada ao MySQL. Inclui painel de indicadores, pesquisa e paginacao de tabelas, registo de clientes e leituras, consultas de ligacoes/contadores/tarifas e gestao de utilizadores para `ADMIN`; o portal `CLIENTE` apresenta somente os dados associados a sua conta. Facturacao e pagamentos continuam como areas futuras, sem operacoes ligadas no backend.

Existe tambem um perfil PostgreSQL opcional. Para o utilizar, configure `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` e active o perfil:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/abastecimento"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "a-sua-palavra-passe"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

O perfil PostgreSQL valida o esquema existente (`ddl-auto: validate`); as migracoes de esquema serao adicionadas quando os modulos e entidades forem definidos.

## Dependencias principais

- Spring Boot Web e Validation: API HTTP e validacao.
- Spring Data JPA com Hibernate: persistencia ORM.
- MySQL Connector/J: ligacao JDBC usada pela aplicacao.
- PostgreSQL JDBC Driver: ligacao ao PostgreSQL.
- H2, JUnit 5 e Mockito: testes automatizados.

RMI nao foi incluido como dependencia: faz parte do JDK e nao e necessario para o fluxo web inicial descrito na proposta. JDBC e utilizado pela camada de persistencia atraves do Hibernate; um modulo RMI so deve ser acrescentado se surgir um caso de uso remoto concreto.

Esta base ja demonstra encapsulamento, interfaces, coleccoes, excepcoes, testes JUnit e persistencia com Hibernate/JPA sobre JDBC. Foi escolhido Spring Boot para executar a aplicacao web sem instalar um servidor externo; apesar de usar APIs Jakarta, nao e um deploy Jakarta EE puro em Payara/WildFly. Se a avaliacao exigir especificamente um servidor JEE, essa decisao deve ser confirmada antes de expandir os modulos.

## Nota sobre o documento de proposta

O titulo e a descricao funcional tratam do abastecimento de agua, mas o indice e alguns diagramas ainda mencionam candidaturas e vagas. Este esqueleto segue o titulo e os requisitos funcionais sobre clientes, consumo, facturacao e pagamentos; convem actualizar essas partes do documento antes de expandir o modelo.
