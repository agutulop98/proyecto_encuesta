package Encuesta;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


import Encuesta.Encuesta;
import Encuesta.Pregunta;
import Encuesta.Respuesta;

public class EncuestaDAO {

	public void insertarEncuesta(Encuesta encuesta) {
	    String sqlEncuesta = "INSERT INTO encuestas (titulo, descripcion) VALUES (?, ?)";
	    String sqlPregunta = "INSERT INTO preguntas (encuesta_id, texto) VALUES (?, ?)";
	    
	    try (Connection conn = ConexionBD.getConexion();
	         PreparedStatement stmtEncuesta = conn.prepareStatement(sqlEncuesta, Statement.RETURN_GENERATED_KEYS);
	         PreparedStatement stmtPregunta = conn.prepareStatement(sqlPregunta)) {
	        
	        // Insertar encuesta
	        stmtEncuesta.setString(1, encuesta.getTitulo());
	        stmtEncuesta.setString(2, encuesta.getDescripcion());
	        stmtEncuesta.executeUpdate();

	        // Obtener el ID generado de la encuesta
	        ResultSet rs = stmtEncuesta.getGeneratedKeys();
	        if (rs.next()) {
	            int encuestaId = rs.getInt(1);

	            // Insertar preguntas
	            for (Pregunta pregunta : encuesta.getPreguntas()) {
	                stmtPregunta.setInt(1, encuestaId);
	                stmtPregunta.setString(2, pregunta.getTexto());
	                stmtPregunta.executeUpdate();
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	public void insertarRespuestas(List<Respuesta> respuestas) {
	    String sql = "INSERT INTO respuestas (pregunta_id, texto) VALUES (?, ?)";

	    try (Connection conn = ConexionBD.getConexion();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	        
	        for (Respuesta respuesta : respuestas) {
	            stmt.setInt(1, obtenerPreguntaId(respuesta.getPregunta()));
	            stmt.setString(2, respuesta.getRespuesta());
	            stmt.executeUpdate();
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	// Método auxiliar para obtener el ID de una pregunta
	private int obtenerPreguntaId(Pregunta pregunta) {
	    String sql = "SELECT id FROM preguntas WHERE texto = ?";
	    try (Connection conn = ConexionBD.getConexion();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	        
	        stmt.setString(1, pregunta.getTexto());
	        ResultSet rs = stmt.executeQuery();
	        if (rs.next()) {
	            return rs.getInt("id");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return -1; // Si no encuentra el ID
	}

	public List<Encuesta> obtenerEncuestasConDetalles() {
	    List<Encuesta> encuestas = new ArrayList<>();
	    String sqlEncuestas = "SELECT * FROM encuestas";
	    String sqlPreguntas = "SELECT * FROM preguntas WHERE encuesta_id = ?";
	    String sqlRespuestas = "SELECT * FROM respuestas WHERE pregunta_id = ?";

	    try (Connection conn = ConexionBD.getConexion();
	         Statement stmtEncuestas = conn.createStatement();
	         ResultSet rsEncuestas = stmtEncuestas.executeQuery(sqlEncuestas)) {
	        
	        while (rsEncuestas.next()) {
	            Encuesta encuesta = new Encuesta(
	                rsEncuestas.getString("titulo"),
	                rsEncuestas.getString("descripcion")
	            );

	            // Obtener preguntas para esta encuesta
	            try (PreparedStatement stmtPreguntas = conn.prepareStatement(sqlPreguntas)) {
	                stmtPreguntas.setInt(1, rsEncuestas.getInt("id"));
	                ResultSet rsPreguntas = stmtPreguntas.executeQuery();

	                while (rsPreguntas.next()) {
	                    Pregunta pregunta = new Pregunta(rsPreguntas.getString("texto"));

	                    // Obtener respuestas para esta pregunta
	                    try (PreparedStatement stmtRespuestas = conn.prepareStatement(sqlRespuestas)) {
	                        stmtRespuestas.setInt(1, rsPreguntas.getInt("id"));
	                        ResultSet rsRespuestas = stmtRespuestas.executeQuery();

	                        while (rsRespuestas.next()) {
	                            Respuesta respuesta = new Respuesta(pregunta, rsRespuestas.getString("texto"));
	                            encuesta.agregarRespuesta(respuesta);
	                        }
	                    }
	                    encuesta.agregarPregunta(pregunta);
	                }
	            }
	            encuestas.add(encuesta);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return encuestas;
	}

    public List<Encuesta> obtenerEncuestas() {
        List<Encuesta> encuestas = new ArrayList<>();
        String sql = "SELECT * FROM Encuestas";
        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String titulo = rs.getString("titulo");
                String descripcion = rs.getString("descripcion");
                encuestas.add(new Encuesta(titulo, descripcion));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return encuestas;
    }
 // MÉTODO PARA LIMPIAR LA BASE DE DATOS (Borrar todas las encuestas, preguntas y respuestas)
    public void limpiarEncuestas() {
        String sqlEliminarRespuestas = "DELETE FROM respuestas";
        String sqlEliminarPreguntas = "DELETE FROM preguntas";
        String sqlEliminarEncuestas = "DELETE FROM encuestas";
        
        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement()) {
            // Primero borrar las respuestas, luego las preguntas y finalmente las encuestas
            stmt.executeUpdate(sqlEliminarRespuestas);
            stmt.executeUpdate(sqlEliminarPreguntas);
            stmt.executeUpdate(sqlEliminarEncuestas);
            
            System.out.println("Se han borrado todas las encuestas y sus datos asociados.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

