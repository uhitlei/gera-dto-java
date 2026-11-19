package com.java.geraDTO.gerador;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MapeadorTiposTest {

    private static void confere(String tipoSql, String tipoJava, String metodoJdbc) {
        assertEquals("tipo Java de " + tipoSql, tipoJava, MapeadorTipos.tipoJava(tipoSql));
        assertEquals("metodo JDBC de " + tipoSql, metodoJdbc, MapeadorTipos.metodoJdbc(tipoSql));
    }

    @Test
    public void inteiros() {
        confere("INT", "Integer", "Int");
        confere("integer", "Integer", "Int");
        confere("SMALLINT", "Integer", "Int");
        confere("TINYINT", "Integer", "Int");
    }

    @Test
    public void inteirosGrandesEUnsignedNaoEstouramInteger() {
        confere("BIGINT", "Long", "Long");
        confere("INT UNSIGNED", "Long", "Long");
        confere("BIGINT UNSIGNED", "java.math.BigDecimal", "BigDecimal");
        confere("SMALLINT UNSIGNED", "Integer", "Int");
    }

    @Test
    public void decimaisNaoViramInteger() {
        confere("NUMERIC", "java.math.BigDecimal", "BigDecimal");
        confere("DECIMAL", "java.math.BigDecimal", "BigDecimal");
        confere("NUMBER", "java.math.BigDecimal", "BigDecimal");
        confere("money", "java.math.BigDecimal", "BigDecimal");
        confere("DOUBLE", "Double", "Double");
        confere("FLOAT", "Double", "Double");
    }

    @Test
    public void textos() {
        confere("VARCHAR", "String", "String");
        confere("CHAR", "String", "String");
        confere("TEXT", "String", "String");
        confere("VARCHAR2", "String", "String");
        confere("NVARCHAR", "String", "String");
    }

    @Test
    public void datasEHoras() {
        confere("DATE", "java.sql.Date", "Date");
        confere("DATETIME", "java.sql.Timestamp", "Timestamp");
        confere("TIMESTAMP", "java.sql.Timestamp", "Timestamp");
        confere("timestamp with time zone", "java.sql.Timestamp", "Timestamp");
        confere("TIME", "java.sql.Time", "Time");
    }

    @Test
    public void booleanosEBinarios() {
        confere("BIT", "Boolean", "Boolean");
        confere("BOOLEAN", "Boolean", "Boolean");
        confere("BLOB", "byte[]", "Bytes");
        confere("VARBINARY", "byte[]", "Bytes");
    }

    @Test
    public void normalizaTamanhoIdentityEspacos() {
        confere("varchar(50)", "String", "String");
        confere("int identity", "Integer", "Int");
        confere("  Decimal(10,2)  ", "java.math.BigDecimal", "BigDecimal");
        assertEquals("int unsigned", MapeadorTipos.normaliza("INT   UNSIGNED"));
    }

    @Test
    public void tipoDesconhecidoViraObject() {
        confere("GEOMETRY", "Object", "Object");
        confere(null, "Object", "Object");
        confere("", "Object", "Object");
    }
}
