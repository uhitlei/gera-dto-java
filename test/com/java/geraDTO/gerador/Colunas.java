package com.java.geraDTO.gerador;

import com.java.geraDTO.bean.ListColuna;
import java.util.ArrayList;
import java.util.List;

/** Monta listas de colunas para os testes, sem banco de dados. */
final class Colunas {

    private Colunas() {
    }

    /** Pares nome/tipo: "codigo", "INT", "nome", "VARCHAR", ... */
    static List<ListColuna> de(String... nomeTipo) {
        List<ListColuna> lista = new ArrayList<ListColuna>();
        for (int i = 0; i < nomeTipo.length; i += 2) {
            ListColuna c = new ListColuna();
            c.setColuna(nomeTipo[i]);
            c.setTypoColuna(nomeTipo[i + 1]);
            lista.add(c);
        }
        return lista;
    }

    static List<ListColuna> cliente() {
        return de("codigo", "INT", "nome", "VARCHAR", "saldo", "DECIMAL", "nascimento", "DATE", "ativo", "BIT");
    }
}
