package com.java.geraDTO.gerador;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.java.geraDTO.bean.ListColuna;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class GeradorClasseDTOTest {

    @Rule
    public TemporaryFolder pasta = new TemporaryFolder();

    private final GeradorClasseDTO gerador = new GeradorClasseDTO();

    @Test
    public void primeiraLetraMaior() {
        assertEquals("Cliente", gerador.getPrimeiraLetraMaior("cliente"));
        assertEquals("Cliente", gerador.getPrimeiraLetraMaior("CLIENTE"));
        assertEquals("Datacadastro", gerador.getPrimeiraLetraMaior("dataCadastro"));
        assertEquals("", gerador.getPrimeiraLetraMaior(""));
    }

    @Test
    public void geraPacoteClasseConstrutorEAtributos() {
        String codigo = gerador.gerarCodigo("cliente", Colunas.cliente());

        assertTrue(codigo.startsWith("package com.java.geraDTO.classeGerada.bean;"));
        assertTrue(codigo.contains("public class Cliente {"));
        assertTrue(codigo.contains("public Cliente(){"));
        assertTrue(codigo.contains(" private Integer codigo;"));
        assertTrue(codigo.contains(" private String nome;"));
        assertTrue(codigo.contains(" private java.math.BigDecimal saldo;"));
        assertTrue(codigo.contains(" private java.sql.Date nascimento;"));
        assertTrue(codigo.contains(" private Boolean ativo;"));
    }

    @Test
    public void geraGettersESetters() {
        String codigo = gerador.gerarCodigo("cliente", Colunas.cliente());

        assertTrue(codigo.contains("public Integer getCodigo(){"));
        assertTrue(codigo.contains("return codigo;"));
        assertTrue(codigo.contains("public void setSaldo(java.math.BigDecimal saldo) {"));
        assertTrue(codigo.contains("this.saldo = saldo;"));
    }

    @Test
    public void gravaDtoEDaoNoDiretorioInformado() throws Exception {
        File destino = new File(pasta.getRoot(), "saida");

        assertTrue(gerador.gerarClasse("cliente", Colunas.cliente(), "utilitarios", "conexao", destino));

        File dto = new File(destino, "Cliente.java");
        File dao = new File(destino, "ClienteDAO.java");
        assertTrue(dto.isFile());
        assertTrue(dao.isFile());
        String conteudo = new String(Files.readAllBytes(dto.toPath()), Charset.forName("UTF-8"));
        assertEquals(gerador.gerarCodigo("cliente", Colunas.cliente()), conteudo);
    }

    @Test
    public void naoGeraSemColunas() {
        assertFalse(gerador.gerarClasse("cliente", new ArrayList<ListColuna>(), "utilitarios", "conexao", pasta.getRoot()));
        assertFalse(gerador.gerarClasse("", Colunas.cliente(), "utilitarios", "conexao", pasta.getRoot()));
    }
}
