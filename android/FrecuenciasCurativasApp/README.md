# Curati App para Android

Proyecto Android en español para Google Play. Presenta `https://infiniti-ia.com/free` dentro de un WebView nativo.

## Datos de publicación

- Nombre visible: **Curati App**
- Paquete: `com.apolosolinvictus.frecuenciascurativas`
- Versión: `1.1` (`versionCode 2`)
- SDK objetivo: 36
- Privacidad: `https://infiniti-ia.com/privacidad1`
- Producto no consumible: `curati_full_access`
- Precio configurado en Play Console: US$7.77, pago único

No hay prueba gratuita, mensualidad, renovación automática ni cargos posteriores. La app consulta y restaura la compra permanente con Google Play Billing.

## Abrir y publicar

1. Abre esta carpeta en Android Studio con JDK 17 y Android SDK API 36.
2. Para una prueba interna, genera o instala el módulo `app`.
3. Para publicar, usa **Build > Generate Signed Bundle / APK > Android App Bundle** y selecciona `release`.
4. El AAB firmado generado localmente está en `app/build/outputs/bundle/release/app-release.aab`.
5. En Play Console crea la app con el paquete indicado, configura el producto de una sola compra, registra la política de privacidad y sube el AAB a prueba interna.

La clave de carga y su contraseña se conservan fuera de Git. Haz una copia segura antes de publicar; se necesita la misma clave para futuras actualizaciones de esta aplicación.
