/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.java.geraDTO.gerador;

import com.java.geraDTO.bean.ListColuna;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;
import utilitarios.conexao;

/**
 *
 * @author WILL
 */
public class GeradorClasseDTO {

    /** Pacote das classes geradas (DTO e DAO). */
    public static final String PACOTE_GERADO = "com.java.geraDTO.classeGerada.bean";

    String nomeClasse;
    List<ListColuna> atributos;
    String pacote = "package " + PACOTE_GERADO + ";\n\n";
    String primeirasLinhas;
    String construtor;
    String geterSetter;

    /**
     * Gera o DTO e o DAO da tabela no diretorio de saida configurado
     * (chave "local" de utilitarios/bancodedados.properties ou ./gerado).
     */
    public boolean gerarClasse(String nomeClasse, List<ListColuna> atributos, String Pacote, String Conexao) {
        return gerarClasse(nomeClasse, atributos, Pacote, Conexao, conexao.getDiretorioSaida());
    }

    /** Gera o DTO e o DAO da tabela no diretorio informado. */
    public boolean gerarClasse(String nomeClasse, List<ListColuna> atributos, String Pacote, String Conexao, File diretorio) {
        if (nomeClasse == null || nomeClasse.trim().isEmpty() || atributos == null || atributos.isEmpty()) {
            System.out.println("ERRO: tabela ou colunas nao informadas");
            return false;
        }
        try {
            if (!diretorio.isDirectory() && !diretorio.mkdirs()) {
                throw new IOException("Nao foi possivel criar o diretorio " + diretorio);
            }
            escreveArquivo(new File(diretorio, getPrimeiraLetraMaior(nomeClasse) + ".java"), gerarCodigo(nomeClasse, atributos));
            GeradorClasseDAO dao = new GeradorClasseDAO(nomeClasse, atributos, Pacote, Conexao);
            dao.salvarEm(diretorio);
            return true;
        } catch (IOException e) {
            System.out.println("ERRO IO: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (RuntimeException e) {
            System.out.println("ERRO Exeption: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /** Monta o codigo-fonte do DTO sem acessar banco nem disco. */
    public String gerarCodigo(String nomeClasse, List<ListColuna> atributos) {
        this.nomeClasse = nomeClasse;
        this.atributos = atributos;
        this.construtor = "public " + getPrimeiraLetraMaior(nomeClasse) + "(){\n\t\t"
                + "\n}";
        primeirasLinhas = "public class " + getPrimeiraLetraMaior(nomeClasse) + " {\n\n" + construtor + "\n\n" + getLinhasAtributos(atributos) + "\n\n\n\n\n"
                + getGetterSetter(atributos) + "\n\n}\n";
        return pacote + primeirasLinhas;
    }

    static void escreveArquivo(File arquivo, String conteudo) throws IOException {
        Writer w = new OutputStreamWriter(new FileOutputStream(arquivo), Charset.forName("UTF-8"));
        try {
            w.write(conteudo);
        } finally {
            w.close();
        }
    }

    /** Tipo Java correspondente ao tipo da coluna (ver {@link MapeadorTipos}). */
    public String getTipoAtributoCorreto(String Atributo) {
        return MapeadorTipos.tipoJava(Atributo);
    }

    private String getLinhasAtributos(List<ListColuna> atributos) {
        StringBuilder LinhaAtributos = new StringBuilder();

        for (int i = 0; i < atributos.size(); i++) {
            LinhaAtributos.append(" private ").append(getTipoAtributoCorreto(atributos.get(i).getTypoColuna()))
                    .append(" ").append(atributos.get(i).getColuna()).append(";\n");
        }
        return LinhaAtributos.toString();
    }

    public String getPrimeiraLetraMaior(String Atributo){
        String atributo = Atributo.toLowerCase();
        if (atributo.isEmpty()) {
            return atributo;
        }
        String primeiraLetra=atributo.substring(0, 1);
        String restoPalavra=atributo.substring(1,atributo.length());

        return primeiraLetra.toUpperCase()+restoPalavra;
    }
    private String getGetterSetter(List<ListColuna> atributos){
        StringBuilder gettersetter = new StringBuilder();
        String getter;
        String setter;

        for (int i = 0; i < atributos.size(); i++) {
            String tipo = getTipoAtributoCorreto(atributos.get(i).getTypoColuna());
            getter =" /** \n"
                    + "* @return the "+atributos.get(i).getColuna()+"\n"
                    + " */\n"
                    + "public "+tipo+" get"+getPrimeiraLetraMaior(atributos.get(i).getColuna())+"(){\n   "
                    + "return "+atributos.get(i).getColuna()+";\n"
                    + "}\n\n";

            setter="  /** \n"
                    + "  * @param "+atributos.get(i).getColuna()+" the "+atributos.get(i).getColuna()+" to set \n"
                    + "   */\n"
                    + "  public void set"+getPrimeiraLetraMaior(atributos.get(i).getColuna())+"("+tipo+" "
                            +atributos.get(i).getColuna()+") {\n"
                    + "    this."+atributos.get(i).getColuna()+" = "+atributos.get(i).getColuna()+";\n  } \n\n\n";

            gettersetter.append(getter).append(setter);
        }

        return gettersetter.toString();
    }

}
