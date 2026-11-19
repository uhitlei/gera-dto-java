/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilitarios;

import com.java.geraDTO.bean.ListColuna;
import com.java.geraDTO.bean.ListTabela;
import java.sql.Connection;
import java.sql.ResultSet;
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
public class Controller2 {

//    private String SqlRetornaTabelas = "SELEC * FROM siscom.tables as tab";
//    private String SqlRetornaCampos;


    Pool pool;

    /** Usa o pool configurado em utilitarios/bancodedados.properties. */
    public Controller2() {
        this(new Pool());
    }

    public Controller2(Pool pool) {
        this.pool = pool;
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

    public List<ListColuna> listaColunas(String Tabela) {
        List<ListColuna> colunas = new ArrayList<ListColuna>();
        ListColuna cols;
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            con = pool.getConnection();
            if (con == null) {
                JOptionPane.showMessageDialog(null, "Class: Controle: pool sem conexao disponivel");
                return colunas;
            }
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT * FROM "
                    + Controller.nomeQualificado(null, Tabela, con.getMetaData().getIdentifierQuoteString())
                    + " WHERE 1=0");
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
        }finally{
            fecha(rs);
            fecha(stmt);
            if (con != null) {
                pool.liberarConnection(con);
            }
        }

        return colunas;
    }

    public List<ListTabela> ListaTabelas() {
        List<ListTabela> tabela = new ArrayList<ListTabela>();
        ListTabela tabs;
        Connection con = null;
        ResultSet rs = null;

        try {
            con = pool.getConnection();
            if (con == null) {
                JOptionPane.showMessageDialog(null, "Class: Controle: pool sem conexao disponivel");
                return tabela;
            }
            // "%" lista todas as tabelas; o padrao "" usado antes nao casava com nenhuma
            rs = con.getMetaData().getTables(con.getCatalog(), null, "%", null);
            while (rs.next()) {
                tabs = new ListTabela();
                tabs.setTabela(rs.getString("TABLE_NAME"));
                tabs.setTipoTabela(rs.getString("TABLE_TYPE"));
                tabela.add(tabs);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Class: Controle: SQL ERRO: " + e.getMessage());
            e.printStackTrace();
        }finally{
            fecha(rs);
            if (con != null) {
                pool.liberarConnection(con);
            }
        }

        return tabela;
    }


}
