# Flask API for WarehouseManagementSystem Data from MongoDB
# --------------------------------------
# How to Install Flask for WarehouseManagementSystem:
# python3 app.py
# --------------------------------------
# Run the following commands to install The App:
# pip install pymongo bcrypt
#
# --------------------------------------
# curl Commands to Test the API:
# --------------------------------------
# 1. To login for a particular user:
#    curl http://localhost:8888/login
#
# 2. To register for a customer or an admin with "CSIS4280" code
#    curl http://localhost:8888/register

from flask import Flask, jsonify, request
from pymongo import MongoClient
import bcrypt

app = Flask(__name__)

# MongoDB Atlas connection string (replace <username>, <password>, and <cluster-url> with your own)
client = MongoClient("mongodb+srv://tannaz:tannaz@cluster0.hpqre.mongodb.net")


# Access the 'warehouse_managemen' database and 'users' collection
db = client.warehouse_management
users_collection = db.users



# Endpoint to register
@app.route('/register', methods=['POST'])
def register():
    data = request.get_json()
    fullname, phone, email,password,  user_type  = data['fullname'], data['phone'], data['email'], data['password'], data['type']
    
    if users_collection.find_one({"email": email}):
        return jsonify({"error": "email already exists"}), 409
    
    hashed_password = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())
    user_data = {
        "fullname": fullname,
        "phone": phone,
        "email": email,
        "password": hashed_password,
        "type": user_type
    }
    users_collection.insert_one(user_data)
    return jsonify({"message": "User registered successfully"}), 201


# Endpoint to login
@app.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    email, password = data['email'], data['password']
    
    user = users_collection.find_one({"email": email})
    if user and bcrypt.checkpw(password.encode('utf-8'), user['password']):
        return jsonify({
            "message": "Login successful",
            "type": user["type"]  # Pass user type to route frontend accordingly
        }), 200
    return jsonify({"error": "Invalid credentials"}), 401


if __name__ == '__main__':
    # Run the application on all available IPs on port 8888
    app.run(host='0.0.0.0', port=8888)
