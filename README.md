<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="docs/logo-dark.png">
    <img src="docs/logo.png" alt="GeraDTO" width="460">
  </picture>
</p>

<p align="center">
  Ferramenta desktop em Java Swing que lê as tabelas de um banco de dados e gera as classes DTO e DAO (CRUD com JDBC) de cada uma.
</p>

<p align="center">
  <a href="https://github.com/uhitlei/gera-dto-java/actions/workflows/ci.yml"><img alt="CI" src="https://github.com/uhitlei/gera-dto-java/actions/workflows/ci.yml/badge.svg"></a>
  <img alt="Java 8" src="https://img.shields.io/badge/Java-8-ED8B00">
  <img alt="Swing" src="https://img.shields.io/badge/UI-Swing-5382A1">
  <img alt="Testes JUnit 4" src="https://img.shields.io/badge/testes-JUnit%204-2E8B57">
  <a href="LICENSE"><img alt="Licença MIT" src="https://img.shields.io/badge/licen%C3%A7a-MIT-blue"></a>
</p>

---

## Sobre

Em um projeto Java com JDBC puro, cada tabela pede as mesmas duas classes: um DTO com um atributo, um getter e um setter por coluna, e um DAO com `salvar`, `atualizar`, `excluir`, `localizar` e `listar`. Escrever isso à mão para cada tabela é repetitivo e fácil de errar.

O GeraDTO ("Gerador de DTO e DAO 3.0", na barra de título) automatiza esse trabalho: você informa o driver, a URL JDBC, o usuário e a senha, escolhe a tabela, e ele lê os metadados das colunas pelo próprio JDBC e escreve os dois arquivos `.java`. O formulário principal é de 2012 e o código foi publicado no GitHub em 2015. O projeto é do NetBeans, com build em Ant.

## O que ele gera

Para cada tabela, dois arquivos no pacote `com.java.geraDTO.classeGerada.bean`:

| Arquivo | Conteúdo |
|---|---|
| `Cliente.java` | DTO com construtor vazio, um atributo `private` por coluna e os getters/setters |
| `ClienteDAO.java` | DAO com `salvar`, `atualizar`, `excluir`, `localizar` e `listar`, todos com `PreparedStatement` |

O DAO não guarda URL nem senha. Ele importa uma classe de conexão do **seu** projeto, informada na tela (pacote e nome da classe), e lê dela `config.getString("driver" | "url" | "usuario" | "senha")`. A classe [`utilitarios.conexao`](src/utilitarios/conexao.java) deste repositório segue esse contrato e serve de modelo.

Exemplo real: estas são as classes geradas para a tabela abaixo, em um MySQL 8.4, com pacote de conexão `utilitarios` e classe `conexao`:

```sql
CREATE TABLE cliente (
  codigo INT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(80) NOT NULL,
  email VARCHAR(120),
  saldo DECIMAL(10,2),
  nascimento DATE,
  cadastro DATETIME,
  ativo BIT(1),
  pontos INT UNSIGNED
);
```

Trecho do `Cliente.java`:

```java
package com.java.geraDTO.classeGerada.bean;

public class Cliente {

public Cliente(){
		
}

 private Integer codigo;
 private String nome;
 private String email;
 private java.math.BigDecimal saldo;
 private java.sql.Date nascimento;
 private java.sql.Timestamp cadastro;
 private Boolean ativo;
 private Long pontos;

 /** 
* @return the codigo
 */
public Integer getCodigo(){
   return codigo;
}
```

Trecho do `ClienteDAO.java`, com o método `localizar`. A indentação é a que o gerador produz:

```java
		public Cliente localizar(Integer codigo) throws SQLException {
		Connection con = DriverManager.getConnection(new conexao().config.getString("url") ,new conexao().config.getString("usuario"),new conexao().config.getString("senha"));
		PreparedStatement ps=null;
		ResultSet rs=null;
		String sqlLocalizar="SELECT * FROM cliente WHERE codigo=?";
		Cliente cliente =  new Cliente();
					try{
			ps = con.prepareStatement(sqlLocalizar);
			ps.setInt(1, codigo);
			rs = ps.executeQuery();
			if(!rs.next()){
				return null;
			}
			cliente.setCodigo(rs.getInt("codigo"));
			cliente.setNome(rs.getString("nome"));
			cliente.setEmail(rs.getString("email"));
			cliente.setSaldo(rs.getBigDecimal("saldo"));
			cliente.setNascimento(rs.getDate("nascimento"));
			cliente.setCadastro(rs.getTimestamp("cadastro"));
			cliente.setAtivo(rs.getBoolean("ativo"));
			cliente.setPontos(rs.getLong("pontos"));
			return cliente; 
		}catch (NumberFormatException e) {
			...
		}
			finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
			con.close();
			}
		return cliente; 
		}
```

Os comandos SQL gerados para essa tabela:

```sql
INSERT INTO cliente (nome, email, saldo, nascimento, cadastro, ativo, pontos ) VALUES (?, ?, ?, ?, ?, ?, ?)
UPDATE cliente SET nome=?, email=?, saldo=?, nascimento=?, cadastro=?, ativo=?, pontos=? WHERE codigo=?
DELETE FROM cliente WHERE codigo=?
SELECT * FROM cliente WHERE codigo=?
SELECT * FROM cliente
```

### Mapeamento de tipos

A conversão fica em [`MapeadorTipos`](src/com/java/geraDTO/gerador/MapeadorTipos.java). Ela recebe o nome de tipo informado pelo driver e ignora maiúsculas, tamanho (`varchar(50)`) e o sufixo `identity`:

| Tipo da coluna | Tipo no DTO | Método JDBC no DAO |
|---|---|---|
| `INT`, `INTEGER`, `SMALLINT`, `TINYINT`, `MEDIUMINT` | `Integer` | `setInt` / `getInt` |
| `BIGINT`, `INT UNSIGNED` | `Long` | `setLong` / `getLong` |
| `NUMERIC`, `DECIMAL`, `NUMBER`, `MONEY`, `BIGINT UNSIGNED` | `java.math.BigDecimal` | `setBigDecimal` / `getBigDecimal` |
| `DOUBLE`, `FLOAT`, `REAL` | `Double` | `setDouble` / `getDouble` |
| `BIT`, `BOOL`, `BOOLEAN` | `Boolean` | `setBoolean` / `getBoolean` |
| `VARCHAR`, `CHAR`, `TEXT`, `NVARCHAR`, `VARCHAR2`, `CLOB`, `ENUM`… | `String` | `setString` / `getString` |
| `DATE` | `java.sql.Date` | `setDate` / `getDate` |
| `DATETIME`, `DATETIME2`, `SMALLDATETIME`, `TIMESTAMP` | `java.sql.Timestamp` | `setTimestamp` / `getTimestamp` |
| `TIME` | `java.sql.Time` | `setTime` / `getTime` |
| `BLOB`, `BINARY`, `VARBINARY`, `IMAGE`, `RAW` | `byte[]` | `setBytes` / `getBytes` |
| qualquer outro | `Object` | `setObject` / `getObject` |

## Como usar

1. Compile com `ant jar` (veja [Compilar e testar](#compilar-e-testar)) ou baixe o `GeraDTO.jar` da [página de releases](https://github.com/uhitlei/gera-dto-java/releases).
2. Baixe o driver JDBC do seu banco. O GeraDTO não inclui nenhum.
3. Execute com o driver no classpath. No Windows, troque `:` por `;`:
   ```bash
   java -cp "dist/GeraDTO.jar:mysql-connector-j-8.4.0.jar" com.java.geraDTO.Classes.ClasseTelaBanco
   ```
   O `java -jar dist/GeraDTO.jar` também abre a tela, mas nesse modo a JVM ignora o `-cp` e o driver não é encontrado.
4. Na tela:
   - **SGDB:** a classe do driver JDBC.
   - **Host do Data Base:** a URL JDBC completa, por exemplo `jdbc:mysql://localhost:3306/banco`.
   - **Usuario** e **Senha** do banco.
   - **Pegar Tabelas:** carrega as tabelas e views do catálogo atual da conexão.
   - **Pegar Colunas:** mostra as colunas e os tipos da tabela escolhida.
   - **Pacote de de implat.** e **Classe Conexao:** o pacote e o nome da classe de conexão que o DAO vai importar. O padrão é `utilitarios` e `conexao`.
   - **GERA DTO/DAO:** grava `Tabela.java` e `TabelaDAO.java`. O status muda para `GERADO`, e a dica do rótulo mostra a pasta.

Os arquivos vão para a pasta `gerado/` do diretório em que o `java` foi executado. Para usar outra pasta, copie [`bancodedados.properties.exemplo`](src/utilitarios/bancodedados.properties.exemplo) para `src/utilitarios/bancodedados.properties`, ajuste a chave `local` e recompile. Esse arquivo está no `.gitignore`, porque pode conter credenciais.

## Bancos suportados

A lista **SGDB** da tela oferece estes drivers (a lista não é editável):

| Driver | Banco | Verificado em 2026 |
|---|---|---|
| `com.mysql.jdbc.Driver` | MySQL | Sim: MySQL 8.4 com Connector/J 8.4.0 |
| `oracle.jdbc.driver.OracleDriver` | Oracle | Não |
| `net.sourceforge.jtds.jdbc.Driver` | SQL Server e Sybase via jTDS | Não |
| `com.sybase.jdbc4.jdbc.SybDriver` | Sybase (jConnect) | Não |
| `com.microsoft.sqlserver.jdbc.SQLServerDriver` | Microsoft SQL Server | Não |

O teste com MySQL rodou em Docker: listou as tabelas, leu as colunas e gerou o DTO e o DAO, que compilaram. Fora da tela, o mesmo fluxo também funcionou com a classe nova do Connector/J, `com.mysql.cj.jdbc.Driver`, que não está na lista. A leitura usa só a API padrão do JDBC (`DatabaseMetaData.getTables` e `ResultSetMetaData`), por isso deve funcionar nos outros bancos da lista. O mapeamento de tipos já cobre os nomes que eles usam, mas esses bancos não foram testados.

## Estrutura

```
build.xml                         Build Ant (importa nbproject/build-impl.xml), download das dependências
nbproject/                        Projeto NetBeans (project.properties: Java 8, classe principal)
src/
├── com/java/geraDTO/
│   ├── Classes/ClasseTelaBanco   Método main: aplica o Nimbus e abre a tela
│   ├── form/TelaBanco            Tela Swing gerada pelo editor do NetBeans (.java + .form)
│   ├── event/EventTelaBanco      Ações dos botões
│   ├── gerador/                  GeradorClasseDTO, GeradorClasseDAO e MapeadorTipos
│   ├── bean/                     ListTabela e ListColuna (metadados lidos do banco)
│   └── image/                    Imagens da tela
└── utilitarios/                  conexao e Controller (leitura de metadados), Pool e DataSource
test/                             Testes JUnit 4
lib/                              Modelos do MySQL Workbench (.mwb) do projeto original. Os jars baixados também ficam aqui
.github/workflows/ci.yml          Pipeline de CI/CD
```

Algumas classes do código original não são usadas pela tela e foram mantidas como estavam: `Controller2`, `Pool` e `DataSource` (uma variante com pool de conexões), `geradorClasseAlt` (uma versão antiga do gerador, com caminho fixo `D:/...`), `Local` e `classeGerada/bean/TesteIserir`.

## Compilar e testar

Requisitos: JDK 8 ou superior e Ant 1.9 ou superior, com o suporte a JUnit (pacote `ant-optional` no Debian e no Ubuntu). No primeiro build, o alvo `-pre-init` do `build.xml` baixa do Maven Central, para `lib/`, o `AbsoluteLayout` (layout da tela), o JUnit 4.13.2 e o Hamcrest 1.3, e confere o SHA-256 de cada jar. Para trabalhar offline, copie os três jars para `lib/` com os mesmos nomes.

### NetBeans

Abra a pasta do repositório como projeto (*File > Open Project*). *Clean and Build* gera o `dist/GeraDTO.jar`, *Test* roda os testes e *Run* abre a tela.

### Ant

```bash
ant jar     # compila e gera dist/GeraDTO.jar, com o AbsoluteLayout embutido
ant test    # compila e roda os testes JUnit
ant run     # abre a tela (sem driver JDBC no classpath)
```

### Docker

Para compilar e testar sem instalar JDK nem Ant:

```bash
docker run --rm -v "$PWD":/src -w /src eclipse-temurin:8-jdk bash -c '
  apt-get update -qq && apt-get install -y -qq ant ant-optional > /dev/null &&
  ant clean jar test'
```

### Testes

São 26 testes em 5 classes, e nenhum precisa de banco de dados:

| Classe | O que cobre |
|---|---|
| `MapeadorTiposTest` | Conversão de tipos SQL para tipos Java e métodos JDBC |
| `GeradorClasseDTOTest` | Texto do DTO, nomes de getters e setters, gravação dos arquivos em pasta temporária |
| `GeradorClasseDAOTest` | SQL gerado, `setXxx`/`getXxx` por tipo, `rs.next()` no `localizar`, fechamento de recursos |
| `CodigoGeradoCompilaTest` | Compila com `javac` o DTO e o DAO gerados, incluindo uma tabela com todos os tipos mapeados |
| `ControllerTest` | Nome de tabela entre aspas do banco, configuração opcional e pasta de saída padrão |

## CI/CD

O workflow [`ci.yml`](.github/workflows/ci.yml) roda no GitHub Actions a cada push e a cada pull request para `master` e `develop`:

1. Instala o Temurin 8 e o Ant em um runner Ubuntu.
2. Roda `ant clean jar test` e guarda o resultado dos testes como artefato.
3. Publica o `GeraDTO.jar` como artefato.
4. Quando o push é de uma tag `v*` (ex.: `v3.1.0`), publica uma release no GitHub com o jar anexado. Só esse job tem permissão de escrita no repositório.

Para lançar uma versão:

```bash
git tag v3.1.0
git push origin v3.1.0
```

## Limitações conhecidas

- **Chave primária:** a primeira coluna da tabela é tratada como chave. O `salvar` não a inclui no `INSERT`, porque supõe auto-incremento. O `atualizar`, o `excluir` e o `localizar` a usam no `WHERE`.
- **Pacote fixo:** as classes geradas sempre ficam no pacote `com.java.geraDTO.classeGerada.bean`.
- **Nomes:** os nomes de tabelas e colunas precisam ser identificadores Java válidos. O nome da classe e os getters e setters ficam com a primeira letra maiúscula e o resto em minúsculas (`dataCadastro` vira `getDatacadastro`). O SQL do DAO usa o nome da tabela sem schema e sem aspas.
- **DAO acoplado ao Swing:** o código gerado mostra mensagens com `JOptionPane`, abre uma conexão nova em cada método e captura as exceções em vez de propagá-las. Se houver erro, `localizar` devolve um objeto vazio.
- **Valores nulos:** em atributos `Integer`, `Long`, `Double` e `Boolean`, um valor `null` gera `NullPointerException` no `setInt`, `setLong` e afins. Na leitura, `getInt` e afins devolvem `0` ou `false` para `NULL`.
- **Driver:** o GeraDTO não inclui driver JDBC, e só os drivers da lista podem ser escolhidos.
- **Tela:** a tela tem tamanho fixo e usa o `AbsoluteLayout` do NetBeans.

## Melhorias de 2026

Ao revisitar o projeto, foram feitas estas correções e adições:

- O projeto não compilava: o `nbproject/project.properties` tinha só 4 linhas e faltava o `AbsoluteLayout` usado pela tela. O arquivo foi completado e o projeto passou para Java 8 (`javac.source`/`javac.target` `1.8`), com a classe principal definida. O `build.xml` agora baixa e confere as dependências, e o jar final já leva o `AbsoluteLayout`.
- A tela não abria nenhuma conexão se faltasse `utilitarios/bancodedados.properties`, arquivo que nunca foi versionado. O arquivo agora é opcional, e a pasta de saída padrão é `./gerado`.
- No MySQL, `Pegar Tabelas` não trazia nada: `TABLE_SCHEM` vem `null`, e a chamada `isEmpty()` lançava `NullPointerException`.
- `Controller` e `Controller2` não fechavam `Statement` e `ResultSet`, e `desconectar` quebrava quando a conexão não tinha sido aberta.
- A leitura das colunas fazia `SELECT *` da tabela inteira. Agora usa `WHERE 1=0`, que traz só os metadados, e põe schema e tabela entre as aspas do próprio banco, em vez de concatená-los direto no SQL.
- `Controller2` usava um `Pool` que nunca era criado. O `Pool` apontava para um arquivo de configuração de outro projeto e, quando a conexão livre já estava fechada, a entregava assim mesmo.
- Gerar com uma tabela diferente da que estava com as colunas carregadas produzia a classe com as colunas erradas. Agora as colunas são sempre relidas antes de gerar, e a geração sem tabela selecionada não lança mais exceção.
- O mapeamento de tipos foi extraído para `MapeadorTipos`. Antes, `NUMERIC` virava `Integer`, `INT UNSIGNED` estourava o `Integer`, `DATETIME` perdia a hora e tipos como `DECIMAL`, `BIGINT` e `VARCHAR2` viravam classes inexistentes (`Decimal`, `Bigint`, `Varchar2`).
- No DAO gerado, o `localizar` lia o `ResultSet` sem chamar `rs.next()` e sempre falhava. O parâmetro dele era sempre `Integer` e agora segue o tipo da chave. O `finally` lançava `NullPointerException` quando o `prepareStatement` falhava e nunca fechava o `ResultSet`.
- A geração do código foi separada da gravação em disco e os arquivos passaram a ser gravados em UTF-8, o que permitiu cobrir o gerador com testes JUnit 4.
- A senha passou a ser lida com `getPassword()`. A lista de drivers perdeu a ponte JDBC-ODBC, removida no Java 8 e que aparecia duplicada.
- Foram adicionados testes, o pipeline de CI/CD, a licença MIT e esta documentação.

## Licença

Distribuído sob a [licença MIT](LICENSE).
