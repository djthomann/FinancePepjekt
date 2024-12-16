import requests
import time
import json

# Konfigurationen
API_URL = "https://finnhub.io/api/v1/quote"  # Ersetze durch die tatsächliche URL
TOKEN = "ct2r2bhr01qiurr42bq0ct2r2bhr01qiurr42bqg"
SYMBOL = "SAP"  # Das Symbol, das du abrufen möchtest
OUTPUT_FILE = f"stock_data_{SYMBOL}.json"  # Dateiname, in dem die Daten gespeichert werden
INTERVAL = 5  # Abfrageintervall in Sekunden
# Zähler für fortlaufende Nummern
counter = 0

def fetch_stock_data(symbol):
    """Ruft Echtzeitdaten für ein bestimmtes Symbol vom API-Endpunkt ab."""
    try:
        response = requests.get(f"{API_URL}?symbol={symbol}&token={TOKEN}")
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        print(f"Fehler beim Abrufen der Daten: {e}")
        return None

def save_data_to_file(data, filename):
    """Speichert die Daten in eine Datei (JSON-Format)."""
    try:
        with open(filename, "a") as file:
            file.write(json.dumps(data) + "\n")
    except Exception as e:
        print(f"Fehler beim Speichern der Daten: {e}")

def main():
    """Hauptfunktion zum regelmäßigen Abrufen und Speichern von Aktienkursen."""
    global counter
    print("Starte Abfrage von API-Daten...")
    while True:
        data = fetch_stock_data(SYMBOL)
        if data:
            counter += 1
            data["id"] = counter  # Füge die fortlaufende Nummer hinzu
            print(f"Daten abgerufen: {data}")
            save_data_to_file(data, OUTPUT_FILE)
        time.sleep(INTERVAL)

if __name__ == "__main__":
    main()
