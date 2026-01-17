#!/bin/bash

# Este script crea la serie 'XXXXX' con 50 capítulos y tareas específicas
# en el grupo 'ESDR'.
set -e
set -x

# --- Configuración ---
BASE_URL="https://sars-app.onrender.com/api"
GROUP_NAME="ESDR"
SERIE_NAME="Sangdo"

# --- 1. Crear la serie 'XXXXX' en el grupo 'ESDR' ---
echo "Paso 1: Creando la serie '${SERIE_NAME}' en el grupo '${GROUP_NAME}'..."
SERIE_PAYLOAD=$(jq -n \
  --arg nombre "${SERIE_NAME}" \
  --arg descripcion "El mercader de Chosun." \
  '{nombre: $nombre, descripcion: $descripcion}')

curl -X POST "${BASE_URL}/grupos/${GROUP_NAME}/series" \
  -H "Content-Type: application/json" \
  -d "$SERIE_PAYLOAD"

# --- 2. Creación masiva de capítulos en dos partes ---

# --- Parte A: Capítulos 1-30 ---
echo "Paso 2a: Creando capítulos 1-30 para la serie '${SERIE_NAME}'..."
CAPITULOS_1_30=$(seq 1 30)

TAREAS_1_30_RAW='[
  {"nombre": "Sincronizar", "estadoTarea": "Completado", "usuarioAsignado": "esdr"},
  {"nombre": "Muxear", "estadoTarea": "Completado", "usuarioAsignado": "esdr"},
  {"nombre": "CC", "estadoTarea": "Completado", "usuarioAsignado": "esdr"}
]'
TAREAS_1_30_JSON=$(echo "$TAREAS_1_30_RAW" | jq . -c)

BULK_1_30_PAYLOAD=$(jq -n \
  --arg nombresCapitulos "$CAPITULOS_1_30" \
  --argjson tareas "$TAREAS_1_30_JSON" \
  '{nombresCapitulos: $nombresCapitulos, tareasEnMasa: $tareas}')

curl -X POST "${BASE_URL}/grupos/${GROUP_NAME}/series/${SERIE_NAME}/capitulos/bulk" \
  -H "Content-Type: application/json" \
  -d "$BULK_1_30_PAYLOAD"


# --- Parte B: Capítulos 31-50 ---
echo "Paso 2b: Creando capítulos 31-50 para la serie '${SERIE_NAME}'..."
CAPITULOS_31_50=$(seq 31 50)

TAREAS_31_50_RAW='[
  {"nombre": "Sincronizar", "estadoTarea": "Asignado", "usuarioAsignado": "esdr"},
  {"nombre": "Muxear", "estadoTarea": "Asignado", "usuarioAsignado": "esdr"},
  {"nombre": "CC", "estadoTarea": "Asignado", "usuarioAsignado": "esdr"}
]'
TAREAS_31_50_JSON=$(echo "$TAREAS_31_50_RAW" | jq . -c)

BULK_31_50_PAYLOAD=$(jq -n \
  --arg nombresCapitulos "$CAPITULOS_31_50" \
  --argjson tareas "$TAREAS_31_50_JSON" \
  '{nombresCapitulos: $nombresCapitulos, tareasEnMasa: $tareas}')

curl -X POST "${BASE_URL}/grupos/${GROUP_NAME}/series/${SERIE_NAME}/capitulos/bulk" \
  -H "Content-Type: application/json" \
  -d "$BULK_31_50_PAYLOAD"

