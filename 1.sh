#!/bin/bash

# Este script configura el proyecto desde cero con reglas específicas para usuarios y tareas.
# Asume que la base de datos está vacía.
set -e
set -x

# --- Configuración ---
BASE_URL="https://sars-app.onrender.com/api"

# --- 1. Crear al usuario 'esdr' como líder del grupo 'ESDR' ---
echo "Paso 1: Creando usuario 'esdr' y grupo 'ESDR'..."
USER_ESRD_PAYLOAD=$(jq -n \
  --arg username "esdr" \
  --arg password "esdr" \
  --arg groupName "ESDR" \
  --arg rol "ROLE_LIDER" \
  '{username: $username, password: $password, nombreGrupo: $groupName, rolEnGrupo: $rol}')

curl -X POST "${BASE_URL}/usuarios" \
  -H "Content-Type: application/json" \
  -d "$USER_ESRD_PAYLOAD"

# --- 2. Crear al usuario 'andros' y añadirlo al grupo 'ESDR' con rol 'QC' ---
echo "Paso 2: Creando usuario 'andros' y asignándolo al grupo 'ESDR'..."
# La contraseña de 'andros' sigue siendo 'alvaro' según la petición original.
USER_ANDROS_PAYLOAD=$(jq -n \
  --arg username "andros" \
  --arg password "andros" \
  '{username: $username, password: $password}')

curl -X POST "${BASE_URL}/usuarios" \
  -H "Content-Type: application/json" \
  -d "$USER_ANDROS_PAYLOAD"

curl -X POST "${BASE_URL}/usuarios/andros/grupos/ESDR" -H "Content-Type: application/json"

USER_ANDROS_ROLE_PAYLOAD=$(jq -n --arg rol "ROLE_QC" '{rolEnGrupo: $rol}')
curl -X PUT "${BASE_URL}/usuarios/andros/grupos/ESDR/rol" \
  -H "Content-Type: application/json" \
  -d "$USER_ANDROS_ROLE_PAYLOAD"


# --- 3. Crear la serie 'Nosesabe' en el grupo 'ESDR' ---
echo "Paso 3: Creando la serie 'Nosesabe' en el grupo 'ESDR'..."
SERIE_PAYLOAD=$(jq -n \
  --arg nombre "Nosesabe 4" \
  --arg descripcion "La serie secreta que monto." \
  '{nombre: $nombre, descripcion: $descripcion}')

curl -X POST "${BASE_URL}/grupos/ESDR/series" \
  -H "Content-Type: application/json" \
  -d "$SERIE_PAYLOAD"


# --- 4. Creación masiva de capítulos en dos partes ---

# --- Parte A: Capítulos 1-30 ---
echo "Paso 4a: Creando capítulos 1-30..."
CAPITULOS_1_30=$(seq 1 41)

TAREAS_1_30_RAW='[
  {"nombre": "IVTC", "estadoTarea": "Completado", "usuarioAsignado": "esdr"},
  {"nombre": "Editar", "estadoTarea": "Completado", "usuarioAsignado": "esdr"},
  {"nombre": "Sincronizar", "estadoTarea": "Completado", "usuarioAsignado": "esdr"},
  {"nombre": "Exportar", "estadoTarea": "Completado", "usuarioAsignado": "esdr"},
  {"nombre": "Muxear", "estadoTarea": "Completado", "usuarioAsignado": "esdr"},
  {"nombre": "Hacer subtítulos", "estadoTarea": "Completado", "usuarioAsignado": "esdr"},
  {"nombre": "Muxear Subs", "estadoTarea": "Completado", "usuarioAsignado": "esdr"},
  {"nombre": "CC", "estadoTarea": "Completado", "usuarioAsignado": "andros"}
]'
TAREAS_1_30_JSON=$(echo "$TAREAS_1_30_RAW" | jq . -c)

BULK_1_30_PAYLOAD=$(jq -n \
  --arg nombresCapitulos "$CAPITULOS_1_30" \
  --argjson tareas "$TAREAS_1_30_JSON" \
  '{nombresCapitulos: $nombresCapitulos, tareasEnMasa: $tareas}')

curl -X POST "${BASE_URL}/grupos/ESDR/series/Nosesabe%204/capitulos/bulk" \
  -H "Content-Type: application/json" \
  -d "$BULK_1_30_PAYLOAD"


# --- Parte B: Capítulos 31-51 ---
echo "Paso 4b: Creando capítulos 31-51..."
CAPITULOS_31_51=$(seq 42 51)

TAREAS_31_51_RAW='[
  {"nombre": "IVTC", "estadoTarea": "Completado", "usuarioAsignado": "esdr"},
  {"nombre": "Editar", "estadoTarea": "Completado", "usuarioAsignado": "esdr"},
  {"nombre": "Sincronizar", "estadoTarea": "Completado", "usuarioAsignado": "esdr"},
  {"nombre": "Exportar", "estadoTarea": "Completado", "usuarioAsignado": "esdr"},
  {"nombre": "Muxear", "estadoTarea": "Completado", "usuarioAsignado": "esdr"},
  {"nombre": "Hacer subtítulos", "estadoTarea": "Completado", "usuarioAsignado": "esdr"},
  {"nombre": "Muxear Subs", "estadoTarea": "Completado", "usuarioAsignado": "esdr"},
  {"nombre": "CC", "estadoTarea": "NoAsignado", "usuarioAsignado": null}
]'
TAREAS_31_51_JSON=$(echo "$TAREAS_31_51_RAW" | jq . -c)

BULK_31_51_PAYLOAD=$(jq -n \
  --arg nombresCapitulos "$CAPITULOS_31_51" \
  --argjson tareas "$TAREAS_31_51_JSON" \
  '{nombresCapitulos: $nombresCapitulos, tareasEnMasa: $tareas}')

curl -X POST "${BASE_URL}/grupos/ESDR/series/Nosesabe%204/capitulos/bulk" \
  -H "Content-Type: application/json" \
  -d "$BULK_31_51_PAYLOAD"

echo "Paso 1: Creando usuario 'pepito'..."
USER_PEPITO_PAYLOAD=$(jq -n \
  --arg username "pepito" \
  --arg password "pepito" \
  '{username: $username, password: $password}')

curl -X POST "${BASE_URL}/usuarios" \
  -H "Content-Type: application/json" \
  -d "$USER_PEPITO_PAYLOAD"

echo "Paso 2: Añadiendo a 'pepito' al grupo 'ESDR'..."
curl -X POST "${BASE_URL}/usuarios/pepito/grupos/ESDR" -H "Content-Type: application/json"

echo "Añadir a 'pepito' completado exitosamente."

echo "Script completado exitosamente."
