const OPENAI_RESPONSES_URL = "https://api.openai.com/v1/responses";
const DEFAULT_CHAT_MODEL = "gpt-5.4-mini";
const MAX_HISTORY_MESSAGES = 10;
const MAX_HISTORY_CHARS = 1600;
const MAX_MESSAGE_CHARS = 6000;

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

function normalizeRole(role) {
  if (role === "ai") return "assistant";
  if (["user", "assistant", "system", "developer"].includes(role)) return role;
  return "user";
}

function normalizeHistory(history) {
  if (!Array.isArray(history)) return [];

  return history
    .slice(-MAX_HISTORY_MESSAGES)
    .map((message) => ({
      role: normalizeRole(message.role),
      content: trimText(message.content, MAX_HISTORY_CHARS),
    }))
    .filter((message) => message.content);
}

function extractOutputText(data) {
  if (typeof data.output_text === "string" && data.output_text.trim()) {
    return data.output_text.trim();
  }

  const output = Array.isArray(data.output) ? data.output : [];
  const parts = [];

  for (const item of output) {
    const content = Array.isArray(item.content) ? item.content : [];
    for (const block of content) {
      if (typeof block.text === "string") {
        parts.push(block.text);
      }
    }
  }

  return parts.join("\n").trim();
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

  const message = trimText(body.message, MAX_MESSAGE_CHARS);
  if (!message) {
    return sendJson(res, 400, { detail: "El mensaje es requerido." });
  }

  const input = normalizeHistory(body.history);
  input.push({ role: "user", content: message });

  const chatInstructions =
    process.env.OPENAI_CHAT_INSTRUCTIONS ||
    "Responde en espanol para Infiniti IA by W Studio. Sigue el contexto comercial que envie la aplicacion, mantente util, claro y orientado a conversion sin inventar precios ni datos de contacto.";

  const openAiPayload = {
    model: process.env.OPENAI_CHAT_MODEL || DEFAULT_CHAT_MODEL,
    instructions: chatInstructions,
    input,
    max_output_tokens: Number(process.env.OPENAI_CHAT_MAX_OUTPUT_TOKENS || 700),
    store: false,
  };

  try {
    const openAiResponse = await fetch(OPENAI_RESPONSES_URL, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${process.env.OPENAI_API_KEY}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(openAiPayload),
    });

    const data = await openAiResponse.json().catch(() => ({}));

    if (!openAiResponse.ok) {
      console.error("OpenAI chat error", data);
      return sendJson(res, openAiResponse.status, {
        detail: data.error?.message || "Error al consultar OpenAI.",
      });
    }

    const response = extractOutputText(data);
    if (!response) {
      return sendJson(res, 502, {
        detail: "OpenAI no devolvio una respuesta de texto.",
      });
    }

    return sendJson(res, 200, { response });
  } catch (error) {
    console.error("Chat function error", error);
    return sendJson(res, 500, {
      detail: "Error interno del backend de chat.",
    });
  }
};
