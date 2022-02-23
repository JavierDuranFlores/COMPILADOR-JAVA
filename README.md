<!-- headings-->
# **_Compilador en Java_**

## **_Por el momento solo tiene la parte del analizador lexico._**

### **TABLA DE PALABRAS RESERVAS**
#### Las palabras reservas son las de Java, pero traducidas al latín.

|Componente Lexico | Lexema(Latín) | Lexema(Original) |
|------------------|---------------|------------------|
|Palabra Reservada | ita           |    if            |
|Palabra Reservada | aliter        |    else          |
|Palabra Reservada | vacuum        |    void          |
|Palabra Reservada | reduc         |    return        |
|Palabra Reservada | magnitudinem  |    sizeof        |
|Palabra Reservada | confractus    |    break         |
|Palabra Reservada | static        |    static        |
|Palabra Reservada | mutatio       |    switch        |
|Palabra Reservada | casus         |    case          |
|Palabra Reservada | defaltam      |    default       |
|Palabra Reservada | supernatet    |    float         |
|Palabra Reservada | totum         |    int           |
|Palabra Reservada | breve         |    short         |
|Palabra Reservada | structure     |    struct        |
|Palabra Reservada | typedef       |    typedef       |
|Palabra Reservada | char          |    char          |
|Palabra Reservada | enim          |    for           |
|Palabra Reservada | long          |    long          |
|Palabra Reservada | duplici       |    double        |
|Palabra Reservada | dum           |    while         |


#

#### Para cambiar la tabla de palabras reservadas por las tuyas, simplemente ve al constructor de AnalizadorLexico y encontradas un metodo llamado reservarTablaSimbolos(token) ahi  le cambias. EJEMPLO
#
#### _Esta es mi palabra reservada que es la traducion al latin de if._
```java
reservarTablaSimbolos(new Token("Palabra Reservada", "ita"));
```
#### _Ya la cambias por la que tuya._
```java
reservarTablaSimbolos(new Token("Palabra Reservada", "si"));
```
## **Automatas Usados**
### Para identificar los operadores se usa este automata.

![Automata-Operadores](/markdown/img/automata-operadores.png)

### Este seria el codigo en Java del automata
```java
public List<Token> siguienteToken() {
        while (!buffer.isEmpty()) {
            switch (estado) {
                case 0:
                    // Se pide un caracter de la cola
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
            }
        }
        return listaTokens;
    }
```
### Para identificar las palabras reservas y los identificadores se usa este automata.

![palabras-reservadas-y-identificadores](/markdown/img/automata-pr-y-id.png)

### Este seria el codigo en Java del automata
```java
public List<Token> siguienteToken() {
    while (!buffer.isEmpty()) {
        switch (estado) {
            case 9:
                caracter = siguienteCaracter();
                if (Character.isLetter(caracter)) {
                    estado = 10;
                    // Se crea variable de StringBuilder para guardar las palabras que van a formar el identificador.
                    identificador = new StringBuilder();
                    // Vamos agregando al identificador cada letra.
                    identificador.append(caracter);
                    // Se remueve de la Cola el caracter.
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
        }
    }
}
```
### Para identificar numeros se usa este automata.
![numeros-sin-signo](/markdown/img/automata-sin-signo.png)
### Este seria el codigo en Java del automata
```java
public List<Token> siguienteToken() {
    while(!buffer.isEmpty()) {
        switch (estado) {
            case 15:
                caracter = siguienteCaracter();
                estado = 16;
                // Se crea un StringBuilder en donde se va aguardar el numero que se va a ir formando siguiente las reglas del automata
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
                    listaTokens.add(new Token("Delimitadore", caracter.toString()));
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
    }
    return listaTokens;
}
```