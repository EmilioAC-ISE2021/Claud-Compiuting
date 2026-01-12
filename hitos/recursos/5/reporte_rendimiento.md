
# Auditoría de Rendimiento SARS App
**Fecha:** 2026-01-12 14:26
**Objetivo:** `https://sars-app.onrender.com/login`

## Resumen Ejecutivo
Se realizaron dos pruebas secuenciales para aislar el impacto del "arranque en frío" de Render.

| Métrica | Escenario A (Arranque) | Escenario B (Estable) |
| :--- | :--- | :--- |
| **Tiempo Total (N=250)**| 35.87 s |
| **Peor Petición** | **158 ms** | 480 ms |
| **Promedio** | 143 ms | **143 ms** |

## Análisis Visual
El siguiente gráfico compara la latencia de cada petición en ambos escenarios.

![Gráfica de Comparativa](comparison_chart.png)

> **Nota:** La línea roja muestra el impacto inicial del servidor despertando. La línea verde representa la experiencia real del usuario una vez el sistema está activo.

## Conclusión
El tiempo de *Cold Start* es de **0.16 segundos**. Una vez superado este primer acceso, la aplicación responde consistentemente en torno a **143 ms**.
    