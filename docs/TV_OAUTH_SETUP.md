# Inicio de sesión en TV (Chromecast con Google TV)

En dispositivos TV la app usa el **flujo OAuth 2.0 para dispositivos con entrada limitada**: en la TV se muestra un código y una URL; el usuario abre la URL en el móvil o PC, introduce el código y autoriza.

---

## Modo por nombres fijos (imagen1, imagen2, video1, video2…) — recomendado

Si subes siempre los archivos con **el mismo nombre** (imagen1, imagen2, … y video1, video2, …), la app puede descubrirlos sin listar Drive y **sin necesidad de iniciar sesión** (en TV ni en móvil).

1. **Sube los archivos** a una URL base accesible por HTTPS, con estos nombres:
   - Imágenes: `imagen1.jpg`, `imagen2.jpg`, `imagen3.jpg`, … (o la extensión que uses).
   - Vídeos: `video1.mp4`, `video2.mp4`, …

2. **En Firebase Remote Config** define:
   - **`BaseMediaUrl`**: URL base donde están los archivos, **con barra final**.  
     Ejemplo: `https://tu-bucket.storage.googleapis.com/promos/`  
     La app probará `imagen1.jpg`, `imagen2.jpg`, … y `video1.mp4`, `video2.mp4`, … bajo esa base hasta que una petición falle (no exista el siguiente).
   - **`ImageExtension`** (opcional): extensión de las imágenes. Por defecto `jpg`.
   - **`VideoExtension`** (opcional): extensión de los vídeos. Por defecto `mp4`.

3. Con **`BaseMediaUrl`** configurado, la app **no pide inicio de sesión** y funciona igual en TV y en móvil. Puedes usar Firebase Storage, un CDN o cualquier servidor que sirva los archivos por URL directa.

## Crear el cliente OAuth para TV

1. **Abrir Google Cloud Console**  
   Entra en [https://console.cloud.google.com/](https://console.cloud.google.com/) e inicia sesión. Arriba, en el selector de proyecto, elige el mismo proyecto que usas para la app (p. ej. el de Firebase / Dermy Pharma).

2. **Ir a Credenciales**  
   En el menú lateral (☰) → **"APIs y servicios"** → **"Credenciales"**.

3. **Crear el ID de cliente**  
   Arriba en la página, clic en **"+ Crear credenciales"** → **"ID de cliente de OAuth"**.

4. **Configurar el tipo de aplicación**  
   - Si te pide **"Configurar la pantalla de consentimiento"** (porque aún no existe), complétala primero (nombre de la app, correo de asistencia, ámbito `drive.readonly`, etc.) y vuelve a Credenciales.  
   - En **"Tipo de aplicación"**, abre el desplegable y elige **"TVs y dispositivos de entrada limitada"** (en inglés: *TVs and Limited Input devices*).  
   - **Nombre:** por ejemplo `PromotionScreen TV`.  
   - Clic en **"Crear"**.

5. **Guardar ID y secreto**  
   En el cuadro que aparece se muestran el **ID de cliente** (termina en `...apps.googleusercontent.com`) y el **Secreto del cliente**. Cópialos; los necesitas en el siguiente paso. Puedes volver a ver el secreto más tarde en la lista de credenciales (icono de copiar o “Mostrar secreto”).

## Configurar la app

1. En la raíz del proyecto crea o edita **`local.properties`** (este archivo no se sube a Git).
2. Añade estas líneas (sustituye por tu ID y secreto reales):

```properties
TV_OAUTH_CLIENT_ID=TU_CLIENT_ID_TV.apps.googleusercontent.com
TV_OAUTH_CLIENT_SECRET=TU_CLIENT_SECRET
```

3. Vuelve a compilar e instala la app en la TV.
4. En la pantalla de inicio de sesión en TV aparecerá el botón **"Iniciar sesión con código"**. Al pulsarlo se mostrará un código y la URL (p. ej. `https://www.google.com/device`). El usuario debe abrir esa URL en otro dispositivo, introducir el código y autorizar el acceso a Drive.

**Nota:** El flujo para TV solo permite el ámbito `drive.file`, que no permite listar una carpeta arbitraria de Drive. Por eso en TV la app puede mostrar "No hay medios en la carpeta". Para solucionarlo, usa la opción de lista por URL (ver más abajo).

---

## Mostrar medios en TV: lista desde URL

En TV la app **no** puede listar una carpeta de Drive por ID (limitación de `drive.file`). Para tener promos en la TV puedes servir la lista desde una URL.

1. **En Firebase Remote Config** añade el parámetro **`MediaListUrl`** (nombre exacto).
2. Como valor, pon la URL de un JSON que devuelva la lista de medios. Ejemplo de formato:

```json
[
  { "url": "https://ejemplo.com/imagen1.jpg", "type": "IMAGE" },
  { "url": "https://ejemplo.com/video1.mp4", "type": "VIDEO", "fileId": null }
]
```

- **url**: enlace directo a la imagen o al vídeo (para vídeos debe ser una URL que se pueda reproducir directamente).
- **type**: `"IMAGE"` o `"VIDEO"`.
- **fileId**: opcional; puedes omitirlo o poner `null` cuando usas URLs directas.

3. Esa URL puede ser:
   - Un **Cloud Function** (o backend) que lea la carpeta de Drive con `drive.readonly` y devuelva este JSON.
   - Un **archivo JSON** alojado en Firebase Hosting, Cloud Storage o cualquier servidor accesible por HTTPS.

4. En dispositivos **TV**, la app usará `MediaListUrl` si está definido; en **móvil** se sigue usando la carpeta de Drive (`DriveUrl`) como hasta ahora.
