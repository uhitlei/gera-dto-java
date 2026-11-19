/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.java.geraDTO.event;

import com.java.geraDTO.bean.ListColuna;
import com.java.geraDTO.bean.ListTabela;
import com.java.geraDTO.form.TelaBanco;
import com.java.geraDTO.gerador.GeradorClasseDTO;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import utilitarios.Controller;
import utilitarios.conexao;

/**
 *
 * @author WILL
 */
public class EventTelaBanco extends TelaBanco implements ActionListener {
    List<ListColuna> colunas= new ArrayList<ListColuna>();
    List<ListTabela> tabelas= new ArrayList<ListTabela>();
    public EventTelaBanco() {
        setLocationRelativeTo(null);
        setResizable(false);
        ImageIcon icon = (ImageIcon) lbJavaBall.getIcon();
        this.setIconImage(icon.getImage());
       // listarTabelas();
       // listarColunas();
        btPegarCampos.addActionListener(this);
        btPegarTabelas.addActionListener(this);
     //   cbTabelas.addActionListener(this);
        btGeraDTO.addActionListener(this);
        
       
        
    }
 
    private String senha() {
        return new String(tfSenha.getPassword());
    }

    /** Tabela selecionada no combo, ou null se nenhuma (evita IndexOutOfBounds). */
    private ListTabela tabelaSelecionada() {
        int i = cbTabelas.getSelectedIndex();
        if (i < 0 || i >= tabelas.size()) {
            lbOk.setText("Selecione uma tabela");
            return null;
        }
        return tabelas.get(i);
    }

    private void listarColunas(){
        ListTabela tabela = tabelaSelecionada();
        if (tabela == null) {
            return;
        }
        Controller rotina = new Controller();
        //OBS CRIAR UMA CLASSE PARA ARMAZENAR ESSES DADOS
        colunas = rotina.listaColunas(tabela.getSchemaTabela(),
                                      tabela.getTabela(),
                                      tfHost.getText(),cbSSGBD.getSelectedItem().toString(),
                                      tfUsuario.getText(),senha());
        cbCampos.removeAllItems();
        for (int i = 0; i < colunas.size(); i++) {
            cbCampos.addItem(colunas.get(i).getColuna()+" Tipo: "+colunas.get(i).getTypoColuna());
        }
    }
    private void listarTabelas(){
        Controller rotina = new Controller();
        tabelas =  rotina.ListaTabelas(tfHost.getText(),
                                       cbSSGBD.getSelectedItem().toString(),
                                       tfUsuario.getText(),
                                       senha() );
        cbTabelas.removeAllItems();
        cbCampos.removeAllItems();
        colunas = new ArrayList<ListColuna>();
        for(int i=0; i<tabelas.size();i++){
            cbTabelas.addItem(tabelas.get(i).getTabela());
        }

    }

    private void geraDTO(){
        ListTabela tabela = tabelaSelecionada();
        if (tabela == null) {
            return;
        }
        // Sempre relê as colunas da tabela selecionada: antes, trocar a tabela no
        // combo sem clicar em "Pegar Colunas" gerava a classe com as colunas da anterior.
        listarColunas();
        if(new GeradorClasseDTO().gerarClasse(tabela.getTabela(), colunas, tfPacote.getText(), tfConexao.getText())){
            lbOk.setText("GERADO");
            lbOk.setToolTipText(conexao.getDiretorioSaida().getAbsolutePath());
        }else
            lbOk.setText("Não gerado");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == cbTabelas|| e.getSource() == btPegarCampos)
            listarColunas();
        if(e.getSource() == btPegarTabelas)
            listarTabelas();
        
        if(e.getSource() == btGeraDTO)
            geraDTO();
    }
    
    
}
