from flask import Flask, request, jsonify
from dotenv import load_dotenv
import os

# Cargar variables del archivo .env
load_dotenv()

API_KEY = os.getenv("API_KEY")
SECRET_KEY = os.getenv("SECRET_KEY")

app = Flask(__name__)

@app.route("/")
def home():
    return "TradingMachine API está funcionando ✅"

@app.route("/order", methods=["POST"])
def create_order():
    data = request.json

    symbol = data.get("symbol")
    side = data.get("side")
    quantity = data.get("quantity")

    # Aquí iría la conexión con el exchange
    # Ejemplo: print(API_KEY, SECRET_KEY)

    return jsonify({
        "status": "ok",
        "message": "Orden recibida",
        "symbol": symbol,
        "side": side,
        "quantity": quantity
    })

if __name__ == "__main__":
    app.run(debug=True, port=5000)
