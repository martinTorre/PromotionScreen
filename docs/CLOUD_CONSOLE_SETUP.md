# Configuración en Google Cloud Console

Tu proyecto de Firebase **es el mismo** que el de Google Cloud. Solo tienes que entrar a la consola de Google Cloud y activar la API y las credenciales.

---

## 1. Abrir el proyecto en Google Cloud Console

1. Entra a [Firebase Console](https://console.firebase.google.com/) e inicia sesión.
2. Abre tu proyecto **Dermy Pharma**.
3. En el menú izquierdo, haz clic en el **engranaje** (Configuración del proyecto).
4. En el menú que se despliega, haz clic en **“General”** (no te quedes solo en el desplegable).
5. Se abre la página de configuración. Baja hasta la sección **“Tu proyecto”** (donde aparece el ID del proyecto).
6. Ahí verás el enlace **“Administrar en Google Cloud Console”**. Haz clic para abrir Google Cloud con el mismo proyecto.

**Si no ves ese enlace:** ve directo a [Google Cloud Console](https://console.cloud.google.com/). Arriba haz clic en el selector de proyecto y elige **“Dermy Pharma”** (el proyecto de Firebase aparece también en la lista de Google Cloud).

---

## 2. Activar la Google Drive API

1. En el menú lateral de Google Cloud Console (☰), ve a **“APIs y servicios”** → **“Biblioteca”**.
2. Busca **“Google Drive API”**.
3. Haz clic en **“Google Drive API”** y luego en **“Habilitar”**.

---

## 3. Pantalla de consentimiento de OAuth (si no está hecha)

Necesaria para que el “Iniciar sesión con Google” funcione.

1. Menú lateral → **“APIs y servicios”** → **“Pantalla de consentimiento de OAuth”**.
2. Si te pregunta **tipo de usuario**:
   - Elige **“Externo”** (para probar con cualquier cuenta de Gmail).
   - Confirma.
3. Rellena solo lo obligatorio:
   - **Nombre de la aplicación:** por ejemplo “Pantalla promociones”.
   - **Correo de asistencia:** tu email.
   - **Dominios autorizados:** puedes dejarlo en blanco para desarrollo.
4. En **“Ámbitos”** → **“Añadir o quitar ámbitos”**:
   - Busca **“Google Drive API”**.
   - Marca **“…/auth/drive.readonly”** (Ver archivos de Drive).
   - Guarda.
5. **Usuarios de prueba** (si dejaste “Externo”):
   - Añade las cuentas de Gmail que usarás para iniciar sesión en la app (las que tengan acceso a la carpeta de Drive).
6. Guarda la pantalla de consentimiento.

---

## 4. Crear credenciales OAuth para Android

1. Menú lateral → **“APIs y servicios”** → **“Credenciales”**.
2. Arriba: **“+ Crear credenciales”** → **“ID de cliente de OAuth”**.
3. **Tipo de aplicación:** **Android**.
4. **Nombre:** por ejemplo “PromotionScreen Android”.
5. **Nombre del paquete:**  
   `com.dermy.pharma.promotionscreen`  
   (debe coincidir con el `applicationId` del `build.gradle.kts`).
6. **Huella digital del certificado SHA-1:**  
   Pega aquí el SHA-1 (ver paso 5).
7. **Crear**.

Repite el mismo proceso si usas **dos keystores** (por ejemplo debug y release): crea un ID de cliente Android por cada SHA-1.

---

## 5. Obtener el SHA-1 de tu keystore

### Debug (desarrollo en tu PC)

En terminal, en la raíz del proyecto (PromotionScreen):

```bash
./gradlew signingReport
```

En la sección **Variant: debug** copia el valor de **SHA1**.

O con `keytool` (Java en el PATH):

```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

En la salida, copia la línea **SHA1** (sin los dos puntos si la consola no los pide).

### Release (APK para producción)

Usa el keystore con el que firmas el release y el alias correspondiente:

```bash
keytool -list -v -keystore /ruta/a/tu/keystore.jks -alias tu_alias
```

Copia el **SHA1** y crea (o edita) un segundo ID de cliente Android en Cloud Console con ese SHA-1 y el mismo package name.

---

## 6. Resumen de comprobaciones

| Qué | Dónde |
|-----|--------|
| Proyecto | El mismo en Firebase y en Google Cloud |
| Drive API | Activada en “APIs y servicios” → Biblioteca |
| OAuth | Pantalla de consentimiento con ámbito `drive.readonly` y usuarios de prueba (si es externo) |
| Credenciales | ID de cliente OAuth tipo **Android**, package `com.dermy.pharma.promotionscreen`, SHA-1 del keystore |

Cuando todo esté así, la app podrá:
- Mostrar “Iniciar sesión con Google”.
- Pedir acceso solo lectura a Drive.
- Listar la carpeta que pongas en **DriveUrl** en Remote Config.

Si algo falla (por ejemplo “Sign in failed” o “API not enabled”), revisa: Drive API activada, SHA-1 y package correctos en el ID de cliente Android, y cuenta en “Usuarios de prueba” si la app está en modo prueba.
