#!/bin/bash

# Script para probar la API de creación y asignación de usuarios y grupos.
# Asume que el servidor está corriendo en http://localhost:8080
#
# REQUISITO: Este script necesita 'jq' para parsear JSON.
# Por favor, instálalo si no lo tienes (ej: sudo apt-get install jq)

# --- Verificación de prerrequisitos ---
if ! command -v jq &> /dev/null
then
    echo "ERROR: El comando 'jq' no se encuentra. Por favor, instálalo para continuar."
    exit 1
fi

BASE_URL="https://sars-app.onrender.com/api"

# Función para manejar errores de verificación
check_failure() {
    echo -e "\nERROR: $1"
    echo "Respuesta recibida: $2"
    exit 1
}

# --- Paso 1: Creando usuario 's' como LIDER del grupo 's' ---
echo "--- Paso 1: Creando usuario 's' como LIDER del grupo 's' ---"
RESPONSE_S=$(curl -s -w "\n%{http_code}" -X POST \
     -H "Content-Type: application/json" \
     -d '{"username": "s", "password": "s", "nombreGrupo": "s", "role": "ROLE_LIDER"}' \
     "$BASE_URL/usuarios")
HTTP_CODE_S=$(echo "$RESPONSE_S" | tail -n1)
BODY_S=$(echo "$RESPONSE_S" | sed '$d')

if [[ "$HTTP_CODE_S" -ne 201 ]]; then
    check_failure "La creación del usuario 's' falló con código de estado $HTTP_CODE_S." "$BODY_S"
fi
echo "Respuesta de creación de 's': $HTTP_CODE_S (Correcto)"

echo "--- Verificando creación de usuario 's' ---"
USER_S_DETAILS=$(curl -s -u "s:s" "$BASE_URL/usuarios/s")
USERNAME_S=$(echo "$USER_S_DETAILS" | jq -r '.username')
GROUP_ROLE_S=$(echo "$USER_S_DETAILS" | jq -r '.membresiasGrupo[] | select(.nombreGrupo=="s").rolEnGrupo')

if [[ "$USERNAME_S" == "s" && "$GROUP_ROLE_S" == "ROLE_LIDER" ]]; then
    echo "VERIFICACIÓN CORRECTA: Usuario 's' creado y es LIDER del grupo 's'."
else
    check_failure "Usuario 's' no fue creado correctamente o no es LIDER del grupo 's'." "$USER_S_DETAILS"
fi
echo ""

# --- Paso 2: Creando usuario 'a' como LIDER del grupo 'a' ---
echo "--- Paso 2: Creando usuario 'a' como LIDER del grupo 'a' ---"
RESPONSE_A=$(curl -s -w "\n%{http_code}" -X POST \
     -H "Content-Type: application/json" \
     -d '{"username": "a", "password": "a", "nombreGrupo": "a", "role": "ROLE_LIDER"}' \
     "$BASE_URL/usuarios")
HTTP_CODE_A=$(echo "$RESPONSE_A" | tail -n1)
BODY_A=$(echo "$RESPONSE_A" | sed '$d')

if [[ "$HTTP_CODE_A" -ne 201 ]]; then
    check_failure "La creación del usuario 'a' falló con código de estado $HTTP_CODE_A." "$BODY_A"
fi
echo "Respuesta de creación de 'a': $HTTP_CODE_A (Correcto)"


echo "--- Verificando creación de usuario 'a' ---"
USER_A_DETAILS=$(curl -s -u "a:a" "$BASE_URL/usuarios/a")
USERNAME_A=$(echo "$USER_A_DETAILS" | jq -r '.username')
GROUP_ROLE_A=$(echo "$USER_A_DETAILS" | jq -r '.membresiasGrupo[] | select(.nombreGrupo=="a").rolEnGrupo')

if [[ "$USERNAME_A" == "a" && "$GROUP_ROLE_A" == "ROLE_LIDER" ]]; then
    echo "VERIFICACIÓN CORRECTA: Usuario 'a' creado y es LIDER del grupo 'a'."
else
    check_failure "Usuario 'a' no fue creado correctamente o no es LIDER del grupo 'a'." "$USER_A_DETAILS"
fi
echo ""

# --- Paso 3: Usuario 's' (líder) añade al usuario 'a' al grupo 's' ---
echo "--- Paso 3: Usuario 's' (líder) añade al usuario 'a' al grupo 's' ---"
RESPONSE_ADD_A=$(curl -s -w "\n%{http_code}" -X POST -u "s:s" "$BASE_URL/usuarios/a/grupos/s")
HTTP_CODE_ADD_A=$(echo "$RESPONSE_ADD_A" | tail -n1)
BODY_ADD_A=$(echo "$RESPONSE_ADD_A" | sed '$d')

if [[ "$HTTP_CODE_ADD_A" -ne 200 ]]; then
    check_failure "Añadir 'a' al grupo 's' falló con código de estado $HTTP_CODE_ADD_A." "$BODY_ADD_A"
fi
echo "Respuesta de añadir 'a' a 's': $HTTP_CODE_ADD_A (Correcto)"

echo "--- Verificando que 'a' es miembro del grupo 's' ---"
GROUP_S_DETAILS=$(curl -s -u "s:s" "$BASE_URL/grupos/s")
MEMBER_A_IN_S=$(echo "$GROUP_S_DETAILS" | jq '.miembros[] | select(.username=="a")')

if [[ -n "$MEMBER_A_IN_S" ]]; then
    echo "VERIFICACIÓN CORRECTA: Usuario 'a' es miembro del grupo 's'."
else
    check_failure "Usuario 'a' no se encontró como miembro en el grupo 's'." "$GROUP_S_DETAILS"
fi
echo ""

# --- Paso 4: Usuario 'a' (líder) añade al usuario 's' al grupo 'a' ---
echo "--- Paso 4: Usuario 'a' (líder) añade al usuario 's' al grupo 'a' ---"
RESPONSE_ADD_S=$(curl -s -w "\n%{http_code}" -X POST -u "a:a" "$BASE_URL/usuarios/s/grupos/a")
HTTP_CODE_ADD_S=$(echo "$RESPONSE_ADD_S" | tail -n1)
BODY_ADD_S=$(echo "$RESPONSE_ADD_S" | sed '$d')

if [[ "$HTTP_CODE_ADD_S" -ne 200 ]]; then
    check_failure "Añadir 's' al grupo 'a' falló con código de estado $HTTP_CODE_ADD_S." "$BODY_ADD_S"
fi
echo "Respuesta de añadir 's' a 'a': $HTTP_CODE_ADD_S (Correcto)"


echo "--- Verificando que 's' es miembro del grupo 'a' ---"
GROUP_A_DETAILS=$(curl -s -u "a:a" "$BASE_URL/grupos/a")
MEMBER_S_IN_A=$(echo "$GROUP_A_DETAILS" | jq '.miembros[] | select(.username=="s")')

if [[ -n "$MEMBER_S_IN_A" ]]; then
    echo "VERIFICACIÓN CORRECTA: Usuario 's' es miembro del grupo 'a'."
else
    check_failure "Usuario 's' no se encontró como miembro en el grupo 'a'." "$GROUP_A_DETAILS"
fi
echo ""

echo "--- Todas las verificaciones han sido exitosas. ---"



# --- Paso 5: Crear usuario 'c' (sin grupo) ---
echo -e "\n--- Paso 5: Creando usuario 'c' (sin grupo) ---"
RESPONSE_C=$(curl -s -w "\n%{http_code}" -X POST \
     -H "Content-Type: application/json" \
     -d '{"username": "c", "password": "c"}' \
     "$BASE_URL/usuarios")
HTTP_CODE_C=$(echo "$RESPONSE_C" | tail -n1)
BODY_C=$(echo "$RESPONSE_C" | sed '$d')

if [[ "$HTTP_CODE_C" -ne 201 ]]; then
    check_failure "La creación del usuario 'c' falló con código de estado $HTTP_CODE_C." "$BODY_C"
fi
echo "Respuesta de creación de 'c': $HTTP_CODE_C (Correcto)"

echo "--- Verificando creación de usuario 'c' ---"
USER_C_DETAILS=$(curl -s -u "c:c" "$BASE_URL/usuarios/c")
USERNAME_C_VERIF=$(echo "$USER_C_DETAILS" | jq -r '.username')
USER_C_MEMBERSHIPS=$(echo "$USER_C_DETAILS" | jq -r '.membresiasGrupo | length') # Should be 0 initially

if [[ "$USERNAME_C_VERIF" == "c" && "$USER_C_MEMBERSHIPS" == "0" ]]; then
    echo "VERIFICACIÓN CORRECTA: Usuario 'c' creado y sin membresías de grupo iniciales."
else
    check_failure "Usuario 'c' no fue creado correctamente o tiene membresías inesperadas." "$USER_C_DETAILS"
fi
echo ""

# --- Paso 6: Añadir 'c' a los grupos 's' y 'a' ---
echo "--- Paso 6: Añadiendo 'c' al grupo 's' ---"
RESPONSE_ADD_C_TO_S=$(curl -s -w "\n%{http_code}" -X POST -u "s:s" "$BASE_URL/usuarios/c/grupos/s")
HTTP_CODE_ADD_C_TO_S=$(echo "$RESPONSE_ADD_C_TO_S" | tail -n1)
BODY_ADD_C_TO_S=$(echo "$RESPONSE_ADD_C_TO_S" | sed '$d')

if [[ "$HTTP_CODE_ADD_C_TO_S" -ne 200 ]]; then
    check_failure "Añadir 'c' al grupo 's' falló con código de estado $HTTP_CODE_ADD_C_TO_S." "$BODY_ADD_C_TO_S"
fi
echo "Respuesta de añadir 'c' al grupo 's': $HTTP_CODE_ADD_C_TO_S (Correcto)"

echo "--- Verificando que 'c' es miembro del grupo 's' ---"
GROUP_S_DETAILS=$(curl -s -u "s:s" "$BASE_URL/grupos/s")
MEMBER_C_IN_S=$(echo "$GROUP_S_DETAILS" | jq '.miembros[] | select(.username=="c")')

if [[ -n "$MEMBER_C_IN_S" ]]; then
    echo "VERIFICACIÓN CORRECTA: Usuario 'c' es miembro del grupo 's'."
else
    check_failure "Usuario 'c' no se encontró en el grupo 's'." "$GROUP_S_DETAILS"
fi
echo ""

echo "--- Añadiendo 'c' al grupo 'a' ---"
RESPONSE_ADD_C_TO_A=$(curl -s -w "\n%{http_code}" -X POST -u "a:a" "$BASE_URL/usuarios/c/grupos/a")
HTTP_CODE_ADD_C_TO_A=$(echo "$RESPONSE_ADD_C_TO_A" | tail -n1)
BODY_ADD_C_TO_A=$(echo "$RESPONSE_ADD_C_TO_A" | sed '$d')

if [[ "$HTTP_CODE_ADD_C_TO_A" -ne 200 ]]; then
    check_failure "Añadir 'c' al grupo 'a' falló con código de estado $HTTP_CODE_ADD_C_TO_A." "$BODY_ADD_C_TO_A"
fi
echo "Respuesta de añadir 'c' al grupo 'a': $HTTP_CODE_ADD_C_TO_A (Correcto)"

echo "--- Verificando que 'c' es miembro del grupo 'a' ---"
GROUP_A_DETAILS=$(curl -s -u "a:a" "$BASE_URL/grupos/a")
MEMBER_C_IN_A=$(echo "$GROUP_A_DETAILS" | jq '.miembros[] | select(.username=="c")')

if [[ -n "$MEMBER_C_IN_A" ]]; then
    echo "VERIFICACIÓN CORRECTA: Usuario 'c' es miembro del grupo 'a'."
else
    check_failure "Usuario 'c' no se encontró en el grupo 'a'." "$GROUP_A_DETAILS"
fi
echo ""

# --- Paso 7: Asignar rol ROLE_QC a 'c' en grupos 's' y 'a' ---
echo "--- Paso 7: Asignando rol ROLE_QC a 'c' en el grupo 's' ---"
RESPONSE_ROLE_C_S=$(curl -s -w "\n%{http_code}" -X PUT \
     -H "Content-Type: application/json" \
     -u "s:s" \
     -d '{"rolEnGrupo": "ROLE_QC"}' \
     "$BASE_URL/grupos/s/miembros/c/rol")
HTTP_CODE_ROLE_C_S=$(echo "$RESPONSE_ROLE_C_S" | tail -n1)
BODY_ROLE_C_S=$(echo "$RESPONSE_ROLE_C_S" | sed '$d')

if [[ "$HTTP_CODE_ROLE_C_S" -ne 200 ]]; then
    check_failure "Cambiar rol de 'c' a ROLE_QC en grupo 's' falló con código $HTTP_CODE_ROLE_C_S." "$BODY_ROLE_C_S"
fi
echo "Respuesta de cambio de rol de 'c' en grupo 's': $HTTP_CODE_ROLE_C_S (Correcto)"

echo "--- Verificando rol de 'c' en grupo 's' (debe ser ROLE_QC) ---"
GROUP_S_DETAILS=$(curl -s -u "s:s" "$BASE_URL/grupos/s")
ROLE_C_IN_S=$(echo "$GROUP_S_DETAILS" | jq -r '.miembros[] | select(.username=="c").rolEnGrupo')

if [[ "$ROLE_C_IN_S" == "ROLE_QC" ]]; then
    echo "VERIFICACIÓN CORRECTA: Rol de 'c' en grupo 's' es ROLE_QC."
else
    check_failure "Rol de 'c' en grupo 's' no es ROLE_QC." "$GROUP_S_DETAILS"
fi
echo ""

echo "--- Asignando rol ROLE_QC a 'c' en el grupo 'a' ---"
RESPONSE_ROLE_C_A=$(curl -s -w "\n%{http_code}" -X PUT \
     -H "Content-Type: application/json" \
     -u "a:a" \
     -d '{"rolEnGrupo": "ROLE_QC"}' \
     "$BASE_URL/grupos/a/miembros/c/rol")
HTTP_CODE_ROLE_C_A=$(echo "$RESPONSE_ROLE_C_A" | tail -n1)
BODY_ROLE_C_A=$(echo "$RESPONSE_ROLE_C_A" | sed '$d')

if [[ "$HTTP_CODE_ROLE_C_A" -ne 200 ]]; then
    check_failure "Cambiar rol de 'c' a ROLE_QC en grupo 'a' falló con código $HTTP_CODE_ROLE_C_A." "$BODY_ROLE_C_A"
fi
echo "Respuesta de cambio de rol de 'c' en grupo 'a': $HTTP_CODE_ROLE_C_A (Correcto)"

echo "--- Verificando rol de 'c' en grupo 'a' (debe ser ROLE_QC) ---"
GROUP_A_DETAILS=$(curl -s -u "a:a" "$BASE_URL/grupos/a")
ROLE_C_IN_A=$(echo "$GROUP_A_DETAILS" | jq -r '.miembros[] | select(.username=="c").rolEnGrupo')

if [[ "$ROLE_C_IN_A" == "ROLE_QC" ]]; then
    echo "VERIFICACIÓN CORRECTA: Rol de 'c' en grupo 'a' es ROLE_QC."
else
    check_failure "Rol de 'c' en grupo 'a' no es ROLE_QC." "$GROUP_A_DETAILS"
fi
echo ""

# --- Paso 8: Crear una serie de prueba en el grupo 's' ---
echo "--- Paso 8: Creando serie 'Serie de Prueba' en el grupo 's' ---"
RESPONSE_CREATE_SERIE=$(curl -s -w "\n%{http_code}" -X POST \
     -H "Content-Type: application/json" \
     -u "s:s" \
     -d '{"nombre": "Serie de Prueba", "descripcion": "Serie de prueba creada por el script para el grupo s."}' \
     "$BASE_URL/grupos/s/series")
HTTP_CODE_CREATE_SERIE=$(echo "$RESPONSE_CREATE_SERIE" | tail -n1)
BODY_CREATE_SERIE=$(echo "$RESPONSE_CREATE_SERIE" | sed '$d')

if [[ "$HTTP_CODE_CREATE_SERIE" -ne 201 ]]; then
    check_failure "Creación de serie 'Serie de Prueba' falló con código $HTTP_CODE_CREATE_SERIE." "$BODY_CREATE_SERIE"
fi
echo "Respuesta de creación de serie 'Serie de Prueba': $HTTP_CODE_CREATE_SERIE (Correcto)"

echo "--- Verificando creación de serie 'Serie de Prueba' ---"
SERIE_DETAILS=$(curl -s -u "s:s" "$BASE_URL/grupos/s/series/Serie%20de%20Prueba")
SERIE_NAME_VERIF=$(echo "$SERIE_DETAILS" | jq -r '.nombre')

if [[ "$SERIE_NAME_VERIF" == "Serie de Prueba" ]]; then
    echo "VERIFICACIÓN CORRECTA: Serie 'Serie de Prueba' creada."
else
    check_failure "Serie 'Serie de Prueba' no fue creada correctamente." "$SERIE_DETAILS"
fi
echo ""

# --- Paso 9: Crear capítulos en la serie de prueba ---
echo "--- Paso 9: Creando capítulo 'Capitulo 1' en la serie 'Serie de Prueba' ---"
RESPONSE_CREATE_CH1=$(curl -s -w "\n%{http_code}" -X POST \
     -H "Content-Type: application/json" \
     -u "s:s" \
     -d '{"nombreCapitulo": "Capitulo 1"}' \
     "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos")
HTTP_CODE_CREATE_CH1=$(echo "$RESPONSE_CREATE_CH1" | tail -n1)
BODY_CREATE_CH1=$(echo "$RESPONSE_CREATE_CH1" | sed '$d')

if [[ "$HTTP_CODE_CREATE_CH1" -ne 201 ]]; then
    check_failure "Creación de capítulo 'Capitulo 1' falló con código $HTTP_CODE_CREATE_CH1." "$BODY_CREATE_CH1"
fi
echo "Respuesta de creación de capítulo 'Capitulo 1': $HTTP_CODE_CREATE_CH1 (Correcto)"

echo "--- Verificando creación de capítulo 'Capitulo 1' ---"
CH1_DETAILS=$(curl -s -u "s:s" "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%201")
CH1_NAME_VERIF=$(echo "$CH1_DETAILS" | jq -r '.nombreCapitulo')

if [[ "$CH1_NAME_VERIF" == "Capitulo 1" ]]; then
    echo "VERIFICACIÓN CORRECTA: Capítulo 'Capitulo 1' creado."
else
    check_failure "Capítulo 'Capitulo 1' no fue creado correctamente." "$CH1_DETAILS"
fi
echo ""

echo "--- Creando capítulo 'Capitulo 2' y sus tareas en masa en la serie 'Serie de Prueba' ---"

JSON_PAYLOAD_CH2_BULK='{"nombresCapitulos": "Capitulo 2","tareasEnMasa": [{"nombre": "Tarea 2.1", "estadoTarea": "NoAsignado", "usuarioAsignado": null},{"nombre": "Tarea 2.2", "estadoTarea": "NoAsignado", "usuarioAsignado": null},{"nombre": "Tarea 2.3", "estadoTarea": "NoAsignado", "usuarioAsignado": null},{"nombre": "Tarea 2.4", "estadoTarea": "NoAsignado", "usuarioAsignado": null},{"nombre": "Tarea 2.5", "estadoTarea": "NoAsignado", "usuarioAsignado": null},{"nombre": "CC", "estadoTarea": "NoAsignado", "usuarioAsignado": null}]}'

RESPONSE_CREATE_CH2_BULK=$(curl -s -w "\n%{http_code}" -X POST -H "Content-Type: application/json" -u "s:s" -d "$JSON_PAYLOAD_CH2_BULK" "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/bulk")

HTTP_CODE_CREATE_CH2_BULK=$(echo "$RESPONSE_CREATE_CH2_BULK" | tail -n1)

BODY_CREATE_CH2_BULK=$(echo "$RESPONSE_CREATE_CH2_BULK" | sed '$d')



if [[ "$HTTP_CODE_CREATE_CH2_BULK" -ne 201 ]]; then

    check_failure "Creación en masa de Capítulo 'Capitulo 2' y tareas falló con código $HTTP_CODE_CREATE_CH2_BULK." "$BODY_CREATE_CH2_BULK"

fi

echo "Respuesta de creación en masa de Capítulo 'Capitulo 2': $HTTP_CODE_CREATE_CH2_BULK (Correcto)"



echo "--- Verificando creación de capítulo 'Capitulo 2' y sus tareas ---"

CH2_DETAILS=$(curl -s -u "s:s" "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%202")

CH2_NAME_VERIF=$(echo "$CH2_DETAILS" | jq -r '.nombreCapitulo')

TASKS_COUNT_CH2=$(echo "$CH2_DETAILS" | jq '.tareas | length')



if [[ "$CH2_NAME_VERIF" == "Capitulo 2" && "$TASKS_COUNT_CH2" -eq 6 ]]; then

    echo "VERIFICACIÓN CORRECTA: Capítulo 'Capitulo 2' y sus 6 tareas creadas en masa."

    # Verificando Tarea CC en el Capítulo 2 (creada automáticamente en masa)

    CC_TASK_NAME="CC"

    T_DETAILS_CC_CH2=$(curl -s -u "s:s" "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%202/tareas/$CC_TASK_NAME")

    T_NAME_VERIF_CC_CH2=$(echo "$T_DETAILS_CC_CH2" | jq -r '.nombre')

    if [[ "$T_NAME_VERIF_CC_CH2" == "$CC_TASK_NAME" ]]; then

                echo "        VERIFICACIÓN CORRECTA: Tarea '$CC_TASK_NAME' creada automáticamente en masa para Capítulo 'Capitulo 2'."

    else

                check_failure "        Tarea '$CC_TASK_NAME' no fue creada automáticamente en masa para Capítulo 'Capitulo 2'." "$T_DETAILS_CC_CH2"

    fi

else

    check_failure "Capítulo 'Capitulo 2' o sus tareas no fueron creadas correctamente en masa." "$CH2_DETAILS"

fi

echo ""

# --- Paso 10: Crear tareas en los Capítulos ---
echo "--- Paso 10: Creando tareas en el capítulo 'Capitulo 1' ---"
for i in $(seq 1 5); do
    TASK_NAME="Tarea 1.$i"
    echo "--- Creando tarea '$TASK_NAME' en el capítulo 'Capitulo 1' ---"
    RESPONSE_CREATE_T=$(curl -s -w "\n%{http_code}" -X POST \
         -H "Content-Type: application/json" \
         -u "s:s" \
         -d '{"nombre": "'"$TASK_NAME"'"}' \
         "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%201/tareas")
    HTTP_CODE_CREATE_T=$(echo "$RESPONSE_CREATE_T" | tail -n1)
    BODY_CREATE_T=$(echo "$RESPONSE_CREATE_T" | sed '$d')

    if [[ "$HTTP_CODE_CREATE_T" -ne 201 ]]; then
        check_failure "Creación de tarea '$TASK_NAME' falló con código $HTTP_CODE_CREATE_T." "$BODY_CREATE_T"
    fi
    echo "Respuesta de creación de tarea '$TASK_NAME': $HTTP_CODE_CREATE_T (Correcto)"

    echo "--- Verificando creación de tarea '$TASK_NAME' ---"
    T_DETAILS=$(curl -s -u "s:s" "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%201/tareas/$(echo "$TASK_NAME" | sed 's/ /%20/g')")
    T_NAME_VERIF=$(echo "$T_DETAILS" | jq -r '.nombre')

    if [[ "$T_NAME_VERIF" == "$TASK_NAME" ]]; then
        echo "VERIFICACIÓN CORRECTA: Tarea '$TASK_NAME' creada."
    else
        check_failure "Tarea '$TASK_NAME' no fue creada correctamente." "$T_DETAILS"
    fi
    echo ""
done

echo "--- Verificando Tarea CC en el Capítulo 1 (creada automáticamente) ---"
CC_TASK_NAME="CC"
T_DETAILS=$(curl -s -u "s:s" "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%201/tareas/$(echo "$CC_TASK_NAME" | sed 's/ /%20/g')")
T_NAME_VERIF=$(echo "$T_DETAILS" | jq -r '.nombre')
if [[ "$T_NAME_VERIF" == "$CC_TASK_NAME" ]]; then
    echo "VERIFICACIÓN CORRECTA: Tarea '$CC_TASK_NAME' creada automáticamente."
else
    check_failure "Tarea '$CC_TASK_NAME' no fue creada automáticamente." "$T_DETAILS"
fi
echo ""



# --- Paso 11: Los diversos usuarios modifican los estados de las tareas ---
echo -e "\n--- Paso 11: Modificando estados de tareas con diferentes usuarios ---"

# Escenario 1: Usuario 'c' (ROLE_QC) actualiza "Tarea 1.1" (Capitulo 1, Grupo 's') a Asignado
echo "--- 'c' cambia 'Tarea 1.1' a Asignado ---"
RESPONSE_C_1_1=$(curl -s -w "\n%{http_code}" -X PUT \
     -H "Content-Type: application/json" \
     -u "c:c" \
     -d '{"username": "c", "nuevoEstado": "Asignado"}' \
     "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%201/tareas/Tarea%201.1/estado")
HTTP_CODE_C_1_1=$(echo "$RESPONSE_C_1_1" | tail -n1)
BODY_C_1_1=$(echo "$RESPONSE_C_1_1" | sed '$d')

if [[ "$HTTP_CODE_C_1_1" -ne 200 ]]; then
    check_failure "Fallo al cambiar estado de 'Tarea 1.1' por 'c'." "$BODY_C_1_1"
fi
echo "Respuesta cambio estado 'Tarea 1.1' por 'c': $HTTP_CODE_C_1_1 (Correcto)"

TASK_1_1_DETAILS=$(curl -s -u "c:c" "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%201/tareas/Tarea%201.1")
TASK_1_1_STATE=$(echo "$TASK_1_1_DETAILS" | jq -r '.estadoTarea')
if [[ "$TASK_1_1_STATE" == "Asignado" ]]; then
    echo "VERIFICACIÓN CORRECTA: 'Tarea 1.1' en estado Asignado."
else
    check_failure "Estado de 'Tarea 1.1' no es Asignado." "$TASK_1_1_DETAILS"
fi
echo ""

# Escenario 2: Usuario 's' (LIDER) actualiza "Tarea 1.2" (Capitulo 1, Grupo 's') a Completado
echo "--- 's' cambia 'Tarea 1.2' a Completado ---"
RESPONSE_S_1_2=$(curl -s -w "\n%{http_code}" -X PUT \
     -H "Content-Type: application/json" \
     -u "s:s" \
     -d '{"username": "s", "nuevoEstado": "Completado"}' \
     "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%201/tareas/Tarea%201.2/estado")
HTTP_CODE_S_1_2=$(echo "$RESPONSE_S_1_2" | tail -n1)
BODY_S_1_2=$(echo "$RESPONSE_S_1_2" | sed '$d')

if [[ "$HTTP_CODE_S_1_2" -ne 200 ]]; then
    check_failure "Fallo al cambiar estado de 'Tarea 1.2' por 's'." "$BODY_S_1_2"
fi
echo "Respuesta cambio estado 'Tarea 1.2' por 's': $HTTP_CODE_S_1_2 (Correcto)"

TASK_1_2_DETAILS=$(curl -s -u "s:s" "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%201/tareas/Tarea%201.2")
TASK_1_2_STATE=$(echo "$TASK_1_2_DETAILS" | jq -r '.estadoTarea')
if [[ "$TASK_1_2_STATE" == "Completado" ]]; then
    echo "VERIFICACIÓN CORRECTA: 'Tarea 1.2' en estado Completado."
else
    check_failure "Estado de 'Tarea 1.2' no es Completado." "$TASK_1_2_DETAILS"
fi
echo ""

# Escenario 3a: 's' (LIDER) cambia 'Tarea 2.1' a Completado (Pre-requisito para QC Repetir) ---
echo "--- 's' (LIDER) cambia 'Tarea 2.1' a Completado (Pre-requisito para QC Repetir) ---"
RESPONSE_S_2_1_COMPLETADO=$(curl -s -w "\n%{http_code}" -X PUT \
     -H "Content-Type: application/json" \
     -u "s:s" \
     -d '{"username": "s", "nuevoEstado": "Completado"}' \
     "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%202/tareas/Tarea%202.1/estado")
HTTP_CODE_S_2_1_COMPLETADO=$(echo "$RESPONSE_S_2_1_COMPLETADO" | tail -n1)
BODY_S_2_1_COMPLETADO=$(echo "$RESPONSE_S_2_1_COMPLETADO" | sed '$d')

if [[ "$HTTP_CODE_S_2_1_COMPLETADO" -ne 200 ]]; then
    check_failure "Fallo al cambiar 'Tarea 2.1' a Completado por 's'." "$BODY_S_2_1_COMPLETADO"
fi
echo "Respuesta cambio estado 'Tarea 2.1' por 's': $HTTP_CODE_S_2_1_COMPLETADO (Correcto)"

TASK_2_1_DETAILS_S_COMPLETADO=$(curl -s -u "s:s" "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%202/tareas/Tarea%202.1")
TASK_2_1_STATE_S_COMPLETADO=$(echo "$TASK_2_1_DETAILS_S_COMPLETADO" | jq -r '.estadoTarea')
if [[ "$TASK_2_1_STATE_S_COMPLETADO" == "Completado" ]]; then
    echo "VERIFICACIÓN CORRECTA: 'Tarea 2.1' en estado Completado por 's'."
else
    check_failure "Estado de 'Tarea 2.1' no es Completado por 's'." "$TASK_2_1_DETAILS_S_COMPLETADO"
fi
echo ""

# Escenario 3b: 'c' (ROLE_QC) cambia 'Tarea 2.1' (ahora Completado) a Repetir
echo "--- 'c' (ROLE_QC) cambia 'Tarea 2.1' (Completado) a Repetir ---"
RESPONSE_C_2_1_REPETIR=$(curl -s -w "\n%{http_code}" -X PUT \
     -H "Content-Type: application/json" \
     -u "c:c" \
     -d '{"username": "c", "nuevoEstado": "Repetir"}' \
     "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%202/tareas/Tarea%202.1/estado")
HTTP_CODE_C_2_1_REPETIR=$(echo "$RESPONSE_C_2_1_REPETIR" | tail -n1)
BODY_C_2_1_REPETIR=$(echo "$RESPONSE_C_2_1_REPETIR" | sed '$d')

if [[ "$HTTP_CODE_C_2_1_REPETIR" -ne 200 ]]; then
    check_failure "Fallo al cambiar 'Tarea 2.1' a Repetir por 'c'." "$BODY_C_2_1_REPETIR"
fi
echo "Respuesta cambio estado 'Tarea 2.1' por 'c': $HTTP_CODE_C_2_1_REPETIR (Correcto)"

TASK_2_1_DETAILS_C_REPETIR=$(curl -s -u "c:c" "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%202/tareas/Tarea%202.1")
TASK_2_1_STATE_C_REPETIR=$(echo "$TASK_2_1_DETAILS_C_REPETIR" | jq -r '.estadoTarea')
if [[ "$TASK_2_1_STATE_C_REPETIR" == "Repetir" ]]; then
    echo "VERIFICACIÓN CORRECTA: 'Tarea 2.1' en estado Repetir por 'c'."
else
    check_failure "Estado de 'Tarea 2.1' no es Repetir por 'c'." "$TASK_2_1_DETAILS_C_REPETIR"
fi
echo ""

# Escenario 4: Usuario 'c' (ROLE_QC en grupo 's') actualiza "CC" (Capitulo 2, Grupo 's') a Completado
echo "--- 'c' cambia 'CC' (Capitulo 2) a Completado ---"
RESPONSE_C_CC=$(curl -s -w "\n%{http_code}" -X PUT \
     -H "Content-Type: application/json" \
     -u "c:c" \
     -d '{"username": "c", "nuevoEstado": "Completado"}' \
     "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%202/tareas/CC/estado")
HTTP_CODE_C_CC=$(echo "$RESPONSE_C_CC" | tail -n1)
BODY_C_CC=$(echo "$RESPONSE_C_CC" | sed '$d')

if [[ "$HTTP_CODE_C_CC" -ne 200 ]]; then
    check_failure "Fallo al cambiar estado de 'CC' (Capitulo 2) por 'c'." "$BODY_C_CC"
fi
echo "Respuesta cambio estado 'CC' (Capitulo 2) por 'c': $HTTP_CODE_C_CC (Correcto)"

TASK_CC_DETAILS=$(curl -s -u "c:c" "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%202/tareas/CC")
TASK_CC_STATE=$(echo "$TASK_CC_DETAILS" | jq -r '.estadoTarea')
if [[ "$TASK_CC_STATE" == "Completado" ]]; then
    echo "VERIFICACIÓN CORRECTA: 'CC' (Capitulo 2) en estado Completado."
else
    check_failure "Estado de 'CC' (Capitulo 2) no es Completado." "$TASK_CC_DETAILS"
fi
echo ""

# --- Nuevas pruebas de cambio de estado para LIDER (s) ---
echo -e "\n--- Nuevas pruebas de cambio de estado para LIDER ('s') ---"

# Tarea 1.3 (inicialmente NoAsignado)
# 's' cambia 'Tarea 1.3' de NoAsignado a Asignado (self-assign)
echo "--- LIDER 's' cambia 'Tarea 1.3' (NoAsignado) a Asignado ---"
RESPONSE_S_1_3_ASIGNADO=$(curl -s -w "\n%{http_code}" -X PUT \
     -H "Content-Type: application/json" \
     -u "s:s" \
     -d '{"username": "s", "nuevoEstado": "Asignado"}' \
     "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%201/tareas/Tarea%201.3/estado")
HTTP_CODE_S_1_3_ASIGNADO=$(echo "$RESPONSE_S_1_3_ASIGNADO" | tail -n1)
BODY_S_1_3_ASIGNADO=$(echo "$RESPONSE_S_1_3_ASIGNADO" | sed '$d')

if [[ "$HTTP_CODE_S_1_3_ASIGNADO" -ne 200 ]]; then
    check_failure "LIDER 's' falló al cambiar 'Tarea 1.3' a Asignado." "$BODY_S_1_3_ASIGNADO"
fi
echo "Respuesta LIDER 's' cambio estado 'Tarea 1.3': $HTTP_CODE_S_1_3_ASIGNADO (Correcto)"

TASK_1_3_DETAILS=$(curl -s -u "s:s" "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%201/tareas/Tarea%201.3")
TASK_1_3_STATE=$(echo "$TASK_1_3_DETAILS" | jq -r '.estadoTarea')
TASK_1_3_ASSIGNED=$(echo "$TASK_1_3_DETAILS" | jq -r '.usuarioAsignado')
if [[ "$TASK_1_3_STATE" == "Asignado" && "$TASK_1_3_ASSIGNED" == "s" ]]; then
    echo "VERIFICACIÓN CORRECTA: 'Tarea 1.3' en estado Asignado y asignada a 's'."
else
    check_failure "Estado de 'Tarea 1.3' no es Asignado o no asignada a 's'." "$TASK_1_3_DETAILS"
fi
echo ""

# 's' cambia 'Tarea 1.3' de Asignado (por 's') a Completado
echo "--- LIDER 's' cambia 'Tarea 1.3' (Asignado) a Completado ---"
RESPONSE_S_1_3_COMPLETADO=$(curl -s -w "\n%{http_code}" -X PUT \
     -H "Content-Type: application/json" \
     -u "s:s" \
     -d '{"username": "s", "nuevoEstado": "Completado"}' \
     "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%201/tareas/Tarea%201.3/estado")
HTTP_CODE_S_1_3_COMPLETADO=$(echo "$RESPONSE_S_1_3_COMPLETADO" | tail -n1)
BODY_S_1_3_COMPLETADO=$(echo "$RESPONSE_S_1_3_COMPLETADO" | sed '$d')

if [[ "$HTTP_CODE_S_1_3_COMPLETADO" -ne 200 ]]; then
    check_failure "LIDER 's' falló al cambiar 'Tarea 1.3' a Completado." "$BODY_S_1_3_COMPLETADO"
fi
echo "Respuesta LIDER 's' cambio estado 'Tarea 1.3': $HTTP_CODE_S_1_3_COMPLETADO (Correcto)"

TASK_1_3_DETAILS=$(curl -s -u "s:s" "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%201/tareas/Tarea%201.3")
TASK_1_3_STATE=$(echo "$TASK_1_3_DETAILS" | jq -r '.estadoTarea')
TASK_1_3_ASSIGNED=$(echo "$TASK_1_3_DETAILS" | jq -r '.usuarioAsignado')
if [[ "$TASK_1_3_STATE" == "Completado" && "$TASK_1_3_ASSIGNED" == "s" ]]; then
    echo "VERIFICACIÓN CORRECTA: 'Tarea 1.3' en estado Completado y asignada a 's'."
else
    check_failure "Estado de 'Tarea 1.3' no es Completado o no asignada a 's'." "$TASK_1_3_DETAILS"
fi
echo ""

# Tarea 1.4 (inicialmente NoAsignado)
# 's' cambia 'Tarea 1.4' de NoAsignado a Completado (self-assign and complete)
echo "--- LIDER 's' cambia 'Tarea 1.4' (NoAsignado) a Completado ---"
RESPONSE_S_1_4_COMPLETADO=$(curl -s -w "\n%{http_code}" -X PUT \
     -H "Content-Type: application/json" \
     -u "s:s" \
     -d '{"username": "s", "nuevoEstado": "Completado"}' \
     "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%201/tareas/Tarea%201.4/estado")
HTTP_CODE_S_1_4_COMPLETADO=$(echo "$RESPONSE_S_1_4_COMPLETADO" | tail -n1)
BODY_S_1_4_COMPLETADO=$(echo "$RESPONSE_S_1_4_COMPLETADO" | sed '$d')

if [[ "$HTTP_CODE_S_1_4_COMPLETADO" -ne 200 ]]; then
    check_failure "LIDER 's' falló al cambiar 'Tarea 1.4' a Completado." "$BODY_S_1_4_COMPLETADO"
fi
echo "Respuesta LIDER 's' cambio estado 'Tarea 1.4': $HTTP_CODE_S_1_4_COMPLETADO (Correcto)"

TASK_1_4_DETAILS=$(curl -s -u "s:s" "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%201/tareas/Tarea%201.4")
TASK_1_4_STATE=$(echo "$TASK_1_4_DETAILS" | jq -r '.estadoTarea')
TASK_1_4_ASSIGNED=$(echo "$TASK_1_4_DETAILS" | jq -r '.usuarioAsignado')
if [[ "$TASK_1_4_STATE" == "Completado" && "$TASK_1_4_ASSIGNED" == "s" ]]; then
    echo "VERIFICACIÓN CORRECTA: 'Tarea 1.4' en estado Completado y asignada a 's'."
else
    check_failure "Estado de 'Tarea 1.4' no es Completado o no asignada a 's'." "$TASK_1_4_DETAILS"
fi
echo ""

# Tarea 1.5 (inicialmente NoAsignado)
# 's' cambia 'Tarea 1.5' de NoAsignado a Repetir (unassign)
echo "--- LIDER 's' cambia 'Tarea 1.5' (NoAsignado) a Repetir ---"
RESPONSE_S_1_5_REPETIR=$(curl -s -w "\n%{http_code}" -X PUT \
     -H "Content-Type: application/json" \
     -u "s:s" \
     -d '{"username": "s", "nuevoEstado": "Repetir"}' \
     "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%201/tareas/Tarea%201.5/estado")
HTTP_CODE_S_1_5_REPETIR=$(echo "$RESPONSE_S_1_5_REPETIR" | tail -n1)
BODY_S_1_5_REPETIR=$(echo "$RESPONSE_S_1_5_REPETIR" | sed '$d')

if [[ "$HTTP_CODE_S_1_5_REPETIR" -ne 200 ]]; then
    check_failure "LIDER 's' falló al cambiar 'Tarea 1.5' a Repetir." "$BODY_S_1_5_REPETIR"
fi
echo "Respuesta LIDER 's' cambio estado 'Tarea 1.5': $HTTP_CODE_S_1_5_REPETIR (Correcto)"

TASK_1_5_DETAILS=$(curl -s -u "s:s" "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%201/tareas/Tarea%201.5")
TASK_1_5_STATE=$(echo "$TASK_1_5_DETAILS" | jq -r '.estadoTarea')
TASK_1_5_ASSIGNED=$(echo "$TASK_1_5_DETAILS" | jq -r '.usuarioAsignado')
if [[ "$TASK_1_5_STATE" == "Repetir" && "$TASK_1_5_ASSIGNED" == "NADIE" ]]; then
    echo "VERIFICACIÓN CORRECTA: 'Tarea 1.5' en estado Repetir y asignada a 'NADIE'."
else
    check_failure "Estado de 'Tarea 1.5' no es Repetir o no asignada a 'NADIE'." "$TASK_1_5_DETAILS"
fi
echo ""

# 's' cambia "CC" (Capitulo 1) de NoAsignado a Completado (this activates CC block for non-LIDERs)
echo "--- LIDER 's' cambia 'CC' (Capitulo 1) a Completado (activará bloqueo de CC para no-LIDERES) ---"
RESPONSE_S_CC_CH1_COMPLETADO=$(curl -s -w "\n%{http_code}" -X PUT \
     -H "Content-Type: application/json" \
     -u "s:s" \
     -d '{"username": "s", "nuevoEstado": "Completado"}' \
     "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%201/tareas/CC/estado")
HTTP_CODE_S_CC_CH1_COMPLETADO=$(echo "$RESPONSE_S_CC_CH1_COMPLETADO" | tail -n1)
BODY_S_CC_CH1_COMPLETADO=$(echo "$RESPONSE_S_CC_CH1_COMPLETADO" | sed '$d')

if [[ "$HTTP_CODE_S_CC_CH1_COMPLETADO" -ne 200 ]]; then
    check_failure "LIDER 's' falló al cambiar 'CC' (Capitulo 1) a Completado." "$BODY_S_CC_CH1_COMPLETADO"
fi
echo "Respuesta LIDER 's' cambio estado 'CC' (Capitulo 1): $HTTP_CODE_S_CC_CH1_COMPLETADO (Correcto)"

TASK_CC_CH1_DETAILS=$(curl -s -u "s:s" "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%201/tareas/CC")
TASK_CC_CH1_STATE=$(echo "$TASK_CC_CH1_DETAILS" | jq -r '.estadoTarea')
if [[ "$TASK_CC_CH1_STATE" == "Completado" ]]; then
    echo "VERIFICACIÓN CORRECTA: 'CC' (Capitulo 1) en estado Completado."
else
    check_failure "Estado de 'CC' (Capitulo 1) no es Completado." "$TASK_CC_CH1_DETAILS"
fi
echo ""

# --- Pruebas para ROLE_QC (c) con bloqueo de CC ---
echo -e "\n--- Pruebas para ROLE_QC ('c') con bloqueo de CC ---"

# Escenario: Capítulo 1 tiene 'CC' en Completado. 'c' (QC) NO DEBE poder cambiar tareas.
# Tarea 1.1 está actualmente Asignado a 'c' (por 'c' en Paso 11, Escenario 1).
echo "--- Intento de 'c' (QC) cambiar 'Tarea 1.1' (Capitulo 1, CC Completado) a Completado (DEBE FALLAR) ---"
RESPONSE_C_1_1_COMPLETADO_FAIL=$(curl -s -w "\n%{http_code}" -X PUT \
     -H "Content-Type: application/json" \
     -u "c:c" \
     -d '{"username": "c", "nuevoEstado": "Completado"}' \
     "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%201/tareas/Tarea%201.1/estado")
HTTP_CODE_C_1_1_COMPLETADO_FAIL=$(echo "$RESPONSE_C_1_1_COMPLETADO_FAIL" | tail -n1)
BODY_C_1_1_COMPLETADO_FAIL=$(echo "$RESPONSE_C_1_1_COMPLETADO_FAIL" | sed '$d')

if [[ "$HTTP_CODE_C_1_1_COMPLETADO_FAIL" -eq 403 ]]; then # Expecting 403 for CcTaskCompletedException
    echo "VERIFICACIÓN CORRECTA: 'c' (QC) falló al cambiar 'Tarea 1.1' a Completado (Código: $HTTP_CODE_C_1_1_COMPLETADO_FAIL)."
else
    check_failure "Error: 'c' (QC) PUDO cambiar 'Tarea 1.1' a Completado cuando DEBIÓ FALLAR (Código: $HTTP_CODE_C_1_1_COMPLETADO_FAIL)." "$BODY_C_1_1_COMPLETADO_FAIL"
fi
echo ""

# Tarea 1.2 está actualmente Completado por 's'.
echo "--- Intento de 'c' (QC) cambiar 'Tarea 1.2' (Capitulo 1, CC Completado) a Repetir (DEBE FALLAR) ---"
RESPONSE_C_1_2_REPETIR_FAIL=$(curl -s -w "\n%{http_code}" -X PUT \
     -H "Content-Type: application/json" \
     -u "c:c" \
     -d '{"username": "c", "nuevoEstado": "Repetir"}' \
     "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%201/tareas/Tarea%201.2/estado")
HTTP_CODE_C_1_2_REPETIR_FAIL=$(echo "$RESPONSE_C_1_2_REPETIR_FAIL" | tail -n1)
BODY_C_1_2_REPETIR_FAIL=$(echo "$RESPONSE_C_1_2_REPETIR_FAIL" | sed '$d')

if [[ "$HTTP_CODE_C_1_2_REPETIR_FAIL" -eq 403 ]]; then # Expecting 403 for CcTaskCompletedException
    echo "VERIFICACIÓN CORRECTA: 'c' (QC) falló al cambiar 'Tarea 1.2' a Repetir (Código: $HTTP_CODE_C_1_2_REPETIR_FAIL)."
else
    check_failure "Error: 'c' (QC) PUDO cambiar 'Tarea 1.2' a Repetir cuando DEBIÓ FALLAR (Código: $HTTP_CODE_C_1_2_REPETIR_FAIL)." "$BODY_C_1_2_REPETIR_FAIL"
fi
echo ""

# Escenario: Capítulo 2 tiene 'CC' en Completado (por 'c' en Paso 11, Escenario 4).
# Intento de 'c' (QC) cambiar 'Tarea 2.2' (NoAsignado) a Asignado (DEBE FALLAR)
echo "--- Intento de 'c' (QC) cambiar 'Tarea 2.2' (Capitulo 2, CC Completado) a Asignado (DEBE FALLAR) ---"
RESPONSE_C_2_2_ASIGNADO_FAIL=$(curl -s -w "\n%{http_code}" -X PUT \
     -H "Content-Type: application/json" \
     -u "c:c" \
     -d '{"username": "c", "nuevoEstado": "Asignado"}' \
     "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%202/tareas/Tarea%202.2/estado")
HTTP_CODE_C_2_2_ASIGNADO_FAIL=$(echo "$RESPONSE_C_2_2_ASIGNADO_FAIL" | tail -n1)
BODY_C_2_2_ASIGNADO_FAIL=$(echo "$RESPONSE_C_2_2_ASIGNADO_FAIL" | sed '$d')

if [[ "$HTTP_CODE_C_2_2_ASIGNADO_FAIL" -eq 403 ]]; then # Expecting 403 for CcTaskCompletedException
    echo "VERIFICACIÓN CORRECTA: 'c' (QC) falló al cambiar 'Tarea 2.2' a Asignado (Código: $HTTP_CODE_C_2_2_ASIGNADO_FAIL)."
else
    check_failure "Error: 'c' (QC) PUDO cambiar 'Tarea 2.2' a Asignado cuando DEBIÓ FALLAR (Código: $HTTP_CODE_C_2_2_ASIGNADO_FAIL)." "$BODY_C_2_2_ASIGNADO_FAIL"
fi
echo ""

# 's' (LIDER) cambia 'Tarea 2.2' (Capitulo 2, CC Completado) a Asignado (DEBE PASAR)
echo "--- LIDER 's' cambia 'Tarea 2.2' (Capitulo 2, CC Completado) a Asignado (DEBE PASAR) ---"
RESPONSE_S_2_2_ASIGNADO=$(curl -s -w "\n%{http_code}" -X PUT \
     -H "Content-Type: application/json" \
     -u "s:s" \
     -d '{"username": "s", "nuevoEstado": "Asignado"}' \
     "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%202/tareas/Tarea%202.2/estado")
HTTP_CODE_S_2_2_ASIGNADO=$(echo "$RESPONSE_S_2_2_ASIGNADO" | tail -n1)
BODY_S_2_2_ASIGNADO=$(echo "$RESPONSE_S_2_2_ASIGNADO" | sed '$d')

if [[ "$HTTP_CODE_S_2_2_ASIGNADO" -ne 200 ]]; then
    check_failure "LIDER 's' falló al cambiar 'Tarea 2.2' a Asignado." "$BODY_S_2_2_ASIGNADO"
fi
echo "Respuesta LIDER 's' cambio estado 'Tarea 2.2': $HTTP_CODE_S_2_2_ASIGNADO (Correcto)"

TASK_2_2_DETAILS=$(curl -s -u "s:s" "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%202/tareas/Tarea%202.2")
TASK_2_2_STATE=$(echo "$TASK_2_2_DETAILS" | jq -r '.estadoTarea')
TASK_2_2_ASSIGNED=$(echo "$TASK_2_2_DETAILS" | jq -r '.usuarioAsignado')
if [[ "$TASK_2_2_STATE" == "Asignado" && "$TASK_2_2_ASSIGNED" == "s" ]]; then
    echo "VERIFICACIÓN CORRECTA: 'Tarea 2.2' en estado Asignado y asignada a 's'."
else
    check_failure "Estado de 'Tarea 2.2' no es Asignado o no asignada a 's'." "$TASK_2_2_DETAILS"
fi
echo ""

# --- Pruebas para ROLE_USER (a) con bloqueo de CC ---
echo -e "\n--- Pruebas para ROLE_USER ('a') con bloqueo de CC ---"

# Escenario: Capítulo 1 tiene 'CC' en Completado. 'a' (USER) NO DEBE poder cambiar tareas.
# Tarea 1.1 está actualmente Asignado a 'c'. 'a' no puede cambiarla por CC bloqueo.
echo "--- Intento de 'a' (USER) cambiar 'Tarea 1.1' (Capitulo 1, CC Completado, Asignada a 'c') a Completado (DEBE FALLAR) ---"
RESPONSE_A_1_1_COMPLETADO_FAIL=$(curl -s -w "\n%{http_code}" -X PUT \
     -H "Content-Type: application/json" \
     -u "a:a" \
     -d '{"username": "a", "nuevoEstado": "Completado"}' \
     "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%201/tareas/Tarea%201.1/estado")
HTTP_CODE_A_1_1_COMPLETADO_FAIL=$(echo "$RESPONSE_A_1_1_COMPLETADO_FAIL" | tail -n1)
BODY_A_1_1_COMPLETADO_FAIL=$(echo "$RESPONSE_A_1_1_COMPLETADO_FAIL" | sed '$d')

if [[ "$HTTP_CODE_A_1_1_COMPLETADO_FAIL" -eq 403 ]]; then # Expecting 403 for CcTaskCompletedException
    echo "VERIFICACIÓN CORRECTA: 'a' (USER) falló al cambiar 'Tarea 1.1' a Completado (Código: $HTTP_CODE_A_1_1_COMPLETADO_FAIL)."
else
    check_failure "Error: 'a' (USER) PUDO cambiar 'Tarea 1.1' a Completado cuando DEBIÓ FALLAR (Código: $HTTP_CODE_A_1_1_COMPLETADO_FAIL)." "$BODY_A_1_1_COMPLETADO_FAIL"
fi
echo ""

# Tarea 1.3 está actualmente Completado por 's'. 'a' no puede cambiarla por CC bloqueo.
echo "--- Intento de 'a' (USER) cambiar 'Tarea 1.3' (Capitulo 1, CC Completado, Completado por 's') a Repetir (DEBE FALLAR) ---"
RESPONSE_A_1_3_REPETIR_FAIL=$(curl -s -w "\n%{http_code}" -X PUT \
     -H "Content-Type: application/json" \
     -u "a:a" \
     -d '{"username": "a", "nuevoEstado": "Repetir"}' \
     "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%201/tareas/Tarea%201.3/estado")
HTTP_CODE_A_1_3_REPETIR_FAIL=$(echo "$RESPONSE_A_1_3_REPETIR_FAIL" | tail -n1)
BODY_A_1_3_REPETIR_FAIL=$(echo "$RESPONSE_A_1_3_REPETIR_FAIL" | sed '$d')

if [[ "$HTTP_CODE_A_1_3_REPETIR_FAIL" -eq 403 ]]; then # Expecting 403 for CcTaskCompletedException
    echo "VERIFICACIÓN CORRECTA: 'a' (USER) falló al cambiar 'Tarea 1.3' a Repetir (Código: $HTTP_CODE_A_1_3_REPETIR_FAIL)."
else
    check_failure "Error: 'a' (USER) PUDO cambiar 'Tarea 1.3' a Repetir cuando DEBIÓ FALLAR (Código: $HTTP_CODE_A_1_3_REPETIR_FAIL)." "$BODY_A_1_3_REPETIR_FAIL"
fi
echo ""

# Escenario: Capítulo 2 tiene 'CC' en Completado. 'a' (USER) NO DEBE poder cambiar tareas.
# Tarea 2.3 está actualmente NoAsignado.
echo "--- Intento de 'a' (USER) cambiar 'Tarea 2.3' (Capitulo 2, CC Completado, NoAsignado) a Asignado (DEBE FALLAR) ---"
RESPONSE_A_2_3_ASIGNADO_FAIL=$(curl -s -w "\n%{http_code}" -X PUT \
     -H "Content-Type: application/json" \
     -u "a:a" \
     -d '{"username": "a", "nuevoEstado": "Asignado"}' \
     "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%202/tareas/Tarea%202.3/estado")
HTTP_CODE_A_2_3_ASIGNADO_FAIL=$(echo "$RESPONSE_A_2_3_ASIGNADO_FAIL" | tail -n1)
BODY_A_2_3_ASIGNADO_FAIL=$(echo "$RESPONSE_A_2_3_ASIGNADO_FAIL" | sed '$d')

if [[ "$HTTP_CODE_A_2_3_ASIGNADO_FAIL" -eq 403 ]]; then # Expecting 403 for CcTaskCompletedException
    echo "VERIFICACIÓN CORRECTA: 'a' (USER) falló al cambiar 'Tarea 2.3' a Asignado (Código: $HTTP_CODE_A_2_3_ASIGNADO_FAIL)."
else
    check_failure "Error: 'a' (USER) PUDO cambiar 'Tarea 2.3' a Asignado cuando DEBIÓ FALLAR (Código: $HTTP_CODE_A_2_3_ASIGNADO_FAIL)." "$BODY_A_2_3_ASIGNADO_FAIL"
fi
echo ""

# --- Pruebas para asignación de tareas por LIDER (s) ---
echo -e "\n--- Pruebas para asignación de tareas por LIDER ('s') ---"

# 's' asigna "Tarea 2.4" a 'a' (ROLE_USER en grupo 's')
echo "--- LIDER 's' asigna 'Tarea 2.4' (NoAsignado) a 'a' ---"
RESPONSE_S_ASSIGN_2_4_TO_A=$(curl -s -w "\n%{http_code}" -X PUT \
     -H "Content-Type: application/json" \
     -u "s:s" \
     -d '{"liderUsername": "s", "asignadoUsername": "a"}' \
     "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%202/tareas/Tarea%202.4/asignado")
HTTP_CODE_S_ASSIGN_2_4_TO_A=$(echo "$RESPONSE_S_ASSIGN_2_4_TO_A" | tail -n1)
BODY_S_ASSIGN_2_4_TO_A=$(echo "$RESPONSE_S_ASSIGN_2_4_TO_A" | sed '$d')

if [[ "$HTTP_CODE_S_ASSIGN_2_4_TO_A" -ne 200 ]]; then
    check_failure "LIDER 's' falló al asignar 'Tarea 2.4' a 'a'." "$BODY_S_ASSIGN_2_4_TO_A"
fi
echo "Respuesta LIDER 's' asignando 'Tarea 2.4': $HTTP_CODE_S_ASSIGN_2_4_TO_A (Correcto)"

TASK_2_4_DETAILS=$(curl -s -u "s:s" "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%202/tareas/Tarea%202.4")
TASK_2_4_ASSIGNED=$(echo "$TASK_2_4_DETAILS" | jq -r '.usuarioAsignado')
if [[ "$TASK_2_4_ASSIGNED" == "a" ]]; then
    echo "VERIFICACIÓN CORRECTA: 'Tarea 2.4' asignada a 'a'."
else
    check_failure "Tarea 'Tarea 2.4' no asignada a 'a'." "$TASK_2_4_DETAILS"
fi
echo ""

# 's' re-asigna "Tarea 2.5" a 'c' (ROLE_QC en grupo 's')
echo "--- LIDER 's' asigna 'Tarea 2.5' (NoAsignado) a 'c' ---"
RESPONSE_S_ASSIGN_2_5_TO_C=$(curl -s -w "\n%{http_code}" -X PUT \
     -H "Content-Type: application/json" \
     -d '{"liderUsername": "s", "asignadoUsername": "c"}' \
     "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%202/tareas/Tarea%202.5/asignado")
HTTP_CODE_S_ASSIGN_2_5_TO_C=$(echo "$RESPONSE_S_ASSIGN_2_5_TO_C" | tail -n1)
BODY_S_ASSIGN_2_5_TO_C=$(echo "$RESPONSE_S_ASSIGN_2_5_TO_C" | sed '$d')

if [[ "$HTTP_CODE_S_ASSIGN_2_5_TO_C" -ne 200 ]]; then
    check_failure "LIDER 's' falló al asignar 'Tarea 2.5' a 'c'." "$BODY_S_ASSIGN_2_5_TO_C"
fi
echo "Respuesta LIDER 's' asignando 'Tarea 2.5': $HTTP_CODE_S_ASSIGN_2_5_TO_C (Correcto)"

TASK_2_5_DETAILS=$(curl -s -u "s:s" "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%202/tareas/Tarea%202.5")
TASK_2_5_ASSIGNED=$(echo "$TASK_2_5_DETAILS" | jq -r '.usuarioAsignado')
if [[ "$TASK_2_5_ASSIGNED" == "c" ]]; then
    echo "VERIFICACIÓN CORRECTA: 'Tarea 2.5' asignada a 'c'."
else
    check_failure "Tarea 'Tarea 2.5' no asignada a 'c'." "$TASK_2_5_DETAILS"
fi
echo ""

# --- Pruebas para no-LIDERES intentando asignar tareas (DEBE FALLAR) ---
echo -e "\n--- Pruebas para no-LIDERES intentando asignar tareas (DEBE FALLAR) ---"

# 'a' (ROLE_USER) intenta asignar "Tarea 2.4" a 'c'
echo "--- Intento de 'a' (USER) asignar 'Tarea 2.4' a 'c' (DEBE FALLAR) ---"
RESPONSE_A_ASSIGN_2_4_TO_C_FAIL=$(curl -s -w "\n%{http_code}" -X PUT \
     -H "Content-Type: application/json" \
     -u "a:a" \
     -d '{"liderUsername": "a", "asignadoUsername": "c"}' \
     "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%202/tareas/Tarea%202.4/asignado")
HTTP_CODE_A_ASSIGN_2_4_TO_C_FAIL=$(echo "$RESPONSE_A_ASSIGN_2_4_TO_C_FAIL" | tail -n1)
BODY_A_ASSIGN_2_4_TO_C_FAIL=$(echo "$RESPONSE_A_ASSIGN_2_4_TO_C_FAIL" | sed '$d')

if [[ "$HTTP_CODE_A_ASSIGN_2_4_TO_C_FAIL" -eq 403 ]]; then # Expecting 403 for AssignmentForbiddenException
    echo "VERIFICACIÓN CORRECTA: 'a' (USER) falló al asignar 'Tarea 2.4' (Código: $HTTP_CODE_A_ASSIGN_2_4_TO_C_FAIL)."
else
    check_failure "Error: 'a' (USER) PUDO asignar 'Tarea 2.4' cuando DEBIÓ FALLAR (Código: $HTTP_CODE_A_ASSIGN_2_4_TO_C_FAIL)." "$BODY_A_ASSIGN_2_4_TO_C_FAIL"
fi
echo ""

# 'c' (ROLE_QC) intenta asignar "Tarea 2.5" a 'a'
echo "--- Intento de 'c' (QC) asignar 'Tarea 2.5' a 'a' (DEBE FALLAR) ---"
RESPONSE_C_ASSIGN_2_5_TO_A_FAIL=$(curl -s -w "\n%{http_code}" -X PUT \
     -H "Content-Type: application/json" \
     -d '{"liderUsername": "c", "asignadoUsername": "a"}' \
     "$BASE_URL/grupos/s/series/Serie%20de%20Prueba/capitulos/Capitulo%202/tareas/Tarea%202.5/asignado")
HTTP_CODE_C_ASSIGN_2_5_TO_A_FAIL=$(echo "$RESPONSE_C_ASSIGN_2_5_TO_A_FAIL" | tail -n1)
BODY_C_ASSIGN_2_5_TO_A_FAIL=$(echo "$RESPONSE_C_ASSIGN_2_5_TO_A_FAIL" | sed '$d')

if [[ "$HTTP_CODE_C_ASSIGN_2_5_TO_A_FAIL" -eq 403 ]]; then # Expecting 403 for AssignmentForbiddenException
    echo "VERIFICACIÓN CORRECTA: 'c' (QC) falló al asignar 'Tarea 2.5' (Código: $HTTP_CODE_C_ASSIGN_2_5_TO_A_FAIL)."
else
    check_failure "Error: 'c' (QC) PUDO asignar 'Tarea 2.5' cuando DEBIÓ FALLAR (Código: $HTTP_CODE_C_ASSIGN_2_5_TO_A_FAIL)." "$BODY_C_ASSIGN_2_5_TO_A_FAIL"
fi
echo ""



