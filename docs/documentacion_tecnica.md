# Documentación 

## Resumen

Ritmo es una aplicación de escritorio Java con interfaz Swing y reproducción/metadatos multimedia mediante JavaFX Media. Maven administra la compilación y las dependencias. El punto de entrada está en `co.edu.uptc.runner.Run`.

## Requisitos y comandos

- JDK 21 o posterior (`maven.compiler.release` está configurado en 21).
- Apache Maven.
- Acceso a Internet para las consultas de Jamendo.

Desde la raíz del repositorio:

```powershell
mvn install package
mvn clean package 
```

El plugin JavaFX está configurado en `pom.xml` con la clase principal `co.edu.uptc.runner.Run`.

## Organización por paquetes

```text
src/co/edu/uptc/
├── model/       dominio, servicios, reproductor y persistencia
├── presenter/   coordinación de eventos y estado de la aplicación
├── runner/      punto de entrada
└── view/        ventana, paneles y diálogos Swing
```

### Vista

`ViewFrame` construye la ventana y registra las secciones definidas en `IFrame`. `MainPanel` contiene el menú y la barra del reproductor; cada funcionalidad tiene su panel en `view/`. Las vistas envían eventos al `Presenter` mediante listeners y no ejecutan directamente operaciones del modelo.

### Presentador

`Presenter` instancia el modelo, crea los gestores de favoritos, playlists, historial y cola, conecta los listeners y sincroniza los paneles. También gestiona login, búsquedas, importación de MP3, reproducción, persistencia y atajos.

### Modelo y servicios

- `Model` implementa el catálogo local, valida canciones y usa JavaFX Media para leer metadatos y duración de MP3.
- `AudioPlayer` reproduce archivos MP3 locales y URL de audio HTTP/HTTPS; controla estado, progreso, volumen y errores.
- `JamendoService` consulta la API pública de tracks para búsquedas y recomendaciones.
- `SongValidator` y `PlaylistValidator` centralizan validaciones.
- `FavoritesManager`, `PlaylistManager`, `HistoryManager` y `PlaybackQueue` gestionan las colecciones correspondientes.

La interfaz Swing se actualiza en el hilo de eventos de Swing. Las búsquedas remotas y la lectura de metadatos se ejecutan fuera de ese hilo. JavaFX Media crea y controla sus reproductores en el hilo de JavaFX.

## Inicio de sesión y almacenamiento

Al iniciar, `Presenter` solicita una carpeta para datos si no hay una ruta válida guardada en las preferencias del sistema. `UserStore` almacena `users.properties` directamente en esa carpeta. Cuando aún no existe el archivo, crea la cuenta inicial `admin/admin`. Las cuentas registradas son de rol `user`; el rol se comprueba al iniciar sesión.

Los datos de música se guardan por cuenta:

```text
<carpeta-elegida>/
├── users.properties
└── usuarios/
    ├── user-<nombre>/musica.properties
    └── admin-<nombre>/musica.properties
```

`MusicDataStore` serializa en formato Java Properties la biblioteca, sus datos de canción, favoritos, playlists y entradas del historial. Las ubicaciones de audio y portadas se guardan como rutas; no se copian los medios al directorio de datos.

Las preferencias de la interfaz y del reproductor (por ejemplo, tema, volumen y ubicación de datos) se guardan con `java.util.prefs.Preferences`, fuera de los archivos de datos por cuenta.

## Búsquedas e integración con Jamendo

`JamendoService` consulta `https://api.jamendo.com/v3.0/tracks/`. La búsqueda general usa el parámetro de búsqueda de tracks; la búsqueda por género usa tags y, si no hay resultados, fuzzy tags. Las recomendaciones solicitan una cantidad mayor de resultados y la vista Home presenta un máximo de cinco.

El ID de cliente se obtiene, en este orden, de:

1. Propiedad de Java `jamendo.clientId`.
2. Variable de entorno `JAMENDO_CLIENT_ID`.
3. Valor predeterminado incluido en el servicio.

Para cambiarlo al ejecutar:

```powershell
$env:JAMENDO_CLIENT_ID = "tu-client-id"
```

No se requiere configurar ese valor para la biblioteca local, debido a que ya se le coloca una clave para cuando en blank ese valor, pero debido a buenas practicas se deja la opcion implementada. 

## Interfaz y estilos

`Palette` define los colores base; `ThemeManager` aplica tema claro u oscuro y refresca componentes dinámicos. `SettingsPanel` persiste las preferencias del tema y de los colores por género. `MyPanel`, `MyButton`, `MySlider` y `MyTextField` son componentes reutilizables para la interfaz.
