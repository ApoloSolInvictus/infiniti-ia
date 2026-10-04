# Frecuencias Curativas App para Android

Este proyecto contiene la app Android para Google Play. Presenta el sintetizador español de Infiniti IA dentro de un WebView nativo y carga:

`https://infiniti-ia.com/free`

La política pública de privacidad para esta versión está en:

`https://infiniti-ia.com/privacidad1`

## Configuración

1. Instala la versión actual de Android Studio en un equipo con JDK 17.
2. En SDK Manager instala Android 16 (API 36) y sus herramientas de compilación.
3. Abre la carpeta `android/FrecuenciasCurativasApp` como proyecto existente.
4. Espera a que Gradle sincronice y selecciona el módulo `app`.
5. Ejecuta en un emulador o dispositivo Android conectado. Confirma que el sintetizador carga, que el botón **Encender Osciladores** inicia el audio, que la automatización funciona y que los enlaces externos se abren fuera de la app.

El proyecto usa `compileSdk 36`, `targetSdk 36`, `minSdk 26` y el identificador de paquete:

`com.apolosolinvictus.frecuenciascurativas`

La app no solicita micrófono, ubicación, contactos, cámara ni almacenamiento. Solo declara `android.permission.INTERNET` para cargar el sitio remoto.

## Generar el AAB

Para una prueba local, abre el proyecto en Android Studio y usa **Run** con un emulador o dispositivo conectado. Si tienes Gradle instalado en tu equipo, también puedes ejecutar `gradle assembleDebug` desde esta carpeta.

Para Google Play se debe generar un **Android App Bundle** de release desde Android Studio con **Build > Generate Signed Bundle / APK > Android App Bundle**, seleccionar `release` y firmarlo con un keystore propio. El keystore, sus contraseñas y las claves de Play no deben guardarse en Git.

Antes de la primera subida, configura Play App Signing en Play Console y conserva una copia segura del keystore de carga. Incrementa `versionCode` en `app/build.gradle` para cada actualización.

## Checklist de Google Play

1. Crea o activa una cuenta de desarrollador de Google Play y completa la verificación solicitada.
2. En Play Console crea una app nueva con el nombre **Frecuencias Curativas App** y el paquete `com.apolosolinvictus.frecuenciascurativas`.
3. Completa la ficha: descripción en español, icono, capturas reales, categoría, clasificación de contenido, correo de contacto y URL de soporte.
4. En **App content**, registra `https://infiniti-ia.com/privacidad1` como política de privacidad.
5. Completa **Data safety** según el comportamiento real de la app y de los proveedores que recibe la página remota. Aunque la app no tenga cuentas ni analítica nativa, las solicitudes web pueden producir registros técnicos en alojamiento, CDN y proveedores de recursos; la declaración debe ser exacta.
6. Completa anuncios, acceso a la app y demás formularios de contenido. La app no requiere inicio de sesión.
7. Sube primero el AAB a una prueba interna, instala en un dispositivo físico y prueba audio, navegación, compartir, privacidad, pérdida de red, rotación y recuperación.
8. Pasa a prueba cerrada si Play Console lo solicita para la cuenta, corrige advertencias y luego envía el release a producción.

## Revisión y alcance

La app incluye controles nativos de navegación, compartir, seguridad sonora y acceso a privacidad, pero su experiencia principal proviene de un WebView remoto. Google puede revisar si la app ofrece suficiente valor propio y puede pedir mejoras. En las notas de revisión describe que es un sintetizador interactivo de frecuencias, que no requiere cuenta, que el audio comienza después de una acción del usuario y que no es un tratamiento médico. No prometas resultados médicos o psicológicos garantizados.

La compilación y firma final requieren Android Studio en Windows, macOS o Linux. El entorno de desarrollo compartido no contiene Android Studio, Gradle ni un keystore privado, por lo que el AAB firmado debe generarse en tu equipo de publicación.

## Referencias oficiales

- [Requisitos de nivel de API de Google Play](https://developer.android.com/google/play/requirements/target-sdk)
- [WebView en Android](https://developer.android.com/develop/ui/views/layout/webapps/webview)
- [Data safety de Google Play](https://support.google.com/googleplay/android-developer/answer/10787469)
