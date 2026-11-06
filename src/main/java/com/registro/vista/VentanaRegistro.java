package com.registro.vista;

import com.registro.modelo.MedioRegistro;
import com.registro.modelo.ResultadoRegistro;
import com.registro.modelo.Vehiculo;
import com.registro.negocio.RegistroVehiculoServicio;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

public class VentanaRegistro extends JFrame {

    private static final Color VERDE = new Color(0, 120, 60);
    private static final Color AZUL = new Color(0, 80, 160);
    private static final Color ROJO = new Color(170, 30, 30);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final RegistroVehiculoServicio servicio = new RegistroVehiculoServicio();

    private final JTextField campoMarca = new JTextField(20);
    private final JTextField campoModelo = new JTextField(20);
    private final JTextField campoAnio = new JTextField(20);
    private final JTextField campoPrecio = new JTextField(20);
    private final JTextField campoColor = new JTextField(20);
    private final JCheckBox casillaSimularFalla =
            new JCheckBox("Simular falla de la conexión directa a la base de datos");
    private final JButton botonGuardar = new JButton("Guardar");
    private final JButton botonLimpiar = new JButton("Limpiar");
    private final JLabel etiquetaResultado = new JLabel("Ingrese los datos del vehículo y presione Guardar.");
    private final JTextArea areaHistorial = new JTextArea(8, 50);

    public VentanaRegistro() {
        super("Registro de vehículos - JDBC con contingencia por API REST");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createTitledBorder("Datos del vehículo"));
        agregarCampo(formulario, 0, "Marca:", campoMarca);
        agregarCampo(formulario, 1, "Modelo:", campoModelo);
        agregarCampo(formulario, 2, "Año:", campoAnio);
        agregarCampo(formulario, 3, "Precio (Q):", campoPrecio);
        agregarCampo(formulario, 4, "Color:", campoColor);

        GridBagConstraints restricciones = new GridBagConstraints();
        restricciones.gridx = 0;
        restricciones.gridy = 5;
        restricciones.gridwidth = 2;
        restricciones.anchor = GridBagConstraints.WEST;
        restricciones.insets = new Insets(8, 6, 4, 6);
        formulario.add(casillaSimularFalla, restricciones);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(botonGuardar);
        botones.add(botonLimpiar);
        restricciones.gridy = 6;
        formulario.add(botones, restricciones);

        etiquetaResultado.setFont(etiquetaResultado.getFont().deriveFont(Font.BOLD, 13f));
        etiquetaResultado.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        areaHistorial.setEditable(false);
        areaHistorial.setLineWrap(true);
        areaHistorial.setWrapStyleWord(true);
        JScrollPane panelHistorial = new JScrollPane(areaHistorial);
        panelHistorial.setBorder(BorderFactory.createTitledBorder("Historial de registros de esta sesión"));

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.add(etiquetaResultado, BorderLayout.NORTH);
        panelInferior.add(panelHistorial, BorderLayout.CENTER);

        add(formulario, BorderLayout.NORTH);
        add(panelInferior, BorderLayout.CENTER);

        botonGuardar.addActionListener(evento -> guardar());
        botonLimpiar.addActionListener(evento -> limpiar());
        getRootPane().setDefaultButton(botonGuardar);

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    private void agregarCampo(JPanel panel, int fila, String texto, JTextField campo) {
        GridBagConstraints etiqueta = new GridBagConstraints();
        etiqueta.gridx = 0;
        etiqueta.gridy = fila;
        etiqueta.anchor = GridBagConstraints.EAST;
        etiqueta.insets = new Insets(4, 6, 4, 6);
        panel.add(new JLabel(texto), etiqueta);

        GridBagConstraints entrada = new GridBagConstraints();
        entrada.gridx = 1;
        entrada.gridy = fila;
        entrada.weightx = 1;
        entrada.fill = GridBagConstraints.HORIZONTAL;
        entrada.insets = new Insets(4, 6, 4, 6);
        panel.add(campo, entrada);
    }

    private void guardar() {
        Vehiculo vehiculo;
        try {
            vehiculo = servicio.crearVehiculo(campoMarca.getText(), campoModelo.getText(),
                    campoAnio.getText(), campoPrecio.getText(), campoColor.getText());
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Datos inválidos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean simularFalla = casillaSimularFalla.isSelected();
        botonGuardar.setEnabled(false);
        mostrarResultado("Registrando el vehículo...", Color.DARK_GRAY);

        new SwingWorker<ResultadoRegistro, Void>() {
            @Override
            protected ResultadoRegistro doInBackground() throws Exception {
                return servicio.registrar(vehiculo, simularFalla);
            }

            @Override
            protected void done() {
                try {
                    confirmar(vehiculo, get());
                } catch (ExecutionException e) {
                    informarError(vehiculo, e.getCause());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    botonGuardar.setEnabled(true);
                }
            }
        }.execute();
    }

    private void confirmar(Vehiculo vehiculo, ResultadoRegistro resultado) {
        boolean porBaseDatos = resultado.getMedio() == MedioRegistro.BASE_DATOS;
        String mensaje = "Vehículo #" + resultado.getIdVehiculo() + " registrado "
                + resultado.getMedio().getDescripcion() + ".";
        mostrarResultado(mensaje, porBaseDatos ? VERDE : AZUL);

        String linea = "[" + LocalTime.now().format(HORA) + "] " + vehiculo.getDescripcion()
                + " → id " + resultado.getIdVehiculo() + ", registrado " + resultado.getMedio().getDescripcion();
        if (!porBaseDatos) {
            linea += ". La conexión directa falló: " + resultado.getMotivoContingencia();
        }
        agregarHistorial(linea);

        String detalle = porBaseDatos
                ? mensaje
                : mensaje + "\n\nLa conexión directa a la base de datos falló, por lo que se utilizó la API.\n"
                + "Motivo: " + resultado.getMotivoContingencia();
        JOptionPane.showMessageDialog(this, detalle, "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
        limpiar();
    }

    private void informarError(Vehiculo vehiculo, Throwable causa) {
        String titulo = causa instanceof SQLException
                ? "La base de datos rechazó el registro"
                : "No se pudo registrar el vehículo";
        mostrarResultado(titulo + ".", ROJO);
        agregarHistorial("[" + LocalTime.now().format(HORA) + "] " + vehiculo.getDescripcion()
                + " → ERROR: " + causa.getMessage());
        JOptionPane.showMessageDialog(this, causa.getMessage(), titulo, JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarResultado(String texto, Color color) {
        etiquetaResultado.setText(texto);
        etiquetaResultado.setForeground(color);
    }

    private void agregarHistorial(String linea) {
        areaHistorial.append(linea + "\n");
        areaHistorial.setCaretPosition(areaHistorial.getDocument().getLength());
    }

    private void limpiar() {
        campoMarca.setText("");
        campoModelo.setText("");
        campoAnio.setText("");
        campoPrecio.setText("");
        campoColor.setText("");
        campoMarca.requestFocusInWindow();
    }
}