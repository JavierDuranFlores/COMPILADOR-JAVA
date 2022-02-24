/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.unach.vista;

import mx.unach.vista.utilerias.NumeroLinea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.xml.bind.Marshaller;
import mx.unach.compilador.Token;
import mx.unach.compilador.lexico.AnalizadorLexico;
import mx.unach.vista.utilerias.MiRenderer;

/**
 *
 * @author javier
 */
public class Principal {

    private JFrame ventana;
    private JPanel panel;

    private JPanel cabecera;
    private JPanel contendio;
    private JPanel footer;

    private JPanel panelBotonArchivo;
    private JButton botonArchivo;
    private JButton botonNuevo;
    private JButton botonGuardar;
    
    private JLabel etiquetaAbrir;
    private JLabel etiquetaNuevo;
    private JLabel etiquetaGuardar;

    private JScrollPane jsp;
    private JTextArea editor;
    private JTable tablaTokens,
            tablaErrores;
    private JPanel panelTablaTokens;
    private String columnasTT[] = {"Nombre Token", "Valor Token"};
    private DefaultTableModel dtmTT = new DefaultTableModel(null, columnasTT);
    private String columnasTE[] = {"Linea", "Columana" , "Descripcion"};
    private DefaultTableModel dtmTE = new DefaultTableModel(null, columnasTE);

    // Footer
    private NumeroLinea nm;
    private JPanel panelTablaErrores,
            panelBotones,
            panelBotonLimpiar;
    private JTable tableErrores;
    private JButton botonAnalisisLexico;
    private JButton botonAnalisisSintactico;
    private JButton botonAnalisisSemantico;
    private JButton limpiarEditor;

    private FileInputStream entrada;
    private FileOutputStream salida;
    private File archivo;
    private JFileChooser seleccionado = new JFileChooser();
    
    // Color fondo
    private Color fondo = new Color(53, 59, 72);
    private Color fondoBackBoton = new Color(64, 115, 158);
    private Color fondoForeBoton = new Color(220, 221, 225);

    public void iniciarComponentes() {

        agregarVentana();
        panelPrincipal();
        panelCabecera();
        panelContenido();
        panelFooter();

        componentesCabecera();
        componentesContenido();
        componentesFooter();
        
        ventana.setResizable(false);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setVisible(true);

    }

    private void agregarVentana() {

        ventana = new JFrame();
        ventana.setSize(700, 530);
        ventana.setLocationRelativeTo(null);
    }

    private void panelPrincipal() {
        panel = new JPanel(new BorderLayout());
        ventana.add(panel);
    }

    private void panelCabecera() {
        cabecera = new JPanel(new BorderLayout());
        cabecera.setPreferredSize(new Dimension(700, 30));
        cabecera.setBackground(fondo);
        panel.add(cabecera, BorderLayout.NORTH);
    }

    private void panelContenido() {
        contendio = new JPanel(new GridLayout(1, 2));
        contendio.setPreferredSize(new Dimension(700, 390));
        contendio.setBackground(fondo);
        panel.add(contendio, BorderLayout.CENTER);
    }

    private void panelFooter() {
        footer = new JPanel(new BorderLayout());
        footer.setPreferredSize(new Dimension(700, 110));
        panel.add(footer, BorderLayout.SOUTH);
    }

    private void componentesCabecera() {
        botonesCabecera();
    }

    private void botonesCabecera() {
        panelBotonArchivo = new JPanel(new FlowLayout());
        panelBotonArchivo.setBackground(fondo);
        botonAgregarArchivo();
        botonNuevoArchivo();
        botonGuardarArchivo();
        cabecera.add(panelBotonArchivo, BorderLayout.WEST);
    }

    private void botonAgregarArchivo() {
        JPanel contenedor = new JPanel(new FlowLayout());
        contenedor.setBackground(fondo);
        botonArchivo = new JButton("Archivo");
        etiquetaAbrir = new JLabel(new ImageIcon("src/mx/unach/imagenes/folder.png"));
        botonArchivo.setFont(new Font("Arial", 3, 10));
        botonArchivo.setBackground(fondoBackBoton);
        botonArchivo.setForeground(fondoForeBoton);
        contenedor.add(etiquetaAbrir);
        contenedor.add(botonArchivo);
        botonArchivo.addActionListener((ae) -> {
            if (seleccionado.showDialog(null, "Abrir archivo") == JFileChooser.APPROVE_OPTION) {
                archivo = seleccionado.getSelectedFile();
                if (archivo.canRead()) {
                    if (archivo.getName().endsWith("txt")) {
                        String contenido = abrirTexto(archivo);
                        editor.setText(contenido);
                    }
                }
            }
        });
        panelBotonArchivo.add(contenedor);
    }

    private String abrirTexto(File archivo) {
        String contendio = "";

        try {
            entrada = new FileInputStream(archivo);
            int ascci;
            while ((ascci = entrada.read()) != -1) {
                char caracter = (char) ascci;
                contendio += caracter;
            }

        } catch (Exception e) {
        }
        return contendio;
    }
    
    private String guardarTexto(File archivo, String contenido) {
        String respuesta=null;
        try {
            salida = new FileOutputStream(archivo);
            byte[] byteText = contenido.getBytes();
            salida.write(byteText);
            respuesta = "Se guardo con exito el Archivo";
        }catch(Exception e){
            
        }
        return respuesta;
    }

    private void botonNuevoArchivo() {
        JPanel contenedor = new JPanel(new FlowLayout());
        contenedor.setBackground(fondo);
        botonNuevo = new JButton("Nuevo");
        etiquetaNuevo = new JLabel(new ImageIcon("src/mx/unach/imagenes/nuevo.png"));
        botonNuevo.setFont(new Font("Arial", 3, 10));
        botonNuevo.setBackground(fondoBackBoton);
        botonNuevo.setForeground(fondoForeBoton);
        contenedor.add(etiquetaNuevo);
        contenedor.add(botonNuevo);
        panelBotonArchivo.add(contenedor);
        botonNuevo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {

                editor.setText("");
                
            }
        });
        
    }

    private void botonGuardarArchivo() {
        JPanel contenedor = new JPanel(new FlowLayout());
        contenedor.setBackground(fondo);
        botonGuardar = new JButton("Guardar");
        etiquetaGuardar = new JLabel(new ImageIcon("src/mx/unach/imagenes/save.png"));
        
        botonGuardar.setFont(new Font("Arial", 3, 10));
        botonGuardar.setBackground(fondoBackBoton);
        botonGuardar.setForeground(fondoForeBoton);
        
        contenedor.add(etiquetaGuardar);
        contenedor.add(botonGuardar);
        panelBotonArchivo.add(contenedor);
        
        botonGuardar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
           
                 if (seleccionado.showDialog(null, "Guardar Archivo") == JFileChooser.APPROVE_OPTION) {
                     archivo = seleccionado.getSelectedFile();
                     if (archivo.getName().endsWith("txt")) {
                         String contenido = editor.getText();
                         String respuesta = guardarTexto(archivo, contenido);
                         if (respuesta != null) {
                             JOptionPane.showMessageDialog(null, respuesta);
                         } else {
                             JOptionPane.showMessageDialog(null, "Error al guardar el archivo");
                         }
                     } else {
                         JOptionPane.showMessageDialog(null, "El texto se debe guardar en un formato texto");
                     }
                 }
                
            }
        });
        
    }

    private void componentesContenido() {
        editor();
        tablaTokens();
    }

    private void editor() {
        editor = new JTextArea();
        editor.setFont(new Font("Source Sans Pro", 3, 12));
        editor.setForeground(new Color(220, 221, 225));
        nm = new NumeroLinea(editor);
        nm.setBackground(fondoBackBoton);
        nm.setForeground(Color.WHITE);
        jsp = new JScrollPane(editor);
        editor.setBackground(fondo);
        jsp.setRowHeaderView(nm);
        JPanel panelFlow = new JPanel(new FlowLayout());
        panelFlow.setPreferredSize(new Dimension(250, 540));
        panelFlow.setBackground(fondo);
        panelFlow.add(jsp);
        jsp.setPreferredSize(new Dimension(290, 340));
        contendio.add(panelFlow);
    }

    private void tablaTokens() {

        panelTablaTokens = new JPanel(new FlowLayout());
        panelTablaTokens.setPreferredSize(new Dimension(290, 340));
        panelTablaTokens.setBackground(fondo);
        tablaTokens = new JTable(dtmTT);

        JPanel panelBorder = new JPanel(new BorderLayout());
        panelBorder.setPreferredSize(new Dimension(260, 340));
        panelBorder.setBackground(fondo);

        panelBorder.add(tablaTokens, BorderLayout.CENTER);
        panelBorder.add(new JScrollPane(tablaTokens));

        panelTablaTokens.add(panelBorder);

        contendio.add(panelTablaTokens);
    }
    
    public void limpiarTabla() {
        int nColumn = dtmTT.getRowCount() - 1;
        if (nColumn >= 0) {
            for (int i = nColumn; i >= 0; i--) {
                dtmTT.removeRow(i);
            }
        }

    }
    
    public void limpiarTablaErrores() {
        int nColumn = dtmTE.getRowCount() - 1;
        if (nColumn >= 0) {
            for (int i = nColumn; i >= 0; i--) {
                dtmTE.removeRow(i);
            }
        }

    }
    
    // Metodo en donde se agrega los paneles de la parte de abajo
    private void componentesFooter() {
        JPanel panelBotonesBorder = new JPanel(new BorderLayout());
        panelBotones = new JPanel(new FlowLayout());
        panelBotones.setBackground(fondo);
        panelBotones.setPreferredSize(new Dimension(500, 25));
        panelTablaErrores = new JPanel(new BorderLayout());
        panelTablaErrores.setPreferredSize(new Dimension(500, 60));
        panelBotonLimpiar = new JPanel(new BorderLayout());
        panelBotonLimpiar.setPreferredSize(new Dimension(500, 25));
        panelBotonesBorder.add(panelBotones, BorderLayout.WEST);
        footer.add(panelBotonesBorder, BorderLayout.NORTH);
        panelBotonesBorder.setBackground(fondo);
        footer.add(panelTablaErrores, BorderLayout.CENTER);
        panelTablaErrores.setBackground(fondo);
        footer.add(panelBotonLimpiar, BorderLayout.SOUTH);
        panelBotonLimpiar.setBackground(fondo);
        botonAnalisisLexico();
        tablaErrores();
        botonLimpiar();
    }

    // Aqui se crea y se agrega el boton de analisis lexico
    // en este metodo puedes cambiarle el diseño al boton
    private void botonAnalisisLexico() {
        botonAnalisisLexico = new JButton("Analisis Lexico");
        botonAnalisisLexico.setFont(new Font("Arial", 3, 10));
        botonAnalisisLexico.setBackground(fondoBackBoton);
        botonAnalisisLexico.setForeground(fondoForeBoton);
        panelBotones.add(botonAnalisisLexico);

        botonAnalisisLexico.addActionListener(eventoBotonLexico());

        botonAnalisisSintactico = new JButton("Analisis Sintactico");
        botonAnalisisSintactico.setFont(new Font("Arial", 3, 10));
        botonAnalisisSintactico.setBackground(fondoBackBoton);
        botonAnalisisSintactico.setForeground(fondoForeBoton);
        panelBotones.add(botonAnalisisSintactico);

        botonAnalisisSemantico = new JButton("Analisis Semantico");
        botonAnalisisSemantico.setFont(new Font("Arial", 3, 10));
        botonAnalisisSemantico.setBackground(fondoBackBoton);
        botonAnalisisSemantico.setForeground(fondoForeBoton);
        panelBotones.add(botonAnalisisSemantico);
    }

    private ActionListener eventoBotonLexico() {
        return (ae) -> {
            AnalizadorLexico analizadorLexico = new AnalizadorLexico(this);
            analizadorLexico.setBuffer(editor.getText());
            limpiarTabla();
            List<Token> tokens = analizadorLexico.siguienteToken();
            String token[] = new String[2];
            for (int i = 0; i < tokens.size(); i++) {
                token[0] = tokens.get(i).getNombre();
                token[1] = tokens.get(i).getValor();
                dtmTT.addRow(token);

            }
            limpiarTablaErrores();
            List<mx.unach.compilador.Error> errores = analizadorLexico.getListaErrores();
            String error[] = new String[3];
            for (int i = 0; i < errores.size(); i++) {
                error[0] = String.valueOf(errores.get(i).getFila());
                
                if (i == 0)
                    error[1] = String.valueOf(errores.get(i).getColumna()+1);
                else 
                    error[1] = String.valueOf(errores.get(i).getColumna());
                
                error[2] = errores.get(i).getDescripcion();
                dtmTE.addRow(error);

            }

        };
    }

    private void tablaErrores() {
        panelTablaErrores.setBackground(fondo);
        panelTablaErrores.setPreferredSize(new Dimension(450, 30));
        tablaErrores = new JTable(dtmTE);
        tablaErrores.setDefaultRenderer(Object.class, new MiRenderer());
        TableColumnModel tcm = tablaErrores.getColumnModel();

        tcm.getColumn(0).setPreferredWidth(40);
        tcm.getColumn(1).setPreferredWidth(60);
        tcm.getColumn(2).setPreferredWidth(350);

        JPanel panelBorder = new JPanel(new BorderLayout());
        panelBorder.setBackground(fondo);

        panelBorder.add(tablaErrores, BorderLayout.CENTER);
        panelBorder.add(new JScrollPane(tablaErrores));

        panelTablaErrores.add(panelBorder, BorderLayout.WEST);

    }

    private void botonLimpiar() {
        JPanel panelFlow = new JPanel(new FlowLayout());
        limpiarEditor = new JButton("Limpiar");
        limpiarEditor.setFont(new Font("Arial", 3, 10));
        limpiarEditor.setBackground(fondoBackBoton);
        limpiarEditor.setForeground(fondoForeBoton);
        panelFlow.add(limpiarEditor);
        panelFlow.setBackground(fondo);
        panelBotonLimpiar.add(panelFlow, BorderLayout.WEST);

        limpiarEditor.addActionListener(eventoLimpiarBoton());
    }

    private ActionListener eventoLimpiarBoton() {
        return (e) -> {
            editor.setText("");
            limpiarTabla();
        };
    }

    public JTextArea getEditor() {
        return editor;
    }
}
