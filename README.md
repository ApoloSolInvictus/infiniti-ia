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
