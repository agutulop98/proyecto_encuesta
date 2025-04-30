# Sistema de Encuestas

Este repositorio contiene un sistema de encuestas completo, dividido en dos partes:

- Una **aplicación de escritorio JavaFX** con conexión a base de datos, autenticación, exportación y funcionalidades avanzadas.
- Un **microservicio REST en Spring Boot** que expone encuestas vía JSON para ser importadas por la aplicación JavaFX.

---

## 📦 Estructura del Proyecto

```txt
SistemaEncuesta2/
├── SistemaEncuesta2/
│   ├── ApiEncuesta/                  # Proyecto Spring Boot (API REST)
│   ├── dejavu-sans/                  # Fuentes TTF para PDF export
│   └── Jar Usados/                   # Librerías externas (.jar)
```

### 📁 `ApiEncuesta/`

Proyecto Spring Boot que expone encuestas JSON para importar desde la aplicación JavaFX.

Estructura relevante:

```txt
ApiEncuesta/
├── src/main/java/com/example/apiencuesta/
│   ├── ApiEncuestaApplication.java
│   └── controller/EncuestaController.java
├── src/main/resources/application.properties
├── pom.xml
```

Puedes ejecutarlo con:

```bash
cd SistemaEncuesta2/ApiEncuesta
mvn spring-boot:run
```

Luego acceder a una encuesta de ejemplo en:
```
http://localhost:8080/api/encuesta/123
```

### 📁 `dejavu-sans/`

Fuentes utilizadas para exportar resultados a PDF (mediante Apache PDFBox).

### 📁 `Jar Usados/`

Librerías necesarias para la ejecución de la aplicación JavaFX:

- `jackson-core`, `jackson-annotations` → Parseo de JSON.
- `activation.jar`, `mail.jar` → Envío de correos.
- `pdfbox-app.jar` → Exportación de resultados a PDF.

---

## ⚙️ Requisitos

| Herramienta   | Versión recomendada |
|---------------|---------------------|
| JDK           | 17 o 21             |
| JavaFX        | 21.0.5              |
| MySQL         | 8+                  |

---

## 🚀 Cómo Ejecutar

1. Ejecuta el microservicio API:
```bash
cd SistemaEncuesta2/ApiEncuesta
mvn spring-boot:run
```

2. Abre el proyecto JavaFX en Eclipse y asegúrate de:
   - Cargar los `.jar` desde `Jar Usados/`
   - Tener configurada correctamente la ruta de JavaFX en el módulo de ejecución.
   - Conexión activa a la base de datos.

3. Desde la app de escritorio, puedes importar encuestas desde:
```
http://localhost:8080/api/encuesta/{id}
```

---

## 📄 Licencia

Este proyecto es de uso académico y educativo. Las fuentes tipográficas DejaVu se incluyen bajo su respectiva licencia.
Este proyecto está bajo la licencia MIT. Consulta el archivo LICENSE para más detalles.

---

**Desarrollado por:** Sofía Sánchez, Agustín Gutiérrez, Montserrat Cividanes, y Samuel Titterton. 🎓
