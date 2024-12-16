import subprocess
import time

# Liste der Symbole und entsprechenden Skripte
scripts_with_symbols = [
    {"script": "ApiPollingScript_APPLE.py", "symbol": "AAPL"},
    {"script": "ApiPollingScript_NVIDIA.py", "symbol": "NVDA"},
    {"script": "ApiPollingScript_TESLA.py", "symbol": "TSLA"},
    {"script": "ApiPollingScript_SAP.py", "symbol": "SAP"}
]

def start_scripts():
    """Startet die angegebenen Skripte mit einer Verzögerung."""
    for entry in scripts_with_symbols:
        script = entry["script"]
        symbol = entry["symbol"]
        try:
            print(f"Starte {script} für Symbol {symbol}...")
            subprocess.Popen(["python", script])  # Startet das Skript als eigenen Prozess
            time.sleep(1)  # Verzögerung von 1 Sekunde
        except Exception as e:
            print(f"Fehler beim Starten von {script}: {e}")

if __name__ == "__main__":
    start_scripts()
