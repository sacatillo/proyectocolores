/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.pro_memoria;

/**
 *
 * @author Martin Montes
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;

class Usuario {
    private String nombre;
    private String password;

    public Usuario(String nombre, String password) {
        this.nombre = nombre;
        this.password = password;
    }

    public String getNombre() {
        return nombre;
    }
    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return nombre; 
    }
}

public class JuegoDeMemoria extends JFrame {
    private int filas, columnas, intentos;
    private JButton[][] botones;
    private ArrayList<Color> colores;
    private boolean[][] descubiertas;

    private int primerClickX = -1, primerClickY = -1;
    private int intentosRestantes;
    private Usuario jugador1;
    private Usuario jugador2;
    private int puntuacionJugador1 = 0;
    private int puntuacionJugador2 = 0;
    private int turnoActual = 0; 
    private long tiempoInicio;

    // Lista de usuarios registrados
    private static ArrayList<Usuario> usuarios = new ArrayList<>();

    // Variable para bloquear clics mientras el temporizador oculta cartas
    private boolean esperandoRespuesta = false; 

    public JuegoDeMemoria(int filas, int columnas, int intentos, Usuario j1, Usuario j2) {
        this.filas = filas;
        this.columnas = columnas;
        this.intentos = intentos;
        this.intentosRestantes = intentos;
        this.botones = new JButton[filas][columnas];
        this.colores = new ArrayList<>();
        this.descubiertas = new boolean[filas][columnas];
        this.jugador1 = j1;
        this.jugador2 = j2;

        // Generar pares de colores
        for (int i = 0; i < (filas * columnas) / 2; i++) {
            Color color = new Color((int)(Math.random() * 0x1000000));
            colores.add(color);
            colores.add(color);
        }
        Collections.shuffle(colores);

        setLayout(new GridLayout(filas, columnas));
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                botones[i][j] = new JButton();
                botones[i][j].setPreferredSize(new Dimension(100, 100));
                botones[i][j].setFont(new Font("Arial", Font.BOLD, 36));
                botones[i][j].setBackground(Color.LIGHT_GRAY);
                final int x = i;
                final int y = j;
                botones[i][j].addActionListener(e -> manejarClick(x, y));
                add(botones[i][j]);
            }
        }

        setTitle("Juego de Memoria - Turno de: " + jugadorActual().getNombre());
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        tiempoInicio = System.currentTimeMillis();
    }

    private Usuario jugadorActual() {
        return (turnoActual == 0) ? jugador1 : jugador2;
    }

    private void cambiarTurno() {
        turnoActual = (turnoActual + 1) % 2;
        setTitle("Juego de Memoria - Turno de: " + jugadorActual().getNombre());
    }

    private void manejarClick(int x, int y) {
        //Aqui se verifica si hay espera en las cartas,se quitan los clics
        if (esperandoRespuesta) {
            return; 
        }

        if (descubiertas[x][y]) return;

        botones[x][y].setBackground(colores.get(x * columnas + y));
        botones[x][y].setEnabled(false);
        descubiertas[x][y] = true;

        if (primerClickX == -1) {
            // Primer clic
            primerClickX = x;
            primerClickY = y;
        } else {
            // Segundo clic
            if (colores.get(primerClickX * columnas + primerClickY).equals(colores.get(x * columnas + y))) {
                // Match
                incrementarPuntuacion();
                if (seEncontraronTodos()) {
                    mostrarResultados();
                } else {
                    mostrarMensaje("¡Es un match! " + jugadorActual().getNombre() + " continúa.");
                    primerClickX = -1;
                    primerClickY = -1;
                }
            } else {
                // No es match
                intentosRestantes--;
                int intentosAux = intentosRestantes;
                mostrarMensaje("No es un match. Intentos restantes: " + intentosAux);
                            
                esperandoRespuesta = true; //Antes de que inicie el tiempo de que se oculten las cartas,se desactivan los clics
                Timer timer = new Timer(1000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ocultarCeldas(primerClickX, primerClickY, x, y);
                        primerClickX = -1;
                        primerClickY = -1;
                        esperandoRespuesta = false;//Ya cuando se terminan de ocultar,se vuelve a permitir dar clics

                        if (intentosAux == 0 || seEncontraronTodos()) {
                            mostrarResultados();
                        } else {
                            cambiarTurno();
                        }
                    }
                });
                timer.setRepeats(false);
                timer.start();
            }
        }
    }

    private void incrementarPuntuacion() {
        if (turnoActual == 0) {
            puntuacionJugador1++;
        } else {
            puntuacionJugador2++;
        }
    }

    private boolean seEncontraronTodos() {
        int totalMatches = (filas * columnas) / 2;
        int matchesEncontrados = puntuacionJugador1 + puntuacionJugador2;
        return matchesEncontrados == totalMatches;
    }

    private void ocultarCeldas(int x1, int y1, int x2, int y2) {
        botones[x1][y1].setBackground(Color.LIGHT_GRAY);
        botones[x1][y1].setEnabled(true);
        botones[x2][y2].setBackground(Color.LIGHT_GRAY);
        botones[x2][y2].setEnabled(true);
        descubiertas[x1][y1] = false;
        descubiertas[x2][y2] = false;
    }

    private void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje);
    }

    private void mostrarResultados() {
        long tiempoFinal = System.currentTimeMillis();
        long tiempoTotal = (tiempoFinal - tiempoInicio) / 1000;
        String resultado = "Puntuación:\n" +
                           jugador1.getNombre() + ": " + puntuacionJugador1 + " matches\n" +
                           jugador2.getNombre() + ": " + puntuacionJugador2 + " matches\n\n";

        String ganador;
        if (puntuacionJugador1 > puntuacionJugador2) {
            ganador = "¡" + jugador1.getNombre() + " gana!";
        } else if (puntuacionJugador2 > puntuacionJugador1) {
            ganador = "¡" + jugador2.getNombre() + " gana!";
        } else {
            ganador = "¡Es un empate!";
        }

        String mensaje = resultado +
                         "Tiempo total: " + tiempoTotal + " segundos\n" +
                         (intentosRestantes > 0 ? "Partida completada." : "No quedan intentos.") + "\n" +
                         ganador;
        JOptionPane.showMessageDialog(this, mensaje);

        
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Quieres jugar de nuevo?", "Juego de Memoria", JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION) {
            reiniciarJuego();
        } else {
            dispose(); // Cerrar la ventana actual
            mostrarMenuPrincipal(); // Volver al menú principal
        }
    }

    private void reiniciarJuego() {
        puntuacionJugador1 = 0;
        puntuacionJugador2 = 0;
        turnoActual = 0;
        intentosRestantes = intentos;
        primerClickX = -1;
        primerClickY = -1;
        tiempoInicio = System.currentTimeMillis();
        colores.clear();
        for (int i = 0; i < (filas * columnas) / 2; i++) {
            Color color = new Color((int)(Math.random() * 0x1000000));
            colores.add(color);
            colores.add(color);
        }
        Collections.shuffle(colores);
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                botones[i][j].setBackground(Color.LIGHT_GRAY);
                botones[i][j].setEnabled(true);
                descubiertas[i][j] = false;
            }
        }
        setTitle("Juego de Memoria - Turno de: " + jugadorActual().getNombre());
    }

    private static boolean registrarUsuario(String nombre, String password) {
        for (Usuario u : usuarios) {
            if (u.getPassword().equals(password)) {
                return false; 
            }
        }
        usuarios.add(new Usuario(nombre, password));
        return true;
    }

    private static void mostrarMenuPrincipal() {
        while (true) {
            String[] opciones = {"Registrar", "Jugar", "Salir"};
            int seleccion = JOptionPane.showOptionDialog(
                null,
                "Bienvenido al Juego de Memoria\nSeleccione una opción:",
                "Menú Principal",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                opciones,
                opciones[0]
            );

            switch (seleccion) {
                case 0: // Registrar
                    String nombreReg = JOptionPane.showInputDialog("Ingrese nombre de usuario:");
                    if (nombreReg == null) break;
                    String passReg = JOptionPane.showInputDialog("Ingrese contraseña:");
                    if (passReg == null) break;
                    if (passReg.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(null, "La contraseña no puede estar vacía.");
                        break;
                    }
                    if (registrarUsuario(nombreReg, passReg)) {
                        JOptionPane.showMessageDialog(null, "Usuario registrado exitosamente.");
                    } else {
                        JOptionPane.showMessageDialog(null, "La contraseña ya está en uso. Intente con otra.");
                    }
                    break;

                case 1: // Jugar
                    if (usuarios.size() < 2) {
                        JOptionPane.showMessageDialog(null, "Se necesitan al menos 2 usuarios registrados para jugar.");
                    } else {
                        Usuario j1 = (Usuario) JOptionPane.showInputDialog(
                            null,
                            "Seleccione Jugador 1:",
                            "Seleccionar Jugador",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            usuarios.toArray(),
                            usuarios.get(0)
                        );
                        if (j1 == null) break;

                        ArrayList<Usuario> resto = new ArrayList<>(usuarios);
                        resto.remove(j1);
                        Usuario j2 = (Usuario) JOptionPane.showInputDialog(
                            null,
                            "Seleccione Jugador 2:",
                            "Seleccionar Jugador",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            resto.toArray(),
                            resto.get(0)
                        );
                        if (j2 == null) break;

                        mostrarMenuDificultad(j1, j2);
                        return;
                    }
                    break;

                default: // Salir
                    System.exit(0);
            }
        }
    }

    private static void mostrarMenuDificultad(Usuario j1, Usuario j2) {
        String[] opciones = {
            "Infantes (6x6, 30 intentos)",
            "Adolescentes (8x8, 13 intentos)",
            "Adultos (10x10, 15 intentos)"
        };
        int seleccion = JOptionPane.showOptionDialog(null, "Selecciona la dificultad", "Juego de Memoria",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, opciones, opciones[0]);

        switch (seleccion) {
            case 0:
                new JuegoDeMemoria(6, 6, 30, j1, j2);
                break;
            case 1:
                new JuegoDeMemoria(8, 8, 13, j1, j2);
                break;
            case 2:
                new JuegoDeMemoria(10, 10, 15, j1, j2);
                break;
            default:
                System.exit(0);
        }
    }

    public static void main(String[] args) {
        mostrarMenuPrincipal();
    }
}