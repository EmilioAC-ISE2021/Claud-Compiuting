import requests
import time
import statistics
import matplotlib.pyplot as plt
import os
from datetime import datetime

# --- CONFIGURACIÓN ---
TARGET_URL = "https://sars-app.onrender.com" # <--- TU URL AQUÍ
ENDPOINT = "/login"       
REQUESTS_PER_SCENARIO = 50
OUTPUT_DIR = "performance_report_final"

# Asegurar que existe el directorio
if not os.path.exists(OUTPUT_DIR):
    os.makedirs(OUTPUT_DIR)

def run_scenario(name, url, count):
    print(f"\n🚀 Ejecutando: {name} ({count} peticiones)...")
    durations = []
    errors = 0
    start_time_total = time.time()
    
    for i in range(count):
        req_start = time.time()
        try:
            # Timeout alto (120s) para soportar el Cold Start sin cancelar
            resp = requests.get(url, timeout=120)
            duration = (time.time() - req_start) * 1000
            durations.append(duration)
            print(f"   [{i+1:02d}/{count}] {duration:6.0f} ms | Status: {resp.status_code}")
        except Exception as e:
            print(f"   [{i+1:02d}/{count}] FALLO: {e}")
            durations.append(0)
            errors += 1
            
    total_time = time.time() - start_time_total
    return {
        "name": name, 
        "durations": durations, 
        "total_time": total_time,
        "errors": errors
    }

def generate_assets(scenario_a, scenario_b):
    # 1. GENERAR GRÁFICA (comparison_chart.png)
    plt.figure(figsize=(10, 6))
    
    # Datos
    x = range(1, len(scenario_a['durations']) + 1)
    
    # Serie A (Cold Start)
    plt.plot(x, scenario_a['durations'], 'o-', color='#e74c3c', label='Escenario A (Con Cold Start)', linewidth=2)
    
    # Serie B (Warm)
    plt.plot(x, scenario_b['durations'], 's-', color='#2ecc71', label='Escenario B (Sin Cold Start)', linewidth=2)
    
    # Etiquetas y Estilo
    plt.title(f'Rendimiento: {TARGET_URL}')
    plt.xlabel('Número de Petición')
    plt.ylabel('Tiempo de Respuesta (ms)')
    plt.grid(True, alpha=0.3)
    plt.legend()
    
    # Anotación visual del Cold Start
    max_val = max(scenario_a['durations'])
    if max_val > 0:
        plt.annotate(f'Cold Start: {max_val/1000:.1f}s', 
                     xy=(1, max_val), 
                     xytext=(3, max_val),
                     arrowprops=dict(facecolor='black', shrink=0.05))

    img_filename = "comparison_chart.png"
    plt.tight_layout()
    plt.savefig(f"{OUTPUT_DIR}/{img_filename}")
    plt.close()
    print(f"\n📸 Gráfica guardada: {OUTPUT_DIR}/{img_filename}")

    # 2. GENERAR REPORTE (.md)
    avg_b = statistics.mean(scenario_b['durations'])
    cold_start_time = scenario_a['durations'][0]
    
    md_content = f"""
# Auditoría de Rendimiento SARS App
**Fecha:** {datetime.now().strftime("%Y-%m-%d %H:%M")}
**Objetivo:** `{TARGET_URL}{ENDPOINT}`

## Resumen Ejecutivo
Se realizaron dos pruebas secuenciales para aislar el impacto del "arranque en frío" de Render.

| Métrica | Escenario A (Arranque) | Escenario B (Estable) |
| :--- | :--- | :--- |
| **Tiempo Total (N={REQUESTS_PER_SCENARIO})** | {scenario_a['total_time']:.2f} s | {scenario_b['total_time']:.2f} s |
| **Peor Petición** | **{cold_start_time:.0f} ms** | {max(scenario_b['durations']):.0f} ms |
| **Promedio** | {statistics.mean(scenario_a['durations']):.0f} ms | **{avg_b:.0f} ms** |

## Análisis Visual
El siguiente gráfico compara la latencia de cada petición en ambos escenarios.

![Gráfica de Comparativa]({img_filename})

> **Nota:** La línea roja muestra el impacto inicial del servidor despertando. La línea verde representa la experiencia real del usuario una vez el sistema está activo.

## Conclusión
El tiempo de *Cold Start* es de **{cold_start_time/1000:.2f} segundos**. Una vez superado este primer acceso, la aplicación responde consistentemente en torno a **{avg_b:.0f} ms**.
    """
    
    md_filename = "reporte_rendimiento.md"
    with open(f"{OUTPUT_DIR}/{md_filename}", "w", encoding="utf-8") as f:
        f.write(md_content)
    print(f"📝 Reporte guardado: {OUTPUT_DIR}/{md_filename}")

def main():
    print("--- INICIANDO AUDITORÍA ---")
    print("⚠️ IMPORTANTE: Si la app no lleva >15 min inactiva, el Cold Start no será real.\n")
    
    # Escenario A (Con Cold Start)
    res_a = run_scenario("Escenario A (Cold Start)", TARGET_URL + ENDPOINT, REQUESTS_PER_SCENARIO)
    
    print("\n--- Enfriando (2s) ---\n")
    time.sleep(2)
    
    # Escenario B (Sin Cold Start)
    res_b = run_scenario("Escenario B (Warm)", TARGET_URL + ENDPOINT, REQUESTS_PER_SCENARIO)
    
    # Generar todo
    generate_assets(res_a, res_b)
    print("\n✅ Auditoría finalizada con éxito.")

if __name__ == "__main__":
    main()
