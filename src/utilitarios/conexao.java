/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilitarios;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *
 * @author WILL
 */
public class conexao {

    /** Nome do arquivo opcional de configuracao: utilitarios/bancodedados.properties. */
    public static final String ARQUIVO_CONFIG = "utilitarios.bancodedados";

    private String url;
    private String driver;
    private String usuario;
    private String senha;
    /** Configuracao lida de bancodedados.properties; {@code null} quando o arquivo nao existe. */
    public ResourceBundle config;
    private Connection con;
    public  PreparedStatement pstm;
    public  ResultSet rs;
    public DatabaseMetaData metaData;



    //config.getString("url"),config.getString("driver"),config.getString("usuario"),config.getString("senha")
    public conexao() {
        this.config = carregaConfig();
        this.url = valor(config, "url");
        this.driver = valor(config, "driver");
        this.usuario = valor(config, "usuario");
        this.senha = valor(config, "senha");
    }

    /** Le o bundle de configuracao, se existir no classpath. */
    public static ResourceBundle carregaConfig() {
        try {
            return ResourceBundle.getBundle(ARQUIVO_CONFIG);
        } catch (MissingResourceException e) {
            return null;
        }
    }

    private static String valor(ResourceBundle config, String chave) {
        if (config == null || !config.containsKey(chave)) {
            return null;
        }
        return config.getString(chave);
    }

    /**
     * Diretorio onde o DTO/DAO sao gravados: chave "local" do
     * bancodedados.properties ou, se ausente, a pasta ./gerado.
     */
    public static File getDiretorioSaida() {
        String local = valor(carregaConfig(), "local");
        if (local == null || local.trim().isEmpty()) {
            return new File(System.getProperty("user.dir"), "gerado");
        }
        return new File(local.trim());
    }


     public Connection conectar(String url, String driver, String usuario, String senha){
        desconectar(); // nao reaproveita nem vaza uma conexao anterior
        try {
            Class.forName(driver);
            con = DriverManager.getConnection(url, usuario, senha);
            System.out.println("CONECTADO!");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            System.out.println("Class: Conexao: Nao foi possivel encontrar o DRIVER");
        }
        catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Class: Conexao: Nao foi possivel Conectar ao Banco");
        }
        return con;
    }


    public void desconectar(){
        if (con == null) {
            return;
        }
        try {
            con.close();
            System.out.println("DESCONECTADO!");
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Class: Conexao: Nao foi possivel Desconectar");
        } finally {
            con = null;
        }
    }


}
