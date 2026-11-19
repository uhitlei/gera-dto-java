package utilitarios;

import static org.junit.Assert.assertEquals;

import java.io.File;
import org.junit.Test;

public class ControllerTest {

    @Test
    public void nomeQualificadoComAspasDoBanco() {
        assertEquals("`clientes`", Controller.nomeQualificado(null, "clientes", "`"));
        assertEquals("\"VENDAS\".\"PEDIDO\"", Controller.nomeQualificado("VENDAS", "PEDIDO", "\""));
        assertEquals("loja.produto", Controller.nomeQualificado("loja", "produto", " "));
        assertEquals("produto", Controller.nomeQualificado("", "produto", null));
    }

    @Test
    public void nomeQualificadoDuplicaAspasInternas() {
        assertEquals("`x``; DROP TABLE y; --`", Controller.nomeQualificado(null, "x`; DROP TABLE y; --", "`"));
    }

    @Test
    public void semArquivoDeConfiguracaoUsaPastaGerado() {
        // nao ha utilitarios/bancodedados.properties no classpath de teste
        assertEquals(null, conexao.carregaConfig());
        assertEquals(new File(System.getProperty("user.dir"), "gerado"), conexao.getDiretorioSaida());
    }

    @Test
    public void conexaoSemConfiguracaoNaoQuebra() {
        conexao c = new conexao();
        assertEquals(null, c.config);
        c.desconectar(); // sem conexao aberta: nao lanca NullPointerException
    }
}
