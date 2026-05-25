const OPENAI_IMAGES_URL = "https://api.openai.com/v1/images/generations";
const DEFAULT_IMAGE_MODEL = "gpt-image-1.5";
const MAX_PROMPT_CHARS = 4000;

function allowedOrigins() {
  const configured = process.env.ALLOWED_ORIGINS;
  if (configured) {
    return configured
      .split(",")
      .map((origin) => origin.trim())
      .filter(Boolean);
  }

  return ["https://infiniti-ia.com", "https://www.infiniti-ia.com"];
}

function setCorsHeaders(req, res) {
  const origin = req.headers.origin;

  if (origin && allowedOrigins().includes(origin)) {
    res.setHeader("Access-Control-Allow-Origin", origin);
    res.setHeader("Vary", "Origin");
  }

  res.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
  res.setHeader("Access-Control-Allow-Headers", "Content-Type");
  res.setHeader("Cache-Control", "no-store");
}

function sendJson(res, statusCode, payload) {
  res.statusCode = statusCode;
  res.setHeader("Content-Type", "application/json; charset=utf-8");
  res.end(JSON.stringify(payload));
}

async function readJson(req) {
  if (req.body && typeof req.body === "object") {
    return req.body;
  }

  if (typeof req.body === "string") {
    return JSON.parse(req.body);
  }

  const chunks = [];
  for await (const chunk of req) {
    chunks.push(Buffer.from(chunk));
  }

  const rawBody = Buffer.concat(chunks).toString("utf8");
  return rawBody ? JSON.parse(rawBody) : {};
}

function trimText(value, maxLength) {
  return String(value || "")
    .replace(/\u0000/g, "")
    .slice(0, maxLength)
    .trim();
}

module.exports = async function handler(req, res) {
  setCorsHeaders(req, res);

  if (req.method === "OPTIONS") {
    res.statusCode = 204;
    return res.end();
  }

  if (req.method !== "POST") {
    return sendJson(res, 405, { detail: "Metodo no permitido." });
  }

  if (!process.env.OPENAI_API_KEY) {
    return sendJson(res, 500, {
      detail: "Falta configurar OPENAI_API_KEY en Vercel.",
    });
  }

  let body;
  try {
    body = await readJson(req);
  } catch (error) {
    return sendJson(res, 400, { detail: "JSON invalido." });
  }

  const prompt = trimText(body.prompt, MAX_PROMPT_CHARS);
  if (!prompt) {
    return sendJson(res, 400, { detail: "El prompt es requerido." });
  }

  const openAiPayload = {
    model: process.env.OPENAI_IMAGE_MODEL || DEFAULT_IMAGE_MODEL,
    prompt,
    n: 1,
    size: process.env.OPENAI_IMAGE_SIZE || "1024x1024",
  };

  if (process.env.OPENAI_IMAGE_QUALITY) {
    openAiPayload.quality = process.env.OPENAI_IMAGE_QUALITY;
  }

  if (process.env.OPENAI_IMAGE_BACKGROUND) {
    openAiPayload.background = process.env.OPENAI_IMAGE_BACKGROUND;
  }

  try {
    const openAiResponse = await fetch(OPENAI_IMAGES_URL, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${process.env.OPENAI_API_KEY}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(openAiPayload),
    });

    const data = await openAiResponse.json().catch(() => ({}));

    if (!openAiResponse.ok) {
      console.error("OpenAI image error", data);
      return sendJson(res, openAiResponse.status, {
        detail: data.error?.message || "Error al generar la imagen con OpenAI.",
      });
    }

    const image = Array.isArray(data.data) ? data.data[0] : null;
    const outputFormat = process.env.OPENAI_IMAGE_OUTPUT_FORMAT || "png";
    const url =
      image?.url ||
      (image?.b64_json
        ? `data:image/${outputFormat};base64,${image.b64_json}`
        : "");

    if (!url) {
      return sendJson(res, 502, {
        detail: "OpenAI no devolvio una imagen utilizable.",
      });
    }

    return sendJson(res, 200, { url });
  } catch (error) {
    console.error("Image function error", error);
    return sendJson(res, 500, {
      detail: "Error interno del backend de imagenes.",
    });
  }
};
