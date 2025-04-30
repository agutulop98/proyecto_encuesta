package Encuesta;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import java.io.InputStream;
import java.io.IOException;

import Encuesta.Encuesta;
import Encuesta.Pregunta;
import Encuesta.Respuesta;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.mail.MessagingException;


public class SistemaEncuesta extends Application {

    private List<Encuesta> encuestas = new ArrayList<>();
    private String userRole = "usuario"; // Rol del usuario actual
    private EncuestaDAO encuestaDAO = new EncuestaDAO();

    public static void main(String[] args) {
        launch(args);
    }
     
    
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sistema de Encuestas y Análisis de Opinión");

        // Cargar los datos de la base de datos
        encuestas = encuestaDAO.obtenerEncuestasConDetalles();
        
        // Luego, configuras tu interfaz (BorderPane, menú, etc.)
        BorderPane root = new BorderPane();
        root.getStyleClass().add("fondo-principal");

        // Parte superior: Título
        Label lblTitulo = new Label("Bienvenido al Sistema de Encuestas");
        lblTitulo.getStyleClass().add("titulo-principal");
        HBox topBox = new HBox(lblTitulo);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(15));
        root.setTop(topBox);

        // Parte central: Menú (se usarán los datos cargados en "encuestas")
        VBox menuCentral = new VBox(15);
        menuCentral.setAlignment(Pos.CENTER);
        menuCentral.setPadding(new Insets(10));

        
        Button btnCrearEncuesta = new Button("Crear Encuesta");
        btnCrearEncuesta.getStyleClass().add("boton");
        btnCrearEncuesta.setOnAction(e -> mostrarCrearEncuesta(primaryStage));
        
        Button btnResponderEncuesta = new Button("Responder Encuesta");
        btnResponderEncuesta.getStyleClass().add("boton");
        btnResponderEncuesta.setOnAction(e -> mostrarResponderEncuesta(primaryStage));
        
        Button btnVerResultados = new Button("Ver Resultados");
        btnVerResultados.getStyleClass().add("boton");
        btnVerResultados.setOnAction(e -> mostrarVerResultados(primaryStage));
        
        Button btnCambiarUsuario = new Button("Cambiar Usuario");
        btnCambiarUsuario.getStyleClass().add("boton");
        btnCambiarUsuario.setOnAction(e -> seleccionarRolUsuario(primaryStage));
        
        Button btnImportarDesdeAPI = new Button("Importar Encuesta (API)");
        btnImportarDesdeAPI.getStyleClass().add("boton");
        btnImportarDesdeAPI.setOnAction(e -> {
            // Verificar si es admin, por ejemplo
            if (!userRole.equals("admin")) {
                mostrarAlerta(Alert.AlertType.ERROR, "Permiso denegado", "Solo admin puede importar encuestas.");
                return;
            }

            // Pedir la URL de la encuesta
            TextField urlField = new TextField();
            urlField.setPromptText("URL de la encuesta (ej: http://localhost:8081/api/encuesta/123)");

            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Importar desde API");
            dialog.setHeaderText("Ingrese la URL de la encuesta");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.getDialogPane().setContent(urlField);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    return urlField.getText();
                }
                return null;
            });

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(url -> {
                // Llamar al método de importación
                importarEncuestaDesdeAPI(url);
            });
        });

        // Agregar el botón a tu menú principal
        menuCentral.getChildren().add(btnImportarDesdeAPI);


        
     // Botón para limpiar la base de datos (borrar encuestas de prueba)
        Button btnLimpiarBD = new Button("Limpiar Base de Datos");
        btnLimpiarBD.getStyleClass().add("boton");
        btnLimpiarBD.setOnAction(e -> {
            // Verificar que el usuario tenga rol "admin"
            if (!userRole.equals("admin")) {
                mostrarAlerta(Alert.AlertType.ERROR, "Permiso denegado", 
                    "Solo el perfil admin puede limpiar la base de datos.");
                return;
            }
            // Mostrar confirmación para borrar todos los registros
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmación");
            confirm.setHeaderText("¿Estás seguro de borrar todos los registros?");
            confirm.setContentText("Esta acción borrará todas las encuestas, preguntas y respuestas de la base de datos.");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                encuestaDAO.limpiarEncuestas();
                // Actualizar la lista local de encuestas tras borrar los registros
                encuestas = encuestaDAO.obtenerEncuestasConDetalles();
                mostrarAlerta(Alert.AlertType.INFORMATION, "Limpiar BD", 
                    "Se han borrado las encuestas de prueba correctamente.");
            }
        });
        
        Button btnCalcularEstadisticas = new Button("Calcular Estadísticas");
        btnCalcularEstadisticas.getStyleClass().add("boton");
        btnCalcularEstadisticas.setOnAction(e -> {
            String estadisticas = obtenerEstadisticas();
            
            // Mostrar el resultado en un Alert con un TextArea (para ver bien el contenido)
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Estadísticas");
            alert.setHeaderText("Resultados Estadísticos");
            
            javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(estadisticas);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            alert.getDialogPane().setContent(textArea);
            
            alert.showAndWait();
        });
        
     // Dentro del método start(...) en SistemaEncuesta.java, junto a tus otros botones
        Button btnEnviarInvitacion = new Button("Enviar Invitación");
        btnEnviarInvitacion.getStyleClass().add("boton");
        btnEnviarInvitacion.setOnAction(e -> {
            // Mostrar un diálogo para pedir la dirección de correo
            TextField emailField = new TextField();
            emailField.setPromptText("Correo electrónico");
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Enviar Invitación");
            dialog.setHeaderText("Ingrese el correo electrónico de destino");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.getDialogPane().setContent(emailField);
            
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    return emailField.getText();
                }
                return null;
            });
            
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(email -> {
                // Define el asunto y cuerpo del mensaje
                String subject = "Invitación para participar en una encuesta";
                String body = "Hola,\n\nTe invitamos a participar en nuestra encuesta. Por favor, ingresa a la aplicación para más detalles.\n\n¡Gracias!";
                try {
                    EmailSender.sendInvitationEmail(email, subject, body);
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Invitación", "Invitación enviada exitosamente a: " + email);
                } catch (MessagingException ex) {
                    ex.printStackTrace();
                    mostrarAlerta(Alert.AlertType.ERROR, "Error de Envío", "No se pudo enviar la invitación: " + ex.getMessage());
                }
            });
        });

        
        // Si agregas una opción para estadísticas o exportación, también puedes incluirla aquí.
        menuCentral.getChildren().addAll(btnCrearEncuesta, btnResponderEncuesta, btnVerResultados, btnCambiarUsuario, btnLimpiarBD);
        menuCentral.getChildren().add(btnCalcularEstadisticas);
        menuCentral.getChildren().add(btnEnviarInvitacion);

        root.setCenter(menuCentral);

        Scene scene = new Scene(root, 600, 400);
        scene.getStylesheets().add(getClass().getResource("estilos.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

 

    private void seleccionarRolUsuario(Stage stage) {
        Dialog<String> dialog = new ChoiceDialog<>("usuario", "admin", "editor", "usuario");
        dialog.setTitle("Seleccionar Rol de Usuario");
        dialog.setHeaderText("Seleccione su rol");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String rolSeleccionado = result.get();
            if ("admin".equals(rolSeleccionado)) {
                if (!mostrarLoginAdmin()) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error de Autenticación");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("La autenticación para admin ha fallado.");
                    errorAlert.showAndWait();
                    userRole = "usuario";
                } else {
                    userRole = "admin";
                }
            } else {
                userRole = rolSeleccionado;
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Rol Seleccionado");
            alert.setHeaderText(null);
            alert.setContentText("Has iniciado sesión como: " + userRole);
            alert.showAndWait();
        }
    }

    // El método mostrarLoginAdmin() puede permanecer igual, o también se le pueden aplicar mejoras de estilo.

    private boolean mostrarLoginAdmin() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Autenticación Admin");
        dialog.setHeaderText("Por favor, ingrese las credenciales de admin");

        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Usuario");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Contraseña");

        grid.add(new Label("Usuario:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Contraseña:"), 0, 1);
        grid.add(passwordField, 1, 1);

        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(() -> usernameField.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(usernameField.getText(), passwordField.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();
        if (result.isPresent()) {
            Pair<String, String> credentials = result.get();
            if (credentials.getKey().equals("admin") && credentials.getValue().equals("admin+")) {
                return true;
            }
        }
        return false;
    }


    private void mostrarCrearEncuesta(Stage stage) {
        if (!userRole.equals("admin")) {
            mostrarPermisoDenegado();
            return;
        }

            // Usamos un BorderPane como contenedor principal
            BorderPane layout = new BorderPane();
            // Le asignamos la clase "panel" (definida en estilos.css)
            layout.getStyleClass().add("panel");

            // ----- Sección superior: Título -----
            Label lblTitulo = new Label("Crear Encuesta");
            // Clase CSS para estilos de encabezado
            lblTitulo.getStyleClass().add("encabezado");

            HBox topBox = new HBox(lblTitulo);
            topBox.setAlignment(Pos.CENTER);
            topBox.setPadding(new Insets(10));
            layout.setTop(topBox);

            // ----- Sección central: Formulario (Título y Descripción) -----
            GridPane formGrid = new GridPane();
            formGrid.setHgap(10);
            formGrid.setVgap(10);

            // Etiqueta y campo para el Título
            Label lblTituloEncuesta = new Label("Título de la Encuesta:");
            lblTituloEncuesta.getStyleClass().add("form-label");
            
            TextField txtTitulo = new TextField();
            txtTitulo.getStyleClass().add("input-field");
            
            // Etiqueta y campo para la Descripción
            Label lblDescripcionEncuesta = new Label("Descripción:");
            lblDescripcionEncuesta.getStyleClass().add("form-label");
            
            TextField txtDescripcion = new TextField();
            txtDescripcion.getStyleClass().add("input-field");

            // Añadimos al GridPane
            formGrid.add(lblTituloEncuesta, 0, 0);
            formGrid.add(txtTitulo, 1, 0);
            formGrid.add(lblDescripcionEncuesta, 0, 1);
            formGrid.add(txtDescripcion, 1, 1);

            layout.setCenter(formGrid);

            // ----- Sección inferior: Preguntas y botones -----
            VBox bottomBox = new VBox(10);
            bottomBox.setPadding(new Insets(10));

            // Contenedor para las preguntas dinámicas
            VBox preguntasBox = new VBox(10);
            preguntasBox.setPadding(new Insets(10));
            // Aplicar un borde gris para separarlo visualmente
            preguntasBox.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5; -fx-padding: 10;");

            // Botón para añadir campos de preguntas
            Button btnAgregarPregunta = new Button("Agregar Pregunta");
            btnAgregarPregunta.getStyleClass().add("boton");
            btnAgregarPregunta.setOnAction(e -> {
                TextField txtPregunta = new TextField();
                txtPregunta.setPromptText("Escribe una pregunta...");
                // Opcional: aplicarle la clase "input-field"
                txtPregunta.getStyleClass().add("input-field");
                preguntasBox.getChildren().add(txtPregunta);
            });

            // Botón para guardar la encuesta
            Button btnGuardar = new Button("Guardar Encuesta");
            btnGuardar.getStyleClass().add("boton");
            btnGuardar.setOnAction(e -> {
                String titulo = txtTitulo.getText().trim();
                String descripcion = txtDescripcion.getText().trim();

                if (titulo.isEmpty() || descripcion.isEmpty()) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "El título y la descripción no pueden estar vacíos.");
                    return;
                }

                // Creamos la encuesta y añadimos las preguntas
                Encuesta encuesta = new Encuesta(titulo, descripcion);
                for (Node node : preguntasBox.getChildren()) {
                    if (node instanceof TextField) {
                        String textoPregunta = ((TextField) node).getText().trim();
                        if (!textoPregunta.isEmpty()) {
                            encuesta.agregarPregunta(new Pregunta(textoPregunta));
                        }
                    }
                }

                encuestas.add(encuesta);
                encuestaDAO.insertarEncuesta(encuesta);

                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Encuesta creada con éxito.");
                start(stage); // Volver al menú principal
            });

            // Botón para regresar al menú
            Button btnVolver = new Button("Volver al Menú Principal");
            btnVolver.getStyleClass().add("boton");
            btnVolver.setOnAction(e -> start(stage));

            // HBox para colocar "Guardar" y "Volver" juntos
            HBox buttonsBox = new HBox(10, btnGuardar, btnVolver);
            buttonsBox.setAlignment(Pos.CENTER);

            bottomBox.getChildren().addAll(btnAgregarPregunta, preguntasBox, buttonsBox);
            layout.setBottom(bottomBox);

            // ----- Creamos la escena y cargamos el CSS -----
            Scene scene = new Scene(layout, 600, 400);
            scene.getStylesheets().add(getClass().getResource("estilos.css").toExternalForm());
            stage.setScene(scene);
        }


    private void mostrarResponderEncuesta(Stage stage) {
        if (encuestas.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Advertencia", "No hay encuestas disponibles.");
            return;
        }

        // Contenedor principal con estilo "panel"
        BorderPane layout = new BorderPane();
        layout.getStyleClass().add("panel");

        // -------- Sección superior: Título --------
        Label lblTitulo = new Label("Responder Encuesta");
        lblTitulo.getStyleClass().add("encabezado");
        HBox topBox = new HBox(lblTitulo);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(10));
        layout.setTop(topBox);

        // -------- Sección central: Selección de encuesta y botón "Responder" --------
        VBox centerBox = new VBox(10);
        centerBox.getStyleClass().add("menu-container");
        centerBox.setAlignment(Pos.CENTER);

        // ComboBox para seleccionar la encuesta
        ComboBox<Encuesta> cbEncuestas = new ComboBox<>();
        cbEncuestas.getItems().addAll(encuestas);
        cbEncuestas.setPromptText("Seleccione una encuesta");
        cbEncuestas.getStyleClass().add("input-field");

        // Botón para proceder a responder la encuesta seleccionada
        Button btnResponder = new Button("Responder");
        btnResponder.getStyleClass().add("boton");
        btnResponder.setOnAction(e -> {
            Encuesta seleccionada = cbEncuestas.getValue();
            if (seleccionada == null) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "Debe seleccionar una encuesta.");
                return;
            }
            // Llamada al método para mostrar las preguntas y recoger respuestas
            mostrarPreguntasEncuesta(stage, seleccionada);
        });

        centerBox.getChildren().addAll(cbEncuestas, btnResponder);
        layout.setCenter(centerBox);

        // -------- Sección inferior: Botón para volver al menú principal --------
        HBox bottomBox = new HBox();
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(10));

        Button btnVolver = new Button("Volver al Menú Principal");
        btnVolver.getStyleClass().add("boton");
        btnVolver.setOnAction(e -> start(stage));

        bottomBox.getChildren().add(btnVolver);
        layout.setBottom(bottomBox);

        // -------- Crear la escena y cargar el CSS --------
        Scene scene = new Scene(layout, 600, 400);
        scene.getStylesheets().add(getClass().getResource("estilos.css").toExternalForm());
        stage.setScene(scene);
    }


    private void mostrarPreguntasEncuesta(Stage stage, Encuesta encuesta) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Label lblTitulo = new Label("Respondiendo: " + encuesta.getTitulo());
        VBox preguntasBox = new VBox(10);

        List<TextField> camposRespuestas = new ArrayList<>();
        for (Pregunta pregunta : encuesta.getPreguntas()) {
            Label lblPregunta = new Label(pregunta.getTexto());
            TextField txtRespuesta = new TextField();
            txtRespuesta.setPromptText("Escriba su respuesta aquí...");
            preguntasBox.getChildren().addAll(lblPregunta, txtRespuesta);
            camposRespuestas.add(txtRespuesta);
        }

        Button btnGuardarRespuestas = new Button("Guardar Respuestas");
        btnGuardarRespuestas.setOnAction(e -> {
            for (int i = 0; i < encuesta.getPreguntas().size(); i++) {
                Pregunta pregunta = encuesta.getPreguntas().get(i);
                String respuestaTexto = camposRespuestas.get(i).getText().trim();
                if (!respuestaTexto.isEmpty()) {
                    Respuesta respuesta = new Respuesta(pregunta, respuestaTexto);
                    encuesta.agregarRespuesta(respuesta);
                }
            }
            encuestaDAO.insertarRespuestas(encuesta.getRespuestas());
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Respuestas guardadas con éxito.");
            start(stage); // Volver al menú principal
        });

        Button btnVolver = new Button("Volver al Menú Principal");
        btnVolver.setOnAction(e -> start(stage));

        layout.getChildren().addAll(lblTitulo, preguntasBox, btnGuardarRespuestas, btnVolver);
        stage.setScene(new Scene(layout, 400, 400));
    }


    private void mostrarVerResultados(Stage stage) {
        if (encuestas.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Advertencia", "No hay encuestas disponibles.");
            return;
        }
        
     // Contenedor principal con estilo "panel"
        BorderPane layout = new BorderPane();
        layout.getStyleClass().add("panel");
        
        // -------- Sección superior: Título --------
        Label lblTitulo = new Label("Resultados de Encuestas");
        lblTitulo.getStyleClass().add("encabezado");
        HBox topBox = new HBox(lblTitulo);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(10));
        layout.setTop(topBox);
        
        // -------- Sección central: Lista de Resultados --------
        ListView<String> listResultados = new ListView<>();
        listResultados.getStyleClass().add("result-list");
        
        for (Encuesta encuesta : encuestas) {
            listResultados.getItems().add("Encuesta: " + encuesta.getTitulo() + " - " + encuesta.getDescripcion());
            for (Pregunta pregunta : encuesta.getPreguntas()) {
                listResultados.getItems().add("  Pregunta: " + pregunta.getTexto());
                List<String> respuestas = encuesta.getRespuestasPorPregunta(pregunta);
                if (respuestas.isEmpty()) {
                    listResultados.getItems().add("    Sin respuestas registradas.");
                } else {
                    for (String respuesta : respuestas) {
                        listResultados.getItems().add("    Respuesta: " + respuesta);
                    }
                }
            }
        }
        layout.setCenter(listResultados);
        
        // -------- Sección de Exportación: Botones para Exportar --------
        HBox exportBox = new HBox(10);
        exportBox.setAlignment(Pos.CENTER);
        exportBox.setPadding(new Insets(10));
        
        Button btnExportCSV = new Button("Exportar a CSV");
        btnExportCSV.getStyleClass().add("boton");
        btnExportCSV.setOnAction(e -> {
            // Usamos FileChooser para seleccionar la ruta de guardado
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar CSV");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                exportarCSV(file.getAbsolutePath());
            }
        });
        
        Button btnExportPDF = new Button("Exportar a PDF");
        btnExportPDF.getStyleClass().add("boton");
        btnExportPDF.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                exportarPDF(file.getAbsolutePath());
            }
        });
        
        exportBox.getChildren().addAll(btnExportCSV, btnExportPDF);
        
        // -------- Sección inferior: Botón para volver al menú --------
        HBox bottomBox = new HBox();
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(10));
        
        Button btnVolver = new Button("Volver al Menú Principal");
        btnVolver.getStyleClass().add("boton");
        btnVolver.setOnAction(e -> start(stage));
        bottomBox.getChildren().add(btnVolver);
        
        // Combina el contenedor de exportación y el botón de volver en un VBox
        VBox bottomContainer = new VBox(10);
        bottomContainer.getChildren().addAll(exportBox, bottomBox);
        layout.setBottom(bottomContainer);
        
        // -------- Crear la escena y cargar el CSS --------
        Scene scene = new Scene(layout, 600, 400);
        scene.getStylesheets().add(getClass().getResource("estilos.css").toExternalForm());
        stage.setScene(scene);
    }



    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    private void mostrarPermisoDenegado() {
        mostrarAlerta(Alert.AlertType.ERROR, "Permiso Denegado", "No tienes permisos para realizar esta acción.");
    }
    
    private void importarEncuestaDesdeAPI(String urlEncuesta) {
        try {
            // 1. Crear el HttpClient
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();

            // 2. Construir la solicitud GET a la URL
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(urlEncuesta))
                    .GET()
                    .build();

            // 3. Enviar la solicitud y obtener la respuesta
            java.net.http.HttpResponse<String> response =
                    client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            // 4. Verificar el código de estado HTTP
            if (response.statusCode() == 200) {
                // 5. Parsear el JSON con Jackson
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Encuesta encuestaImportada = mapper.readValue(response.body(), Encuesta.class);

                // 6. Insertar en la BD
                encuestaDAO.insertarEncuesta(encuestaImportada);

                // 7. Actualizar la lista local si tu interfaz la usa
                encuestas.add(encuestaImportada);

                // 8. Notificar al usuario
                mostrarAlerta(Alert.AlertType.INFORMATION, "Importar Encuesta",
                        "Se ha importado la encuesta desde la API correctamente.");
            } else {
                // Si no fue 200, mostrar error
                mostrarAlerta(Alert.AlertType.ERROR, "Error de API",
                        "No se pudo obtener la encuesta. Código HTTP: " + response.statusCode());
            }

        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error Importando",
                    "Ocurrió un error al importar la encuesta: " + ex.getMessage());
        }
    }


    
    public String obtenerEstadisticas() {
        StringBuilder sb = new StringBuilder();

        for (Encuesta encuesta : encuestas) {
            sb.append("Encuesta: ").append(encuesta.getTitulo()).append("\n");
            // Si deseas incluir la descripción, puedes descomentar la siguiente línea:
            // sb.append("Descripción: ").append(encuesta.getDescripcion()).append("\n");

            for (Pregunta pregunta : encuesta.getPreguntas()) {
                sb.append("  Pregunta: ").append(pregunta.getTexto()).append("\n");
                List<String> respuestas = encuesta.getRespuestasPorPregunta(pregunta);
                if (respuestas.isEmpty()) {
                    sb.append("    Sin respuestas.\n");
                } else {
                    // Calcular la distribución de respuestas
                    java.util.Map<String, Integer> frecuencias = new java.util.HashMap<>();
                    double sumaNumeros = 0;
                    int contadorNumericos = 0;
                    
                    for (String resp : respuestas) {
                        // Contar respuesta
                        frecuencias.put(resp, frecuencias.getOrDefault(resp, 0) + 1);
                        // Intentar parsear la respuesta a número y acumular para promedio
                        try {
                            double valor = Double.parseDouble(resp);
                            sumaNumeros += valor;
                            contadorNumericos++;
                        } catch (NumberFormatException ex) {
                            // La respuesta no es numérica; seguimos
                        }
                    }
                    
                    // Mostrar las frecuencias y porcentajes
                    for (java.util.Map.Entry<String, Integer> entry : frecuencias.entrySet()) {
                        int frecuencia = entry.getValue();
                        double porcentaje = (frecuencia * 100.0) / respuestas.size();
                        sb.append("    ").append(entry.getKey())
                          .append(": ")
                          .append(frecuencia)
                          .append(" respuestas (")
                          .append(String.format("%.2f", porcentaje))
                          .append("%)\n");
                    }
                    
                    // Si hay respuestas numéricas, calcular y mostrar el promedio
                    if (contadorNumericos > 0) {
                        double promedio = sumaNumeros / contadorNumericos;
                        sb.append("    Promedio (solo numéricas): ")
                          .append(String.format("%.2f", promedio))
                          .append("\n");
                    }
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    
    
    public void exportarCSV(String rutaArchivo) {
        try (FileWriter writer = new FileWriter(rutaArchivo)) {
            // Escribimos una cabecera
            writer.append("Título,Descripción,Pregunta,Respuesta\n");
            // Recorremos las encuestas
            for (Encuesta encuesta : encuestas) {
                String titulo = encuesta.getTitulo();
                String descripcion = encuesta.getDescripcion();
                // Recorremos las preguntas de cada encuesta
                for (Pregunta pregunta : encuesta.getPreguntas()) {
                    // Obtenemos las respuestas asociadas
                    List<String> respuestas = encuesta.getRespuestasPorPregunta(pregunta);
                    // Si la pregunta no tiene respuestas, escribimos "Sin respuesta"
                    if (respuestas.isEmpty()) {
                        writer.append(String.format("\"%s\",\"%s\",\"%s\",\"Sin respuesta\"\n",
                                titulo, descripcion, pregunta.getTexto()));
                    } else {
                        // Para cada respuesta, escribir una línea
                        for (String respuesta : respuestas) {
                            writer.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\"\n",
                                    titulo, descripcion, pregunta.getTexto(), respuesta));
                        }
                    }
                }
            }
            writer.flush();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Exportación CSV", "Exportación a CSV completada correctamente.");
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Exportación CSV", "Error al exportar a CSV: " + e.getMessage());
        }
    }
    public void exportarPDF(String rutaArchivo) {
        PDDocument document = new PDDocument();
        try {
            PDPage page = new PDPage();
            document.addPage(page);
            
            // Carga la fuente TrueType desde los recursos
            InputStream fontStream = getClass().getResourceAsStream("DejaVuSans.ttf");
            if (fontStream == null) {
                System.err.println("No se encontró la fuente DejaVuSans.ttf");
                mostrarAlerta(Alert.AlertType.ERROR, "Exportación PDF", "No se encontró la fuente necesaria.");
                return;
            }
            // Cargar la fuente TTF en el documento
            PDType0Font fuenteTTF = PDType0Font.load(document, fontStream);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Configuración de inicio
            contentStream.beginText();
            contentStream.setFont(fuenteTTF, 12);  // Tamaño 12
            contentStream.newLineAtOffset(50, 750);  // Posición inicial

            // Escribimos un título
            contentStream.showText("Resultados de Encuestas");
            contentStream.newLineAtOffset(0, -20);

            // Recorremos las encuestas para escribir sus datos
            for (Encuesta encuesta : encuestas) {
                contentStream.showText("Encuesta: " + encuesta.getTitulo() + " - " + encuesta.getDescripcion());
                contentStream.newLineAtOffset(0, -15);
                for (Pregunta pregunta : encuesta.getPreguntas()) {
                    contentStream.showText("  Pregunta: " + pregunta.getTexto());
                    contentStream.newLineAtOffset(0, -15);
                    List<String> respuestas = encuesta.getRespuestasPorPregunta(pregunta);
                    if (respuestas.isEmpty()) {
                        contentStream.showText("    Sin respuestas registradas.");
                        contentStream.newLineAtOffset(0, -15);
                    } else {
                        for (String respuesta : respuestas) {
                            contentStream.showText("    Respuesta: " + respuesta);
                            contentStream.newLineAtOffset(0, -15);
                        }
                    }
                }
                // Ojo: si el offset acumulado se vuelve demasiado bajo,
                // quizás debas iniciar una nueva página en este punto.
                contentStream.newLineAtOffset(0, -10);
            }
            contentStream.endText();
            contentStream.close();

            document.save(rutaArchivo);
            mostrarAlerta(Alert.AlertType.INFORMATION, "Exportación PDF", "Exportación a PDF completada correctamente.");
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Exportación PDF", "Error al exportar a PDF: " + e.getMessage());
        } finally {
            try {
                document.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}






