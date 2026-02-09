# Medios desde Firebase Storage (carpeta pública)

La app **solo** usa Firebase Storage para los medios. Carga imágenes y vídeos desde una carpeta pública, con **nombres variables**. No hace falta iniciar sesión (ni en móvil ni en TV).

**URL por defecto:** `gs://dermy-pharma.firebasestorage.app/dermy` (carpeta `dermy` en el bucket). Puedes cambiarla en Remote Config con la clave **StorageBaseUrl**.

## 1. Crear la carpeta en Firebase Storage

1. En [Firebase Console](https://console.firebase.google.com/) → **Storage** → **Get started** (si no lo has hecho).
2. Crea o usa la carpeta de las promos (en tu caso `dermy`).
3. Sube ahí tus archivos con **cualquier nombre** (ej. `oferta_enero.jpg`, `lanzamiento.mp4`).
4. Asegúrate de que las **reglas de Storage** permitan lectura pública para esa ruta, por ejemplo:

```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /dermy/{allPaths=**} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```

## 2. Archivo list.json

En **la misma carpeta** (ej. `dermy`) sube un archivo llamado **`list.json`** que defina qué archivos se muestran. El orden del JSON es el orden de reproducción.

**Formato con nombres (recomendado):** la app construye la URL a partir de la base + el nombre.

```json
[
  { "name": "oferta_enero.jpg", "type": "IMAGE" },
  { "name": "banner_febrero.png", "type": "IMAGE" },
  { "name": "lanzamiento.mp4", "type": "VIDEO" },
  { "name": "spot_promo.mp4", "type": "VIDEO" }
]
```

**Formato con URLs completas:** si prefieres poner la URL de cada archivo:

```json
[
  { "url": "https://firebasestorage.googleapis.com/v0/b/TU_BUCKET/o/promos%2Foferta.jpg?alt=media", "type": "IMAGE" },
  { "url": "https://...", "type": "VIDEO" }
]
```

- **type** debe ser `"IMAGE"` o `"VIDEO"`.
- El orden del array es el orden de reproducción en la app.

## 3. URL base (StorageBaseUrl)

La **URL base** es la de la carpeta en Firebase Storage, **con barra final**.

Para tu carpeta (`gs://dermy-pharma.firebasestorage.app/dermy`) la URL base es:

```
https://firebasestorage.googleapis.com/v0/b/dermy-pharma.firebasestorage.app/o/dermy%2F
```

Ya está configurada por defecto en la app. Si usas otra carpeta, en Remote Config cambia **StorageBaseUrl**.

## 4. Remote Config (opcional)

Si quieres otra carpeta, en Firebase **Remote Config** edita **StorageBaseUrl** con la URL base de esa carpeta (con `/` al final).

## Resumen

| Qué | Dónde |
|-----|--------|
| Carpeta | Firebase Storage, en tu caso `dermy` |
| Archivos | Cualquier nombre (imágenes y vídeos) |
| Listado | Archivo **list.json** en la misma carpeta |
| Orden | El que tenga el array en `list.json` |
