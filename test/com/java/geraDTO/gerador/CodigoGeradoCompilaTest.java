package com.java.geraDTO.gerador;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;

import com.java.geraDTO.bean.ListColuna;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Garante que o DTO e o DAO gerados compilam de verdade (com javac), usando a
 * classe utilitarios.conexao do proprio projeto como classe de conexao.
 */
public class CodigoGeradoCompilaTest {

    @Rule
    public TemporaryFolder pasta = new TemporaryFolder();

    private void compila(String tabela, List<ListColuna> colunas) throws Exception {
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        assumeNotNull(javac); // so roda em JDK, nao em JRE

        File fontes = pasta.newFolder(tabela + "-src");
        File classes = pasta.newFolder(tabela + "-classes");
        assertTrue(new GeradorClasseDTO().gerarClasse(tabela, colunas, "utilitarios", "conexao", fontes));

        String nome = new GeradorClasseDTO().getPrimeiraLetraMaior(tabela);
        ByteArrayOutputStream erros = new ByteArrayOutputStream();
        int status = javac.run(null, null, erros,
                "-encoding", "UTF-8",
                "-cp", System.getProperty("java.class.path"),
                "-d", classes.getAbsolutePath(),
                new File(fontes, nome + ".java").getAbsolutePath(),
                new File(fontes, nome + "DAO.java").getAbsolutePath());

        assertEquals("javac falhou:\n" + erros, 0, status);
        assertTrue(new File(classes, "com/java/geraDTO/classeGerada/bean/" + nome + "DAO.class").isFile());
    }

    @Test
    public void clienteCompila() throws Exception {
        compila("cliente", Colunas.cliente());
    }

    @Test
    public void todosOsTiposMapeadosCompilam() throws Exception {
        compila("todos", Colunas.de(
                "id", "BIGINT", "inteiro", "INT", "grande", "INT UNSIGNED", "valor", "NUMERIC",
                "real", "DOUBLE", "flag", "BOOLEAN", "texto", "VARCHAR", "dia", "DATE",
                "momento", "DATETIME", "hora", "TIME", "arquivo", "BLOB", "outro", "GEOMETRY"));
    }
}
