# Manual de usuario de Ritmo

## 1. ¿Qué es Ritmo?

Ritmo permite explorar recomendaciones y música de Jamendo, reproducir pistas en línea o archivos MP3 del computador, y organizar canciones en una biblioteca personal. Cada cuenta mantiene sus propios favoritos, playlists e historial.

## 2. Requisitos y primer inicio

Se necesita Java 21 o posterior para ejecutar la aplicación. Para buscar música en Jamendo se requiere conexión a Internet; las canciones locales siguen disponibles sin conexión.

Al abrir Ritmo:

1. Selecciona una carpeta donde la aplicación pueda guardar datos. Ritmo recuerda la ubicación en las preferencias del sistema operativo.
2. En una instalación nueva, inicia sesión con el administrador inicial:
   - **Usuario:** `admin`
   - **Contraseña:** `admin`
3. Para crear una cuenta personal, usa **Crear cuenta**. El registro crea una cuenta de usuario normal; no se puede escoger el rol desde ese formulario.

Los nombres de usuario admiten de 3 a 20 letras ASCII (`a-z`), números, puntos, guiones o guiones bajos. No distinguen entre mayúsculas y minúsculas. La contraseña debe tener al menos 4 caracteres. La aplicación no tiene una opción para cambiar o recuperar contraseñas.

> La cuenta inicial `admin/admin` se crea para tener en el sistema un administrador. 

## 3. Navegación

Después de iniciar sesión, usa el menú lateral:

- **Home:** recomendaciones por género, artistas, álbumes y canciones. Las recomendaciones vienen de Jamendo. Haz doble clic sobre una canción para reproducirla; selecciona un artista o álbum para buscarlo en **Explorar**. El corazón agrega o quita la canción de favoritos.
- **Explorar:** busca música en Jamendo o selecciona uno de los géneros disponibles. El modo **Por artista** permite buscar texto; **Por género** habilita la búsqueda por género. En los resultados puedes reproducir, marcar favoritos o agregar una canción a una playlist. El doble clic sobre una fila también reproduce.
- **Biblioteca:** administra las canciones importadas desde el computador. Puedes buscar por canción, artista o género, reproducir una pista o las canciones visibles, marcar favoritos, agregar a una playlist, editar el título o la portada y quitar una canción de la biblioteca.
- **Favoritos:** muestra las canciones marcadas con un corazón. El buscador filtra la lista y **Reproducir todo** reproduce las canciones visibles. **Quitar de favoritos** no elimina la canción de la biblioteca.
- **Playlists:** crea y selecciona listas, agrega canciones desde la biblioteca, reproduce la lista, la renombra o la elimina. También puedes quitar canciones individuales de una playlist.
- **Historial:** muestra las reproducciones recientes primero. Puedes reproducir una entrada o borrar todo el historial; la aplicación pide confirmación.
- **Configuración:** cambia entre tema claro y oscuro, personaliza el color del botón **Cambiar** de Pop, Rock, Electrónica o Folk y consulta los atajos. La tarjeta **Mi cuenta** muestra el usuario actual y permite cerrar sesión.

## 4. Importar música a la biblioteca

En **Biblioteca**, usa **+ Agregar canciones** para abrir las opciones:

### Importar archivos MP3

Selecciona **Importar MP3** y elige uno o varios archivos. Ritmo lee los metadatos disponibles (título, artista, álbum, género y duración); si faltan, usa valores predeterminados. La importación también está disponible desde **Explorar**.

### Importar desde una carpeta

En **+ Agregar canciones**, elige una carpeta y pulsa **Importar carpeta**. Se buscan archivos `.mp3` dentro de esa carpeta y sus subcarpetas.

### Agregar una canción manualmente

Completa el título y el género, que son obligatorios. Selecciona un archivo MP3 válido; artista, álbum e imagen de portada son opcionales. Las portadas admitidas son `.png`, `.jpg` y `.jpeg`.

Los duplicados no se agregan de nuevo. Los archivos de audio no se copian ni se mueven: si los renombras, los mueves o los eliminas después de importarlos, Ritmo ya no podrá reproducirlos desde la biblioteca.

## 5. Reproducción y cola

La barra inferior permanece disponible en las secciones de la aplicación:

- **▶ / ⏸:** inicia, pausa o reanuda la reproducción.
- **⏮ / ⏭:** reproduce la pista anterior o siguiente de la cola.
- **Barra de progreso:** arrástrala para saltar a otra posición cuando la pista esté cargada.
- **Vol.:** ajusta el volumen; se recuerda entre ejecuciones.
- **Cola:** muestra las pistas pendientes y permite quitar la seleccionada. Cuando termina una canción, Ritmo continúa con la siguiente.

También están disponibles estos atajos, salvo cuando el cursor está en un campo de texto:

| Acción | Tecla |
|---|---|
| Pausar o reanudar | `Espacio` |
| Siguiente pista | `→` |
| Pista anterior | `←` |

## 6. Playlists

1. Abre **Playlists**, escribe un nombre y pulsa **Crear**.
2. Selecciona la playlist y pulsa **Agregar desde biblioteca**.
3. Busca por canción, artista, álbum o género y pulsa **+ Agregar** en cada pista.
4. Usa **▶ Reproducir** para reproducir la lista completa. En la lista de canciones puedes reproducir o quitar una pista.

Los nombres vacíos o repetidos no se aceptan. Eliminar una playlist o quitar una canción de ella no elimina canciones de la biblioteca.

## 7. Historial y cierre de sesión

El historial se ordena de la reproducción más reciente a la más antigua. Para eliminarlo, pulsa **Borrar historial** y confirma la acción. El historial eliminado no se puede recuperar.

Usa **Cerrar sesión** en el menú lateral o en **Configuración** para guardar los datos actuales y volver al acceso. Los cambios también se guardan durante el uso y al cerrar la ventana.

## 8. Dónde se guardan los datos

En la carpeta que elegiste al iniciar:

- `users.properties`: cuentas y credenciales protegidas.
- `usuarios/user-<nombre>/musica.properties`: biblioteca, favoritos, playlists e historial de cada usuario.
- `usuarios/admin-<nombre>/musica.properties`: esos mismos datos para una cuenta administradora.

La aplicación guarda rutas de archivos locales, no los archivos de audio o imagen. Para conservar una biblioteca al cambiar de computador, copia tanto los archivos multimedia como la carpeta de datos y mantén las rutas accesibles. También se recomienda cerrar Ritmo antes de copiar los datos.

## 9. Solución de problemas

- **No puedo iniciar sesión:** comprueba el nombre, la contraseña y el rol elegido (Usuario o Administrador). El registro crea cuentas de usuario, no de administrador.
- **La aplicación vuelve a pedir una carpeta:** la ubicación guardada ya no existe o no está disponible. Selecciona la carpeta de datos correcta.
- **No aparecen recomendaciones o resultados:** revisa la conexión a Internet e inténtalo de nuevo más tarde. Las búsquedas dependen del servicio Jamendo.
- **No se importa un archivo:** comprueba que sea un MP3 legible y que no esté dañado. Los duplicados se omiten.
- **Una canción local no se reproduce:** verifica que el archivo siga en la misma ubicación y que la cuenta tenga permiso para leerlo.
- **No aparece una pista en la biblioteca:** confirma que la importación terminó y revisa el mensaje de estado; los metadatos que falten pueden sustituirse por valores predeterminados.
