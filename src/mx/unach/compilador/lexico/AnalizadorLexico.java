/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.unach.compilador.lexico;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import mx.unach.compilador.Token;
import mx.unach.vista.Principal;
import mx.unach.compilador.Error;

/**
 *
 * @author javier
 */
public class AnalizadorLexico {

    private Queue buffer;
    private List<Character> aux;
    private Integer estado;
    private Character caracter;
    private StringBuilder identificador;
    private StringBuilder cifra;
    private Hashtable tablaSimbolos;
    private Hashtable tablaDelimitadores;
    private Hashtable tablaAritmeticos;
    private Integer linea;
    private Integer columna;

    private List<Token> listaTokens;
    private List<Error> listaErrores;

    // EXPRESION REGULAR PARA Exponecial
    Pattern patNumeros = Pattern.compile("^([-]||[+]){0,1}[0-9]{1,}$");
    Pattern patExponencial = Pattern.compile("^[a-z]||[ABCDFJHIJKLMNOPQRSTUVWXYZ]$");
    Pattern patOperadoresAritmeticos = Pattern.compile("^[*+/-]$");
    Pattern patOperadoresLogicos = Pattern.compile("^[<=>]$");
    Pattern patDelimitadores = Pattern.compile("^[\\(\\)\\[\\]\\{\\};,\\.\"]$");
    Pattern patPuntoYComa = Pattern.compile("^[^;]+$");
    Matcher matNumeros, matExponencial, matAritmeticos, matLogicos, matDelimitadores, matPuntoYComa;

    private Principal principal;

    public AnalizadorLexico(Principal principal) {
        this.buffer = new LinkedList();
        this.listaTokens = new ArrayList<>();
        this.listaErrores = new ArrayList<>();
        this.estado = 0;
        this.caracter = ' ';
        this.tablaSimbolos = new Hashtable();
        this.tablaDelimitadores = new Hashtable();
        this.tablaAritmeticos = new Hashtable();
        this.principal = principal;
        this.linea = 1;
        this.columna = 0;

        reservarTablaSimbolos(new Token("Palabra Reservada", "ita"));
        reservarTablaSimbolos(new Token("Palabra Reservada", "aliter"));
        reservarTablaSimbolos(new Token("Palabra Reservada", "vacuum"));
        reservarTablaSimbolos(new Token("Palabra Reservada", "reduc"));
        reservarTablaSimbolos(new Token("Palabra Reservada", "catena"));
        reservarTablaSimbolos(new Token("Palabra Reservada", "magnitudinem"));
        reservarTablaSimbolos(new Token("Palabra Reservada", "principalem"));
        reservarTablaSimbolos(new Token("Palabra Reservada", "confractus"));
        reservarTablaSimbolos(new Token("Palabra Reservada", "static"));
        reservarTablaSimbolos(new Token("Palabra Reservada", "mutatio"));
        reservarTablaSimbolos(new Token("Palabra Reservada", "casus"));
        reservarTablaSimbolos(new Token("Palabra Reservada", "defaltam"));
        reservarTablaSimbolos(new Token("Palabra Reservada", "supernatet"));
        reservarTablaSimbolos(new Token("Palabra Reservada", "totum"));
        reservarTablaSimbolos(new Token("Palabra Reservada", "breve"));
        reservarTablaSimbolos(new Token("Palabra Reservada", "structure"));
        reservarTablaSimbolos(new Token("Palabra Reservada", "typedef"));
        reservarTablaSimbolos(new Token("Palabra Reservada", "char"));
        reservarTablaSimbolos(new Token("Palabra Reservada", "facio"));
        reservarTablaSimbolos(new Token("Palabra Reservada", "enim"));
        reservarTablaSimbolos(new Token("Palabra Reservada", "long"));
        reservarTablaSimbolos(new Token("Palabra Reservada", "duplici"));
        reservarTablaSimbolos(new Token("Palabra Reservada", "dum"));

        reservarTablaDeliminatores(new Token("Delimitador", "("));
        reservarTablaDeliminatores(new Token("Delimitador", ")"));
        reservarTablaDeliminatores(new Token("Delimitador", "{"));
        reservarTablaDeliminatores(new Token("Delimitador", "}"));
        reservarTablaDeliminatores(new Token("Delimitador", "["));
        reservarTablaDeliminatores(new Token("Delimitador", "]"));
        reservarTablaDeliminatores(new Token("Delimitador", ";"));
        reservarTablaDeliminatores(new Token("Delimitador", ","));
        reservarTablaDeliminatores(new Token("Delimitador", "."));
        reservarTablaDeliminatores(new Token("Delimitador", "\""));

        reservarTablaAritmeticos(new Token("Aritmetico", "+"));
        reservarTablaAritmeticos(new Token("Aritmetico", "-"));
        reservarTablaAritmeticos(new Token("Aritmetico", "/"));
        reservarTablaAritmeticos(new Token("Aritmetico", "*"));

    }

    private void reservarTablaSimbolos(Token token) {
        tablaSimbolos.put(token.getValor(), token);
    }

    private void reservarTablaDeliminatores(Token token) {
        tablaDelimitadores.put(token.getValor(), token);
    }

    private void reservarTablaAritmeticos(Token token) {
        tablaAritmeticos.put(token.getValor(), token);
    }

    public Queue getBuffer() {
        return buffer;
    }

    public void setBuffer(String texto) {

        for (int i = 0; i < texto.length(); i++) {
            buffer.add(texto.charAt(i));
        }
        buffer.add(Character.MIN_VALUE);
    }

    private char siguienteCaracter() {
        return (Character) buffer.peek();
    }

    private void removerCaracter() {
        buffer.remove();
    }

    private Integer fallo(Integer estado) {

        Integer estadoRetorno = 0;

        switch (estado) {
            case 0:
                estadoRetorno = 9;
                break;
            case 11:
                estadoRetorno = 12;
                break;
        }

        return estadoRetorno;
    }

    public void lexico() {
        while (!buffer.isEmpty()) {
            caracter = siguienteCaracter();
            matNumeros = patNumeros.matcher(caracter.toString());
            matLogicos = patOperadoresLogicos.matcher(caracter.toString());
            matAritmeticos = patOperadoresAritmeticos.matcher(caracter.toString());
            matDelimitadores = patDelimitadores.matcher(caracter.toString());
            Queue aux = buffer;
            if (matNumeros.matches()) {
                listaTokens.add(numeroDecimal());
            } else if (matLogicos.matches()) {
                listaTokens.add(operadores());
            } else if (Character.isLetter(caracter)) {
                listaTokens.add(pr_y_id());
            } else if (caracter == '\0') {
                removerCaracter();
            } else if (caracter == ' ') {
                columna++;
                removerCaracter();
            } else if (caracter == '\t') {
                columna+=3;
                removerCaracter();
            } else if (matAritmeticos.matches()) {
                listaTokens.add(operadoresAritmeticos());
            } else if (matDelimitadores.matches()) {
                listaTokens.add(delimitadores());
            } else if (caracter == '\n') {
                linea++;
                removerCaracter();
            }
            estado = 0;
            columna = 0;
        }
    }

    public Token numeroDecimal() {
        while (!buffer.isEmpty()) {
            switch (estado) {
                case 0:
                    caracter = siguienteCaracter();
                    cifra = new StringBuilder();
                    if (caracter == '-') {
                        cifra.append(caracter);
                        removerCaracter();
                        estado = 2;
                    } else if (Character.isDigit(caracter)) {
                        cifra.append(caracter);
                        removerCaracter();
                        estado = 1;
                    } else if (caracter == '+') {
                        estado = 3;
                        removerCaracter();
                    }
                    break;
                case 1:
                    caracter = siguienteCaracter();
                    matAritmeticos = patOperadoresAritmeticos.matcher(caracter.toString());
                    matLogicos = patOperadoresLogicos.matcher(caracter.toString());
                    matExponencial = patExponencial.matcher(caracter.toString());
                    matPuntoYComa = patPuntoYComa.matcher(caracter.toString());
                    if (Character.isDigit(caracter)) {
                        cifra.append(caracter);
                        removerCaracter();
                        estado = 1;
                    } else if (caracter == '.') {
                        cifra.append(caracter);
                        removerCaracter();
                        estado = 4;
                    } else if (matExponencial.matches()) {
                        removerCaracter();
                        estado = 16;
                    } else if (matAritmeticos.matches()) {
                        estado = 10;
                    } else if (matLogicos.matches()) {
                        estado = 10;
                    } else if (caracter == '\0') {
                        estado = 12;
                    } else if (caracter == ' ' || caracter == '\n') {
                        estado = 10;
                    } else if (caracter == 'E') {
                        cifra.append(caracter);
                        removerCaracter();
                        estado = 6;
                    } else if (!matPuntoYComa.matches()) {
                        estado = 9;
                    } else {
                        estado = 16;
                    }
                    break;
                case 2:
                    caracter = siguienteCaracter();
                    if (Character.isDigit(caracter)) {
                        cifra.append(caracter.toString());
                        removerCaracter();
                        estado = 1;
                    } else {
                        estado = 11;
                    }
                    break;
                case 3:
                    caracter = siguienteCaracter();
                    if (Character.isDigit(caracter)) {
                        cifra.append(caracter.toString());
                        removerCaracter();
                        estado = 1;
                    } else {
                        estado = 12;
                    }
                    break;
                case 4:
                    caracter = siguienteCaracter();

                    if (Character.isDigit(caracter)) {
                        cifra.append(caracter);
                        removerCaracter();
                        estado = 5;
                    } else if (caracter == ' ' || caracter == '\n') {
                        estado = 13;
                    } else {
                        removerCaracter();
                        estado = 16;
                    }
                    break;
                case 5:
                    caracter = siguienteCaracter();
                    matLogicos = patOperadoresLogicos.matcher(caracter.toString());
                    matPuntoYComa = patPuntoYComa.matcher(caracter.toString());
                    if (Character.isDigit(caracter)) {
                        cifra.append(caracter);
                        removerCaracter();
                        estado = 5;
                    } else if (caracter == 'E') {
                        cifra.append(caracter);
                        removerCaracter();
                        estado = 6;
                    } else if (caracter == ' ' || caracter == '\0') {
                        estado = 14;
                    } else if (caracter == '\n') {
                        linea++;
                        estado = 14;
                    } else if (matLogicos.matches()) {
                        estado = 9;
                    } else if (!matPuntoYComa.matches()) {
                        estado = 9;
                    } else {
                        removerCaracter();
                        estado = 16;
                    }
                    break;
                case 6:
                    caracter = siguienteCaracter();
                    matExponencial = patExponencial.matcher(caracter.toString());
                    if (caracter == '+' || caracter == '-') {
                        cifra.append(caracter);
                        removerCaracter();
                        estado = 7;
                    } else if (Character.isDigit(caracter)) {
                        cifra.append(caracter);
                        removerCaracter();
                        estado = 8;
                    } else if (caracter == ' ' || caracter == '\n') {
                        linea++;
                        removerCaracter();
                        estado = 15;
                    } else if (caracter == '\0') {
                        estado = 19;
                    } else {
                        removerCaracter();
                        estado = 16;
                    }
                    break;
                case 7:
                    caracter = siguienteCaracter();
                    if (Character.isDigit(caracter)) {
                        cifra.append(caracter);
                        removerCaracter();
                        estado = 8;
                    } else {
                        removerCaracter();
                        estado = 16;
                    }
                    break;
                case 8:
                    caracter = siguienteCaracter();
                    if (Character.isDigit(caracter)) {
                        cifra.append(caracter);
                        removerCaracter();
                        estado = 8;
                    } else {
                        estado = 9;
                    }
                    break;
                case 9:
                    estado = 0;
                    return new Token("Cifra", cifra.toString());
                case 10:
                    estado = 0;
                    return new Token("Cifra", cifra.toString());
                case 11:
                    estado = 0;
                    return new Token("Cifra", cifra.toString());
                case 12:
                    estado = 0;
                    return new Token("Cifra", cifra.toString());
                case 13:
                    estado = 0;
                    listaErrores.add(new Error(1, columna, "Digito no valido"));
                    return null;
                case 14:
                    estado = 0;
                    return new Token("Cifra", cifra.toString());
                case 15:
                    estado = 0;
                    return new Token("Cifra", cifra.toString());
                case 16:
                    caracter = siguienteCaracter();

                    matLogicos = patOperadoresLogicos.matcher(caracter.toString());
                    if (caracter == ' ' || caracter == '\n' || caracter == '\0') {
                        estado = 17;
                    } else {
                        estado = 16;
                        removerCaracter();
                    }
                    break;
                case 17:
                    listaErrores.add(new Error(linea, columna, "Digito no valido"));
                    return null;

            }
            columna++;
        }
        return null;
    }

    public Token operadores() {
        while (!buffer.isEmpty()) {
            switch (estado) {
                case 0:
                    caracter = siguienteCaracter();
                    if (caracter == '<') {
                        estado = 1;
                        removerCaracter();
                    } else if (caracter == '=') {
                        estado = 5;
                        removerCaracter();
                    } else if (caracter == '>') {
                        estado = 6;
                        removerCaracter();
                    } else if (Character.isDigit(caracter)) {
                        estado = 17;
                    } else {
                        estado = fallo(0);
                    }
                    break;
                case 1:
                    caracter = siguienteCaracter();
                    if (caracter == '=') {
                        estado = 2;
                        removerCaracter();
                    } else if (caracter == '>') {
                        estado = 3;
                        removerCaracter();
                    } else if (caracter == '<') {
                        estado = 9;
                        removerCaracter();
                    } else {
                        estado = 4;
                    }
                    break;
                case 2:
                    return new Token("Operador", "<=");

                case 3:
                    return new Token("Operador", "<>");

                case 4:
                    return new Token("Operador", "<");

                case 5:
                    caracter = siguienteCaracter();
                    if (caracter == '=') {
                        removerCaracter();
                        return new Token("Operador", "==");
                    } else {

                        return new Token("Operador", "=");
                    }

                case 6:
                    caracter = siguienteCaracter();
                    if (caracter == '=') {
                        estado = 7;
                    } else if (caracter == '<' || caracter == '>') {
                        estado = 10;
                        removerCaracter();
                    } else {
                        estado = 8;
                    }
                    break;
                case 7:
                    return new Token("Operador", ">=");
                case 8:
                    return new Token("Operador", ">");
                case 9:
                    listaErrores.add(new Error(linea, columna, "Operador no valido"));
                    return null;
                case 10:
                    listaErrores.add(new Error(linea, columna, "Operador no valido"));
                    return null;
            }
            columna++;
        }
        return null;
    }

    public Token pr_y_id() {
        while (!buffer.isEmpty()) {
            switch (estado) {
                case 0:
                    caracter = siguienteCaracter();
                    if (Character.isLetter(caracter)) {
                        estado = 1;
                        identificador = new StringBuilder();
                        identificador.append(caracter);
                        removerCaracter();
                    } else {
                        estado = 13;
                    }
                    break;
                case 1:
                    caracter = siguienteCaracter();
                    if (Character.isLetterOrDigit(caracter)) {
                        estado = 1;
                        identificador.append(caracter);
                        removerCaracter();
                    } else {
                        estado = 2;
                    }
                    break;
                case 2:
                    String palabraReservada = identificador.toString();
                    Token tokenPalabraReservada = (Token) tablaSimbolos.get(palabraReservada);

                    if (tokenPalabraReservada != null) {
                        estado = 0;
                        return tokenPalabraReservada;

                    }

                    tablaSimbolos.put(palabraReservada, new Token("Identificador", palabraReservada));
                    estado = 0;
                    return new Token("Identificador", identificador.toString());

            }
        }
        return null;
    }

    public Token operadoresAritmeticos() {

        while (!buffer.isEmpty()) {
            switch (estado) {
                case 0:
                    caracter = siguienteCaracter();
                    if (caracter == '+') {
                        estado = 2;
                    } else if (caracter == '-') {
                        estado = 1;
                    } else if (caracter == '*') {
                        estado = 3;
                        removerCaracter();
                    } else if (caracter == '/') {
                        estado = 4;
                        removerCaracter();
                    }
                    break;
                case 1:
                    aux = (List) buffer;
                    Character n1 = aux.get(1);
                    if (Character.isDigit(n1)) {
                        estado = 0;
                        return numeroDecimal();
                    } else {
                        removerCaracter();
                        return new Token("Aritmeticos", "-");
                    }
                case 2:
                    aux = (List) buffer;
                    Character n2 = aux.get(1);
                    if (Character.isDigit(n2)) {
                        estado = 0;
                        return numeroDecimal();
                    } else {
                        removerCaracter();
                        return new Token("Aritmeticos", "+");
                    }

                case 3:
                    return new Token("Aritmeticos", "*");
                case 4:
                    return new Token("Aritmeticos", "/");

            }
        }
        return null;
    }

    public Token delimitadores() {
        while (!buffer.isEmpty()) {
            switch (estado) {
                case 0:
                    caracter = siguienteCaracter();
                    Token tokenPalabraDelimitadora = (Token) tablaDelimitadores.get(caracter.toString());
                    if (tokenPalabraDelimitadora != null) {
                        removerCaracter();
                        return tokenPalabraDelimitadora;
                    }
            }
        }

        return null;
    }

    public List<Token> siguienteToken() {

        while (!buffer.isEmpty()) {
            switch (estado) {
                case 0:
                    caracter = siguienteCaracter();
                    if (caracter == '<') {
                        estado = 1;
                        removerCaracter();
                    } else if (caracter == '=') {
                        estado = 5;
                    } else if (caracter == '>') {
                        estado = 6;
                        removerCaracter();
                    } else if (Character.isDigit(caracter)) {
                        estado = 17;
                    } else {
                        estado = fallo(0);
                    }
                    break;
                case 1:
                    caracter = siguienteCaracter();
                    if (caracter == '=') {
                        estado = 2;
                        removerCaracter();
                    } else if (caracter == '>') {
                        estado = 3;
                        removerCaracter();
                    } else {
                        estado = 4;
                    }
                    break;
                case 2:
                    listaTokens.add(new Token("Operador", "<="));
                    estado = 0;
                    break;

                case 3:
                    listaTokens.add(new Token("Operador", "<>"));
                    estado = 0;
                    break;

                case 4:
                    listaTokens.add(new Token("Operador", "<"));
                    estado = 0;
                    break;

                case 5:
                    listaTokens.add(new Token("Operador", "="));
                    removerCaracter();
                    estado = 0;
                    break;

                case 6:
                    caracter = siguienteCaracter();
                    if (caracter == '=') {
                        estado = 7;
                    } else {
                        estado = 8;
                    }
                    break;
                case 7:
                    listaTokens.add(new Token("Operador", ">="));
                    removerCaracter();
                    estado = 0;
                    break;
                case 8:
                    listaTokens.add(new Token("Operador", ">"));
                    estado = 0;
                    break;
                case 9:
                    caracter = siguienteCaracter();
                    if (Character.isLetter(caracter)) {
                        estado = 10;
                        identificador = new StringBuilder();
                        identificador.append(caracter);
                        removerCaracter();
                    } else if (Character.isWhitespace(caracter)) {
                        estado = fallo(11);
                    } else {
                        estado = 13;
                    }
                    break;
                case 10:
                    caracter = siguienteCaracter();
                    if (Character.isLetterOrDigit(caracter)) {
                        estado = 10;
                        identificador.append(caracter);
                        removerCaracter();
                    } else {
                        estado = 11;
                    }
                    break;
                case 11:
                    String palabraReservada = identificador.toString();
                    Token tokenPalabraReservada = (Token) tablaSimbolos.get(palabraReservada);

                    if (tokenPalabraReservada != null) {
                        listaTokens.add(tokenPalabraReservada);
                        estado = 0;
                        break;
                    }
                    if (caracter == ' ') {
                        estado = 12;
                    }

                    tablaSimbolos.put(palabraReservada, new Token("Identificador", palabraReservada));

                    listaTokens.add(new Token("Identificador", identificador.toString()));

                    estado = 0;

                    break;
                case 12:
                    caracter = siguienteCaracter();
                    if (caracter == '\n') {
                        linea++;
                    }
                    columna = 0;
                    removerCaracter();
                    estado = 0;
                    break;
                case 13:
                    caracter = siguienteCaracter();
                    Token tokenPalabraDelimitadora = (Token) tablaDelimitadores.get(caracter.toString());
                    if (tokenPalabraDelimitadora != null) {
                        listaTokens.add(tokenPalabraDelimitadora);
                    } else {
                        estado = 14;
                        break;
                    }
                    estado = 0;
                    removerCaracter();
                    break;

                case 14:
                    Token tokenSimboloAritmetico = (Token) tablaAritmeticos.get(caracter.toString());
                    if (tokenSimboloAritmetico != null) {
                        listaTokens.add(tokenSimboloAritmetico);
                    } else if (Character.isDigit(caracter)) {
                        estado = 15;
                        break;
                    }

                    estado = 0;
                    removerCaracter();
                    break;
                case 15:
                    caracter = siguienteCaracter();
                    estado = 16;
                    cifra = new StringBuilder();
                    cifra.append(caracter);
                    removerCaracter();
                    break;

                case 16:
                    caracter = siguienteCaracter();
                    cifra.append(caracter);
                    removerCaracter();
                    caracter = siguienteCaracter();
                    if (!Character.isDigit(caracter)) {
                        estado = 17;
                    }
                    break;
                case 17:
                    caracter = siguienteCaracter();
                    if (Character.isDigit(caracter)) {
                        cifra = new StringBuilder();
                        cifra.append(caracter);
                        removerCaracter();
                        estado = 18;
                    }
                    break;
                case 18:
                    caracter = siguienteCaracter();
                    if (Character.isDigit(caracter)) {
                        cifra.append(caracter);
                        removerCaracter();
                        estado = 18;
                    } else if (caracter == '.') {
                        cifra.append(caracter);
                        removerCaracter();
                        estado = 19;
                    } else if (caracter == 'E') {
                        cifra.append(caracter);
                        removerCaracter();
                        estado = 21;
                    } else if (Character.isLetter(caracter)) {
                        listaTokens.add(new Token("Cifra", cifra.toString()));
                        estado = fallo(0);
                    } else {
                        estado = 25;
                    }
                    break;
                case 19:
                    caracter = siguienteCaracter();
                    if (Character.isDigit(caracter)) {
                        cifra.append(caracter);
                        removerCaracter();
                        estado = 20;
                    } else {
                        estado = 24;
                    }
                    break;
                case 20:
                    caracter = siguienteCaracter();
                    if (Character.isDigit(caracter)) {
                        cifra.append(caracter);
                        removerCaracter();
                        estado = 20;
                    } else if (caracter == 'E') {
                        cifra.append(caracter);
                        removerCaracter();
                        estado = 21;
                    } else if (caracter == '.') {
                        listaErrores.add(new Error(linea + 1, columna, "Numero no valido"));
                        estado = 26;
                    } else {
                        estado = 26;
                    }
                    break;
                case 21:
                    caracter = siguienteCaracter();
                    if (caracter == '-' || caracter == '+') {
                        cifra.append(caracter);
                        removerCaracter();
                        estado = 22;
                    } else if (Character.isDigit(caracter)) {
                        cifra.append(caracter);
                        removerCaracter();
                        estado = 23;
                    }
                    break;
                case 22:
                    caracter = siguienteCaracter();
                    if (Character.isDigit(caracter)) {
                        cifra.append(caracter);
                        removerCaracter();
                        estado = 23;
                    }
                    break;
                case 23:
                    caracter = siguienteCaracter();
                    if (Character.isDigit(caracter)) {
                        cifra.append(caracter);
                        removerCaracter();
                        estado = 23;
                    } else {
                        estado = 24;
                    }
                    break;
                case 24:
                    listaTokens.add(new Token("Cifra", cifra.toString() + "0"));
                    estado = 0;
                    break;
                case 25:
                    listaTokens.add(new Token("Cifra", cifra.toString()));
                    estado = 0;
                    break;
                case 26:
                    listaTokens.add(new Token("Cifra", cifra.toString() + "0"));
                    estado = 0;
                    break;
                case 27:
                    caracter = siguienteCaracter();
                    if (caracter == '.') {
                        listaTokens.add(new Token("Delimitador", caracter.toString()));
                        removerCaracter();
                        estado = 27;
                    } else if (Character.isDigit(caracter)) {
                        estado = 17;
                    } else {
                        estado = 18;
                        removerCaracter();
                    }
                    break;

            }
            columna++;
        }

        return listaTokens;

    }

    public List<Error> getListaErrores() {
        return listaErrores;
    }

    public List<Token> getListaTokens() {
        return listaTokens;
    }
}
