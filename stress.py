import asyncio
import aiohttp
import time
import matplotlib.pyplot as plt
from collections import Counter

# --- CONFIGURACIÓN ---
# ¡IMPORTANTE: CAMBIA ESTO POR TU URL REAL!
TARGET_URL = "https://sars-app.onrender.com" 
TOTAL_REQUESTS = 1000       # Peticiones totales a enviar
CONCURRENT_REQUESTS = 50    # Usuarios simultáneos ("hilos")
OUTPUT_FILE = "reporte_estres_final.png"
# ---------------------

results = []

async def fetch(session, url):
    start_time = time.time()
    status = 0
    try:
        # timeout bajo para detectar saturación rápido
        async with session.get(url, timeout=10) as response:
            status = response.status
            await response.read()
    except Exception:
        status = 999 # 999 = Error de conexión / Timeout
    finally:
        end_time = time.time()
        duration_ms = (end_time - start_time) * 1000
        results.append((start_time, duration_ms, status))
        
        if len(results) % 100 == 0:
            print(f"--> {len(results)}/{TOTAL_REQUESTS} completadas...")

async def worker(queue, session):
    while True:
        url = await queue.get()
        await fetch(session, url)
        queue.task_done()

async def run_load_test():
    print(f"--- INICIANDO TEST: {TARGET_URL} ---")
    print(f"--- Total: {TOTAL_REQUESTS} | Concurrencia: {CONCURRENT_REQUESTS} ---")
    
    queue = asyncio.Queue()
    
    # Llenamos la cola
    for _ in range(TOTAL_REQUESTS):
        queue.put_nowait(TARGET_URL)
        
    async with aiohttp.ClientSession() as session:
        tasks = [asyncio.create_task(worker(queue, session)) for _ in range(CONCURRENT_REQUESTS)]
        
        start_global = time.time()
        await queue.join() # Esperar a que se vacíe la cola
        total_time = time.time() - start_global
        
        for task in tasks: task.cancel()
            
    return total_time

def save_graphs(total_time):
    if not results:
        print("Error: No hay datos para graficar.")
        return

    timestamps = [r[0] for r in results]
    latencies = [r[1] for r in results]
    statuses = [r[2] for r in results]
    
    start_time = min(timestamps)
    relative_times = [t - start_time for t in timestamps]
    avg_rps = TOTAL_REQUESTS / total_time

    # Configuración del Lienzo (Canvas)
    fig, (ax1, ax2, ax3) = plt.subplots(3, 1, figsize=(10, 14))
    
    # Espaciado para que quepa el título grande
    plt.subplots_adjust(top=0.90, hspace=0.4, bottom=0.15) 

    # --- TÍTULO PRINCIPAL CON DATOS CLAVE ---
    main_title = (f"TEST DE ESTRÉS: {TARGET_URL}\n"
                  f"Peticiones Totales: {TOTAL_REQUESTS} | Concurrencia: {CONCURRENT_REQUESTS}")
    fig.suptitle(main_title, fontsize=14, fontweight='bold', color='#333333')

    # 1. Gráfica de Dispersión (Latencia)
    colors = ['#2ca02c' if s == 200 else '#d62728' for s in statuses] # Verde ok, Rojo error
    ax1.scatter(relative_times, latencies, c=colors, alpha=0.5, s=15, label='Request')
    ax1.set_title('Latencia individual (ms)', fontsize=10)
    ax1.set_ylabel('Tiempo de respuesta (ms)')
    ax1.grid(True, alpha=0.3)
    # Línea de referencia de 1 segundo
    ax1.axhline(y=1000, color='orange', linestyle='--', alpha=0.5, label='1 seg') 

    # 2. Gráfica de Pastel (Errores)
    status_counts = Counter(statuses)
    labels = [f"HTTP {k}" for k in status_counts.keys()]
    ax2.pie(status_counts.values(), labels=labels, autopct='%1.1f%%', 
            colors=['#99ff99','#ff9999','#66b3ff', '#ffcc99'])
    ax2.set_title('Distribución de Respuestas (Status Codes)', fontsize=10)

    # 3. Histograma
    ax3.hist(latencies, bins=40, color='#9467bd', alpha=0.7, edgecolor='black')
    ax3.set_title('Histograma de Latencia (Frecuencia)', fontsize=10)
    ax3.set_xlabel('Milisegundos')

    # --- CUADRO DE RESUMEN INFORMATIVO (ABAJO) ---
    summary_text = (
        f"RESUMEN ESTADÍSTICO:\n"
        f"• Duración del Test: {total_time:.2f} segundos\n"
        f"• RPS Promedio (Cliente): {avg_rps:.2f} req/s\n"
        f"• Latencia Máxima: {max(latencies):.2f} ms\n"
        f"• Latencia Mínima: {min(latencies):.2f} ms\n"
        f"• Total Errores (no 200): {len([s for s in statuses if s != 200])}"
    )
    # Añadimos el texto al pie de la imagen
    fig.text(0.5, 0.02, summary_text, ha='center', fontsize=11, 
             bbox=dict(facecolor='#f0f0f0', alpha=0.8, boxstyle='round,pad=1'))

    print(f"Guardando {OUTPUT_FILE}...")
    plt.savefig(OUTPUT_FILE)
    plt.close()
    print("¡Gráfica guardada con éxito!")

if __name__ == "__main__":
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    try:
        total_time = loop.run_until_complete(run_load_test())
        save_graphs(total_time)
    except KeyboardInterrupt:
        print("\nCancelado por el usuario.")
