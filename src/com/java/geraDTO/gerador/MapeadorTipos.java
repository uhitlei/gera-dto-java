package com.java.geraDTO.gerador;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Converte o nome de tipo de coluna informado pelo driver JDBC
 * ({@code ResultSetMetaData.getColumnTypeName}) no tipo Java usado no DTO e
 * no sufixo do metodo JDBC usado no DAO ({@code ps.setXxx} / {@code rs.getXxx}).
 *
 * <p>Nao depende de banco de dados, por isso pode ser testada isoladamente.</p>
 */
public final class MapeadorTipos {

    /** Tipo Java e sufixo do metodo JDBC correspondente. */
    private static final class Mapeamento {
        final String tipoJava;
        final String metodoJdbc;

        Mapeamento(String tipoJava, String metodoJdbc) {
            this.tipoJava = tipoJava;
            this.metodoJdbc = metodoJdbc;
        }
    }

    private static final Mapeamento OBJECT = new Mapeamento("Object", "Object");
    private static final Map<String, Mapeamento> TIPOS = new HashMap<String, Mapeamento>();

    static {
        registra("Integer", "Int", "int", "integer", "smallint", "tinyint", "mediumint", "int2", "int4", "serial");
        registra("Long", "Long", "bigint", "int8", "bigserial", "int unsigned", "integer unsigned", "mediumint unsigned");
        registra("java.math.BigDecimal", "BigDecimal", "numeric", "decimal", "number", "money", "smallmoney",
                "bigint unsigned", "decimal unsigned");
        registra("Double", "Double", "double", "double precision", "float", "float8", "real", "float4", "binary_double",
                "binary_float");
        registra("Boolean", "Boolean", "bit", "bool", "boolean");
        registra("String", "String", "varchar", "char", "text", "tinytext", "mediumtext", "longtext", "nvarchar",
                "nchar", "ntext", "varchar2", "nvarchar2", "clob", "nclob", "enum", "set", "uniqueidentifier",
                "character", "character varying", "sysname", "json", "xml", "long");
        registra("java.sql.Date", "Date", "date");
        registra("java.sql.Timestamp", "Timestamp", "datetime", "datetime2", "smalldatetime", "timestamp",
                "timestamptz");
        registra("java.sql.Time", "Time", "time");
        registra("byte[]", "Bytes", "blob", "tinyblob", "mediumblob", "longblob", "binary", "varbinary", "image",
                "raw", "long raw", "bytea");
    }

    private MapeadorTipos() {
    }

    private static void registra(String tipoJava, String metodoJdbc, String... tiposSql) {
        Mapeamento m = new Mapeamento(tipoJava, metodoJdbc);
        for (String tipoSql : tiposSql) {
            TIPOS.put(tipoSql, m);
        }
    }

    /**
     * Normaliza o nome do tipo: minusculas, sem tamanho/precisao ("varchar(50)")
     * e sem o sufixo "identity" do SQL Server ("int identity").
     */
    static String normaliza(String tipoSql) {
        if (tipoSql == null) {
            return "";
        }
        String t = tipoSql.toLowerCase(Locale.ROOT).replaceAll("\\(.*?\\)", " ").trim();
        if (t.endsWith(" identity")) {
            t = t.substring(0, t.length() - " identity".length()).trim();
        }
        return t.replaceAll("\\s+", " ");
    }

    private static Mapeamento busca(String tipoSql) {
        String t = normaliza(tipoSql);
        Mapeamento m = TIPOS.get(t);
        if (m == null && t.endsWith(" unsigned")) {
            m = TIPOS.get(t.substring(0, t.length() - " unsigned".length()));
        }
        if (m == null && t.startsWith("timestamp")) {
            m = TIPOS.get("timestamp"); // ex.: "timestamp with time zone"
        }
        return m == null ? OBJECT : m;
    }

    /** Tipo Java do atributo no DTO (ex.: {@code VARCHAR} vira {@code String}). */
    public static String tipoJava(String tipoSql) {
        return busca(tipoSql).tipoJava;
    }

    /** Sufixo do metodo JDBC (ex.: {@code INT} vira {@code Int}, para {@code setInt}/{@code getInt}). */
    public static String metodoJdbc(String tipoSql) {
        return busca(tipoSql).metodoJdbc;
    }
}
