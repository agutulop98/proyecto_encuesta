package Encuesta;


class Respuesta {
    private Pregunta pregunta;
    private String respuesta;

    public Respuesta(Pregunta pregunta, String respuesta) {
        this.pregunta = pregunta;
        this.respuesta = respuesta;
    }

    public Pregunta getPregunta() {
        return pregunta;
    }

    public String getRespuesta() {
        return respuesta;
    }
}
