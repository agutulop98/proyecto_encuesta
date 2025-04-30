package Encuesta;

import java.util.ArrayList;
import java.util.List;

public class Encuesta {
    private String titulo;
    private String descripcion;
    private List<Pregunta> preguntas;
    private List<Respuesta> respuestas;

    // Constructor sin argumentos (para Jackson)
    public Encuesta() {
        // inicializar las listas para evitar NullPointerExceptions
        this.preguntas = new ArrayList<>();
        this.respuestas = new ArrayList<>();
    }

    // Constructor con argumentos
    public Encuesta(String titulo, String descripcion) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.preguntas = new ArrayList<>();
        this.respuestas = new ArrayList<>();
    }

    // Getters
    public String getTitulo() {
        return titulo;
    }
    public String getDescripcion() {
        return descripcion;
    }
    public List<Pregunta> getPreguntas() {
        return preguntas;
    }
    public List<Respuesta> getRespuestas() {
        return respuestas;
    }

    // Setters (si tu JSON incluye estos campos y Jackson debe asignarlos)
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    public void setPreguntas(List<Pregunta> preguntas) {
        this.preguntas = preguntas;
    }
    public void setRespuestas(List<Respuesta> respuestas) {
        this.respuestas = respuestas;
    }

    // Métodos adicionales
    public List<String> getRespuestasPorPregunta(Pregunta pregunta) {
        List<String> respuestasPregunta = new ArrayList<>();
        for (Respuesta respuesta : respuestas) {
            if (respuesta.getPregunta().equals(pregunta)) {
                respuestasPregunta.add(respuesta.getRespuesta());
            }
        }
        return respuestasPregunta;
    }

    public void agregarPregunta(Pregunta pregunta) {
        preguntas.add(pregunta);
    }

    public void agregarRespuesta(Respuesta respuesta) {
        respuestas.add(respuesta);
    }
}
