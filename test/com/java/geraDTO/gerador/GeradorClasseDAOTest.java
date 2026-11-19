package com.java.geraDTO.gerador;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GeradorClasseDAOTest {

    private static String dao() {
        return new GeradorClasseDAO("cliente", Colunas.cliente(), "utilitarios", "conexao").gerarCodigo();
    }

    private static int ocorrencias(String texto, String trecho) {
        int n = 0;
        for (int i = texto.indexOf(trecho); i >= 0; i = texto.indexOf(trecho, i + 1)) {
            n++;
        }
        return n;
    }

    @Test
    public void cabecalhoEImportDaClasseDeConexao() {
        String codigo = dao();
        assertTrue(codigo.startsWith("package com.java.geraDTO.classeGerada.bean;"));
        assertTrue(codigo.contains("import utilitarios.conexao;"));
        assertTrue(codigo.contains("public class ClienteDAO {"));
        assertTrue(codigo.contains("Class.forName(new conexao().config.getString(\"driver\"));"));
    }

    @Test
    public void semPacoteDeConexaoNaoGeraImportInvalido() {
        String codigo = new GeradorClasseDAO("cliente", Colunas.cliente(), " ", "conexao").gerarCodigo();
        assertFalse(codigo.contains("import ."));
    }

    @Test
    public void sqlsUsamPlaceholdersEPrimeiraColunaComoChave() {
        String codigo = dao();
        assertTrue(codigo.contains("\"INSERT INTO cliente (nome, saldo, nascimento, ativo ) VALUES (?, ?, ?, ?)\""));
        assertTrue(codigo.contains("\"UPDATE cliente SET nome=?, saldo=?, nascimento=?, ativo=? WHERE codigo=?\""));
        assertTrue(codigo.contains("\"DELETE FROM cliente WHERE codigo=?\""));
        assertTrue(codigo.contains("\"SELECT * FROM cliente WHERE codigo=?\""));
        assertTrue(codigo.contains("\"SELECT * FROM cliente\""));
    }

    @Test
    public void parametrosUsamOMetodoJdbcDoTipo() {
        String codigo = dao();
        assertTrue(codigo.contains("ps.setString(1, cliente.getNome());"));
        assertTrue(codigo.contains("ps.setBigDecimal(2, cliente.getSaldo());"));
        assertTrue(codigo.contains("ps.setDate(3, cliente.getNascimento());"));
        assertTrue(codigo.contains("ps.setBoolean(4, cliente.getAtivo());"));
        assertTrue(codigo.contains("ps.setInt(5, cliente.getCodigo());"));
        assertTrue(codigo.contains("cliente.setSaldo(rs.getBigDecimal(\"saldo\"));"));
    }

    @Test
    public void localizarUsaTipoDaChaveEChamaNext() {
        String codigo = new GeradorClasseDAO("pedido", Colunas.de("numero", "BIGINT", "obs", "TEXT"),
                "utilitarios", "conexao").gerarCodigo();
        assertTrue(codigo.contains("public Pedido localizar(Long numero) throws SQLException {"));
        assertTrue(codigo.contains("ps.setLong(1, numero);"));
        int executa = codigo.indexOf("rs = ps.executeQuery();");
        int next = codigo.indexOf("if(!rs.next()){", executa);
        int leitura = codigo.indexOf("pedido.setNumero(rs.getLong(\"numero\"));", executa);
        assertTrue("localizar deve chamar rs.next() antes de ler", executa >= 0 && next > executa && leitura > next);
    }

    @Test
    public void finallyFechaRecursosSemNullPointer() {
        String codigo = dao();
        assertEquals(5, ocorrencias(codigo, "if (ps != null) ps.close();"));
        assertEquals(2, ocorrencias(codigo, "if (rs != null) rs.close();"));
        assertEquals(5, ocorrencias(codigo, "con.close();"));
        assertEquals("todo ps.close() deve ter checagem de null", 5, ocorrencias(codigo, "ps.close();"));
    }

    @Test
    public void mensagensDeErroIdentificamOMetodo() {
        String codigo = dao();
        assertTrue(codigo.contains("ClasseDAO Func. Listar"));
        assertTrue(codigo.contains("Excluido Com Sucesso"));
    }
}
