/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.unach.compilador.sintactico;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mx.unach.compilador.Token;
import mx.unach.compilador.Error;

/**
 *
 * @author javier
 */
public class AnalizadorSintactico {

    private List<Token> listaTokens;
    private List<Error> listaErrores = new ArrayList<>();
    int i = 0;

    public void setListaTokens(List<Token> listaTokens) {
        this.listaTokens = listaTokens;
    }

    public void setListaErrores(List<Error> listaErrores) {
        this.listaErrores = listaErrores;
    }

    public List<Error> getListaErrores() {
        return listaErrores;
    }

    public boolean error = false;

    StringBuilder buffer = new StringBuilder();

    private int estado = 1;

    public void sintactico() {
        estado = 1;
        declaracion(peekToken());
        fila++;
        if (listaTokens.size() > i) {
            sintactico();
        }
    }

    private Token peekToken() {

        if (i < listaTokens.size()) {
            Token aux = listaTokens.get(i);
            i++;
            return aux;
        }

        i++;
        return null;
    }

    boolean c = false;
    private boolean decla = false;
    int fila = 1;

    private boolean declaracion(Token token) {
        if (token != null) {
            if (!token.getValor().equals(";")) {
                switch (estado) {
                    case 1:
                        if (tipoDato(token)) {
                            if (listaTokens.get(i) != null) {
                                if (this.listaTokens.get(i).getValor().equals(";")) {
                                    listaErrores.add(new Error(fila, 1, "Declaracion de variable no valida"));
                                    estado = 100;
                                } else {
                                    estado = 2;
                                }
                            }

                        } else if (token.getValor().equals("ita")) {
                            condicional(token);
                        } else if (token.getValor().equals("dum")) {
                            dum(token);
                        }
                        break;
                    case 2:
                        if (id(token)) {

                            estado = 3;

                        }
                        break;
                    case 3:
                        if (puntoYComa(token)) {
                            return true;
                        } else if (igual(token)) {
                            if (listaTokens.get(i) != null) {
                                if (this.listaTokens.get(i).getValor().equals(";")) {
                                    listaErrores.add(new Error(fila, 0, "Se espera numero o variable para inicializar"));
                                    estado = 100;
                                } else {
                                    estado = 4;
                                }
                            }

                        } else if (token.getValor().equals("(")) {
                            estado = 1;
                            metodos(token);
                        }
                        break;
                    case 4:
                        if (valor(token)) {
                            estado = 3;
                            try {
                                if (listaTokens.size() > i) {
                                    if (!listaTokens.get(i).getNombre().equals("Aritmetico") && !listaTokens.get(i).getValor().equals(";")) {
                                        listaErrores.add(new Error(fila, 0, "Se espera numero o variable para inicializar"));
                                        estado = 100;
                                    }

                                } else {
                                    listaErrores.add(new Error(fila, 0, "Falta punto y Coma"));
                                    estado = 100;
                                }

                            } catch (Exception e) {
                            }

                        } else {
                            listaErrores.add(new Error(fila, 0, "Se espera numero o variable para inicializar"));
                            estado = 100;
                        }
                        break;
                }

                declaracion(peekToken());
            } else {
                fila++;
                if (estado != 100) {
                    decla = true;

                } else {
                    error = true;
                    decla = false;
                }
            }
        }
        return decla;
    }

    private void metodos(Token token) {
        if (token != null) {
            if (!token.getValor().equals(";")) {
                switch (estado) {
                    case 1:
                        if (token.getValor().equals("(")) {
                            estado = 1;
                        } else if (listaTokens.get(i).getValor().equals(";")) {
                            listaErrores.add(new Error(fila, 0, "Error Sintactico, no va punto y coma"));
                            estado = 100;
                        } else {
                            listaErrores.add(new Error(fila, 0, "Error Sintactico, van los parametros"));
                            estado = 100;
                        }
                        break;
                }

                metodos(peekToken());

            } else {
                fila++;
                if (estado == 100) {
                    error = true;
                }
            }
        }
    }

    private void listaParametro(Token token) {
        if (token != null) {
            if (!token.getValor().equals(";")) {

                switch (estado) {
                    case 1:
                        if (token.getValor().equals("(")) {
                            estado = 2;
                        }
                        break;
                    case 2:
                        if (token.getValor().equals(")")) {
                            estado = 3;
                            if (listaTokens.get(i).getValor().equals(";")) {
                                estado = 100;
                                listaErrores.add(new Error(fila, 0, "Abra llaves del metodo"));
                            }

                        } else if (listaTokens.get(i).getValor().equals(";")) {
                            estado = 100;
                            listaErrores.add(new Error(fila, 0, "Abra llaves del metodo"));
                        } else {
                            parametro(token);
                        }
                        break;
                    case 3:
                        if (token.getValor().equals("{")) {
                            estado = 1;
                            listaInstrucciones(token);
                        } else {
                            estado = 100;
                        }
                        break;
                }

                listaParametro(peekToken());
            } else {
                fila++;
                if (estado == 100) {
                    error = true;
                }
            }
        }
    }

    private void listaInstrucciones(Token token) {
        if (token != null) {
            if (!token.getValor().equals(";")) {

                switch (estado) {
                    case 1:
                        if (declaracion(token)) {
                            estado = 1;
                        }
                        break;
                }

                listaInstrucciones(peekToken());
            } else {
                fila++;
                if (estado == 100) {
                    error = true;
                }
            }
        }
    }

    private void condicional(Token token) {
        if (token != null) {
            if (!token.getValor().equals(";")) {

                switch (estado) {
                    case 1:
                        if (token.getValor().equals("ita")) {
                            estado = 2;
                        }
                        break;
                    case 2:
                        if (token.getValor().equals("(")) {
                            estado = 3;
                        } else {
                            estado = 100;
                        }
                        break;
                    case 3:
                        if (id(token) || cifra(token)) {
                            estado = 4;
                        } else {
                            estado = 6;
                            listaErrores.add(new Error(fila, 0, "Condicion no valida"));
                        }
                        break;
                    case 4:
                        if (operl(token)) {
                            estado = 5;
                        } else {
                            listaErrores.add(new Error(fila, 0, "Condicion no valida"));
                        }
                        break;
                    case 5:
                        if (id(token) || cifra(token)) {
                            estado = 6;
                        } else {
                            estado = 6;
                            listaErrores.add(new Error(fila, 0, "Condicion no valida"));
                        }
                        
                        break;
                    case 6:
                        if (token.getValor().equals(")")) {
                            estado = 7;
                            if (listaTokens.get(i).getValor().equals(";")) {
                                
                            }
                        }
                        break;
                    case 7:
                        if (token.getValor().equals("{")) {
                            estado = 8;
                        } else if (listaTokens.get(i).getValor().equals(";")) {
                            listaErrores.add(new Error(fila, 0, "Faltan las llaves"));
                        }
                        break;
                    case 8:
                        estado = 1;
                        if (tipoDato(token)) {
                            declaracion(token);
                        }

                        estado = 9;
                        break;
                    case 9:
                        if (token.getValor().equals("}")) {
                            estado = 1;
                        }
                        break;
                }

                condicional(peekToken());
            } else {
                fila++;
                if (estado == 100) {
                    error = true;
                } 
            }
        }
    }

    private void parametro(Token token) {
        if (token != null) {
            if (!token.getValor().equals(";")) {

                switch (estado) {
                    case 1:
                        if (tipoDato(token)) {
                            estado = 2;
                        } else if (id(token)) {
                            listaErrores.add(new Error(fila, 0, "Mal inicializacion de parametro"));
                        }
                        break;

                    case 2:
                        if (id(token)) {
                            estado = 3;
                        } else if (tipoDato(token)) {
                            listaErrores.add(new Error(fila, 0, "Mal inicializacion de parametro"));
                        }
                        break;
                        
                    case 3:
                        if (token.getValor().equals(",")) {
                            estado = 1;
                        } else if (token.getValor().equals(")")) {
                            estado = 1;
                            if (listaTokens.get(i).getValor().equals(";")) {
                                listaErrores.add(new Error(fila, 0, "Faltan las llaves"));
                            }
                            listaParametro(token);

                        } else if (tipoDato(token)) {
                            listaErrores.add(new Error(fila, 0, "Separe los parametros con la (,)"));
                        } else if (id(token)) {
                            listaErrores.add(new Error(fila, 0, "Separe los parametros con la (,)"));
                        }

                        break;
                }

                parametro(peekToken());
            } else {
                fila++;
            }
        }

    }

    private void dum(Token token) {
        if (token != null) {
            if (!token.getValor().equals(";")) {
                switch (estado) {
                    case 1:
                        if (token.getValor().equals("dum")) {
                            estado = 2;
                        }
                        break;
                    case 2:
                        if (token.getValor().equals("(")) {
                            estado = 3;
                        }
                        break;
                    case 3:
                        if (id(token) || cifra(token)) {
                            estado = 4;
                        } else {
                            estado = 100;
                        }
                        break;
                    case 4:
                        if (operl(token)) {
                            estado = 5;
                        } else {
                            estado = 100;
                        }

                        break;
                    case 5:
                        if (id(token) || cifra(token)) {
                            estado = 6;
                        } else {

                            estado = 100;
                        }
                        break;
                    case 6:
                        if (token.getValor().equals(")")) {
                            estado = 7;
                        } else {
                            estado = 100;
                        }
                        break;
                    case 7:
                        if (token.getValor().equals("{")) {
                            estado = 8;
                        } else {
                            estado = 100;
                        }
                        break;
                    case 8:
                        estado = 1;
                        if (tipoDato(token)) {
                            declaracion(token);
                            estado = 9;
                        } else {
                            estado = 100;
                        }

                        break;
                    case 9:
                        if (token.getValor().equals("}")) {
                            estado = 1;
                        }
                        break;
                }

                dum(peekToken());
            } else {
                fila++;
                if (estado == 100) {
                    error = true;
                } else {
                }
            }
        }
    }

    private void operadores(Token token) {
        if (token.getValor().equals(";")) {

            switch (estado) {

            }

            operadores(peekToken());
        }
    }

    private boolean id(Token token) {
        if (token.getNombre().equals("Identificador")) {
            return true;
        } else {
            return false;
        }
    }

    private boolean cifra(Token token) {
        if (token.getNombre().equals("Cifra")) {
            return true;
        }
        return false;
    }

    List<String> operl = Arrays.asList(new String("<"), new String(">"), new String("=="), new String("<="), new String(">="));

    private boolean operl(Token token) {
        for (String op : operl) {
            if (token.getValor().equals(op)) {
                return true;
            }
        }
        return false;
    }

    private boolean igual(Token token) {
        if (token.getValor().equals("=")) {
            return true;
        } else {
            return false;
        }
    }

    List<String> tipos = Arrays.asList(new String("supernatet"),
            new String("totum"),
            new String("breve"),
            new String("char"),
            new String("long"),
            new String("duplici"),
            new String("catena"),
            new String("vacuum"));

    private boolean tipoDato(Token token) {
        for (String t : tipos) {
            if (token.getValor().equals(t)) {
                return true;
            }
        }
        return false;
    }

    private boolean valor(Token token) {
        if (token.getNombre().equals("Cifra")) {
            return true;
        }
        return false;
    }

    private boolean puntoYComa(Token token) {
        if (token.getValor().equals(";")) {
            return true;
        }
        return false;
    }
}