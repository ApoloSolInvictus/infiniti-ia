# Infiniti IA

Sitio estatico de Infiniti IA preparado para Vercel con dos funciones serverless:

- `POST /api/chat`: proxy seguro hacia OpenAI Responses API.
- `POST /api/image`: proxy seguro hacia OpenAI Images API.

## Variables de entorno en Vercel

Configura estas variables en Project Settings > Environment Variables:

```env
OPENAI_API_KEY=sk-proj_your_key_here
OPENAI_CHAT_MODEL=gpt-5.4-mini
OPENAI_IMAGE_MODEL=gpt-image-1.5
OPENAI_IMAGE_SIZE=1024x1024
ALLOWED_ORIGINS=https://infiniti-ia.com,https://www.infiniti-ia.com
```

`OPENAI_API_KEY` es la unica obligatoria. Las demas permiten ajustar modelos,
tamano de imagen y origenes permitidos sin tocar el codigo.

## Desarrollo local

```bash
npm run dev
```

El frontend usa endpoints relativos (`/api/chat` y `/api/image`), por lo que el
mismo codigo funciona en local, previews de Vercel y produccion.

## App iOS

El proyecto inicial de **Healing Frequencies App** esta en
`ios/HealingFrequenciesApp/HealingFrequenciesApp.xcodeproj`. Es una envoltura
nativa SwiftUI con `WKWebView` que presenta `https://infiniti-ia.com/english`
con controles de navegacion, recarga, compartir, estados de conexion y notas de
seguridad sonora. La guia de compilacion y publicacion esta en
`ios/README.md`.

La politica publica de privacidad para App Store Connect esta en
`https://infiniti-ia.com/privacy` y tambien se puede abrir desde el menu nativo
de la app.

La app en español para `free.html` esta en
`ios/FrecuenciasCurativasApp/FrecuenciasCurativasApp.xcodeproj` y abre
`https://infiniti-ia.com/free`. Su politica publica de privacidad esta en
`https://infiniti-ia.com/privacidad`.

La app Android para Google Play esta en
`android/FrecuenciasCurativasApp` y tambien abre `https://infiniti-ia.com/free`.
Su politica publica de privacidad esta en
`https://infiniti-ia.com/privacidad1`. La guia de compilacion, firma y subida
esta en `android/README.md`.
