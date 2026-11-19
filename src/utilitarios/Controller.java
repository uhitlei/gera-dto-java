/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilitarios;

import com.java.geraDTO.bean.ListColuna;
import com.java.geraDTO.bean.ListTabela;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author WILL
 */
/*
 * 
 * Connection conn = null;  
 ResultSet result = null;  
 ResultSet result1 = null;  
 try {  
 Class.forName("com.mysql.jdbc.Driver");  
 conn = DriverManager.getConnection("jdbc:mysql://localhost/demojavabeans", "root", "");  
 result = conn.getMetaData().getTables(null, null, "", null);  
 result1 = conn.getMetaData().getColumns(null, null, "", null);  
 while (result.next()) {  
 System.out.println(result.getString("TABLE_NAME"));  
 System.out.println(result.getString("TABLE_TYPE"));  
 }  
              
 while (result1.next()) {  
 System.out.println(result1.getString("TABLE_NAME"));  
 System.out.println(result1.getString("COLUMN_NAME"));  
 }  
 } finally {  
 conn.close();  
 }  

 * 
 * 
 * 
 * 
 */
public class Controller extends conexao {

//    private String SqlRetornaTabelas = "SELEC * FROM siscom.tables as tab";
//    private String SqlRetornaCampos;

    /**
     * Monta o nome qualificado da tabela com os identificadores entre aspas do
     * proprio banco (ex.: `schema`.`tabela` no MySQL, "SCHEMA"."TABELA" no Oracle).
     * Aspas internas sao duplicadas, evitando injecao de SQL pelo nome.
     */
    public static String nomeQualificado(String schema, String tabela, String aspas) {
        String q = (aspas == null || aspas.trim().isEmpty()) ? "" : aspas.trim();
        StringBuilder sb = new StringBuilder();
        if (schema != null && !schema.trim().isEmpty()) {
            sb.append(cita(schema, q)).append('.');
        }
        return sb.append(cita(tabela, q)).toString();
    }

    private static String cita(String nome, String q) {
        if (q.isEmpty()) {
            return nome;
        }
        return q + nome.replace(q, q + q) + q;
    }

    private static void fecha(AutoCloseable recurso) {
        if (recurso != null) {
            try {
                recurso.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List<ListColuna> listaColunas(String Schema, String Tabela, String getURL, String getDriver, String getUsuario, String getSenha) {
        List<ListColuna> colunas = new ArrayList<ListColuna>();
        ListColuna cols;
        Statement stmt = null;
        try {
            Connection con = conectar(getURL, getDriver, getUsuario, getSenha);
            if (con == null) {
                JOptionPane.showMessageDialog(null, "Class: Controle: nao foi possivel conectar ao banco");
                return colunas;
            }
            String tabela = nomeQualificado(Schema, Tabela, con.getMetaData().getIdentifierQuoteString());
            stmt = con.createStatement();
            // WHERE 1=0: so precisamos dos metadados, nao dos registros
            rs = stmt.executeQuery("SELECT * FROM " + tabela + " WHERE 1=0");

            ResultSetMetaData md = rs.getMetaData();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                cols = new ListColuna();
                cols.setColuna(md.getColumnLabel(i));
                cols.setTypoColuna(md.getColumnTypeName(i));
                colunas.add(cols);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Class: Controle: SQL ERRO: " + e.getMessage());
            e.printStackTrace();
        } finally {
            fecha(rs);
            rs = null;
            fecha(stmt);
            desconectar();
        }

        return colunas;
    }

    public List<ListTabela> ListaTabelas(String getURL, String getDriver, String getUsuario, String getSenha) {
        List<ListTabela> tabela = new ArrayList<ListTabela>();
        ListTabela tabs;

        try {
            Connection con = conectar(getURL, getDriver, getUsuario, getSenha);
            if (con == null) {
                JOptionPane.showMessageDialog(null, "Class: Controle: nao foi possivel conectar ao banco");
                return tabela;
            }
            DatabaseMetaData metaData = con.getMetaData();

            rs = metaData.getTables(con.getCatalog(), null, "%", null);
            while (rs.next()) {
                tabs = new ListTabela();
                tabs.setTabela(rs.getString("TABLE_NAME"));
                tabs.setTipoTabela(rs.getString("TABLE_TYPE"));
                tabs.setCatTable(rs.getString("TABLE_CAT"));
                // TABLE_SCHEM vem null no MySQL; antes isso gerava NullPointerException
                String schema = rs.getString("TABLE_SCHEM");
                if (schema != null && !schema.isEmpty()) {
                    tabs.setSchemaTabela(schema);// para Sybase
                }
                tabela.add(tabs);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Class: Controle: SQL ERRO: " + e.getMessage());
            e.printStackTrace();
        } finally {
            fecha(rs);
            rs = null;
            desconectar();
        }

        return tabela;
    }
}
