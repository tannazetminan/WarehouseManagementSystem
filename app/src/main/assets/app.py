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
#
# 3. To see the User Profile
#    curl http://localhost:8888/user/<user_id>  methods=['GET']
#
# 4. Update User Profile
#    curl http://localhost:8888/user/<user_id> methods=['PUT']
#
# 5. To create a new product:
# curl -X POST http://localhost:8888/create_product -H "Content-Type: application/json" -d '{  "prodName": "Product Name",  "prodDescription": "Description of the product",  "prodCategory": "Category of the product",  "salePrice": 100.00,  "costPrice": 50.00,  "quantity": 20,  "image_url": "path/to/image.jpg"}'
#
# 6. To retrieve all products:
# curl http://localhost:8888/retrieve_all_products
#
# 7. To retrieve a single product by ID:
# curl http://localhost:8888/retrieve_single_product/<product_id>
#
# 8. To delete a single product by ID:
# curl -X DELETE http://localhost:8888/delete_single_product/<product_id>
#
# 9. To update a product by ID:
# curl -X PUT http://localhost:8888/update_single_product/<product_id> -H "Content-Type: application/json" -d '{  "prodName": "Updated Product Name",  "prodDescription": "Updated Description",  "prodCategory": "Updated Category",  "salePrice": 150.00,  "costPrice": 75.00,  "quantity": 30,  "image_url": "path/to/updated_image.jpg"}'
#
# 10. To create a transaction:
#curl -X POST http://localhost:8888/transaction -H "Content-Type: application/json" -d '{  "user": "user_id_here",  "products": [    {"prodName": "Product Name", "salePrice": 100.00, "quantity": 2},    {"prodName": "Another Product", "salePrice": 50.00, "quantity": 1}  ],  "trans_date": "2024-11-14",  "trans_time": "14:30:00"}'
#
# 11. To see user's cart items
#   curl -X GET http://localhost:8888/cart/<user_id>
#
# 12. To Add an item to user's cart
#   curl -X Post http://localhost:8888/cart/<user_id>
#
# 13. To Add an item to user's cart
#   curl -X Post http://localhost:8888/cart/<user_id>
#
# 14. To remove an item from the cart
#   curl -X Post http://localhost:8888/cart/<user_id>/<product_id>
#
# 15. To get all transactions
# curl -X GET http://localhost:8888/retrieve_all_transactions
#
# 16. to get transaction by id
# curl -X GET http://localhost:8888/retrieve_transaction_by_id/<transaction_id>
#


from flask import Flask, session, jsonify, request, make_response
from flask_session import Session
from datetime import timedelta
from pymongo import MongoClient
import bcrypt
import os
from werkzeug.utils import secure_filename
from bson import ObjectId  # Import ObjectId from bson
from datetime import datetime

app = Flask(__name__)
app.secret_key = 'your_secret_key'  # Replace with a secure key in a production app
# MongoDB Atlas connection string (replace <username>, <password>, and <cluster-url> with your own)
client = MongoClient("mongodb+srv://tannaz:tannaz@cluster0.hpqre.mongodb.net")

# Use MongoDB to store sessions
app.config['SESSION_TYPE'] = 'mongodb'
app.config['SESSION_PERMANENT'] = False
app.config['SESSION_USE_SIGNER'] = True
app.config['SESSION_MONGODB'] = client.warehouse_management  # Connect to the MongoDB database
app.config['SESSION_MONGODB_COLLECT'] = 'sessions'  # Collection to store sessions
app.config['PERMANENT_SESSION_LIFETIME'] = timedelta(minutes=5)



# Allowed extensions for the uploaded images
ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'gif'}

# Configure the upload folder
UPLOAD_FOLDER = os.path.join(os.getcwd(), 'uploads', 'images')  # Folder where images will be stored

# Make sure the upload folder exists
if not os.path.exists(UPLOAD_FOLDER):
    os.makedirs(UPLOAD_FOLDER)

app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER


# Function to check if the file is an allowed image type
def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS



# Access the 'warehouse_managemen' database and 'users' collection
db = client.warehouse_management
users_collection = db.users
products_collection = db.products
transactions_collection = db.transactions
cart_collection = db.cart
sessions_collection = db.sessions


@app.route('/start_session/<username>', methods=['GET'])
def start_session(username):
    # Check if there is an active session for another user
    if '_id' in session:
        # Get the current session ID
        session_id = ObjectId(session['_id'])  # Convert to ObjectId for the current session

        # Fetch the existing session
        existing_session = sessions_collection.find_one({"_id": session_id})

        if existing_session:
            # Update the end_time of the existing (previous) session
            sessions_collection.update_one(
                {"_id": session_id},
                {"$set": {"end_time": datetime.now()}}  # Set the end time of the previous session
            )

        # You can now update the session to the new user's session
        session['username'] = username
        session['counter'] = 0

        # Save session data for the new user session
        session_data = {
            "username": username,
            "counter": session['counter'],
            "start_time": datetime.now(),
            "end_time": None  # Set end time as None initially
        }

        result = sessions_collection.insert_one(session_data)
        session['_id'] = str(result.inserted_id)  # Save the MongoDB ObjectId to the session
        return jsonify(message="Session started", user=username, counter=session['counter'])

    else:
        # If no session exists (first session), initialize the session for the user
        session['username'] = username
        session['counter'] = 0

        # Save session data for the new user session
        session_data = {
            "username": username,
            "counter": session['counter'],
            "start_time": datetime.now(),
            "end_time": None  # Set end time as None initially
        }

        result = sessions_collection.insert_one(session_data)
        session['_id'] = str(result.inserted_id)  # Save the MongoDB ObjectId to the session
        return jsonify(message="Session started", user=username, counter=session['counter'])

# Endpoint to retrieve all sessions for admins
@app.route('/get_all_sessions', methods=['GET'])
def get_all_sessions():
    # Fetch all sessions from the MongoDB database
    sessions = sessions_collection.find()
    session_list = []

    for session_data in sessions:
        session_data['_id'] = str(session_data['_id'])  # Convert ObjectId to string
        # Convert datetime fields to string
        if session_data.get('start_time'):
            session_data['start_time'] = session_data['start_time'].strftime("%Y-%m-%d %H:%M:%S")
        if session_data.get('end_time'):
            session_data['end_time'] = session_data['end_time'].strftime("%Y-%m-%d %H:%M:%S")

        session_list.append(session_data)

    return jsonify(session_list), 200

#
@app.route('/increment', methods=['GET'])
def increment():
    # Check if session is active
    if '_id' in session:  # Using session '_id' which is MongoDB's ObjectId for this session
        session_id = ObjectId(session['_id'])  # Convert to ObjectId

        # Find the session in MongoDB by _id
        session_data = sessions_collection.find_one({"_id": session_id})

        if session_data:
            # Increment the counter in MongoDB
            sessions_collection.update_one(
                {"_id": session_id},
                {"$inc": {"counter": 1}}  # Increment counter by 1
            )
            updated_session = sessions_collection.find_one({"_id": session_id})
            return jsonify(message="Counter incremented", counter=updated_session['counter'])
        else:
            return jsonify(message="Session data not found in database"), 404
    else:
        # Prompt user to start a session if none exists
        return jsonify(message="No active session found. Please start a session first using /start_session/<username>"), 404

#Not in use
@app.route('/get_session_data', methods=['GET'])
def get_session_data():
    if 'username' in session and 'counter' in session:
        return jsonify(message="Session data retrieved", user=session['username'], counter=session['counter'])
    else:
        return jsonify(message="No active session"), 404

@app.route('/end_session', methods=['GET'])
def end_session():
    if 'session_id' in session:
        session_id = session['session_id']
        # Update the session with the end time
        sessions_collection.update_one(
            {"session_id": session_id},
            {"$set": {"end_time": datetime.now()}}
        )
        session.clear()  # Clear the session from the server-side
        return jsonify(message="Session ended successfully"), 200
    else:
        return jsonify(message="No active session found"), 404

#USER APIs
# Endpoint to register
@app.route('/register', methods=['POST'])
def register():
    data = request.get_json()

    # Extract data from the request
    fullname = data.get('fullname')
    phone = data.get('phone')
    email = data.get('email')
    password = data.get('password')
    user_type = data.get('type')

    # Check if the email already exists
    if users_collection.find_one({"email": email}):
        return jsonify({"error": "Email already exists"}), 409

    # Hash the password
    hashed_password = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())

    # Create user data for insertion
    user_data = {
        "fullname": fullname,
        "phone": phone,
        "email": email,
        "password": hashed_password,
        "type": user_type
    }

    # Insert the user into the database
    users_collection.insert_one(user_data)

    return jsonify({"message": "User registered successfully"}), 201

# Endpoint to login
@app.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    email, password = data['email'], data['password']

    user = users_collection.find_one({"email": email})
    if user and bcrypt.checkpw(password.encode('utf-8'), user['password']):
        # Start session after successful login
        session['username'] = user["fullname"]  # Store the full name or username
        session['counter'] = 0  # Optionally initialize any session-related counters
        # Call the start_session function to create a MongoDB session record
        start_session(user["fullname"])  # Pass the username to start a session
        return jsonify({
            "message": "Login successful",
            "type": user["type"],
            "user_id": str(user["_id"]),  # Include user ID in the response
            "session_data": {
                "username": session['username'],
                "counter": session['counter']
            }
        }), 200
    return jsonify({"error": "Invalid credentials"}), 401

# Get User Profile
@app.route('/user/<user_id>', methods=['GET'])
def get_user_profile(user_id):
    user = users_collection.find_one({"_id": ObjectId(user_id)})
    if user:
        return jsonify({
            "fullname": user["fullname"],
            "email": user["email"],
            "phone": user["phone"]
        }), 200
    else:
        return jsonify({"error": "User not found"}), 404

# Update User Profile
@app.route('/user/<user_id>', methods=['PUT'])
def update_user_profile(user_id):
    data = request.get_json()
    updated_data = {
        "fullname": data.get("fullname"),
        "email": data.get("email"),
        "phone": data.get("phone")
    }

    result = users_collection.update_one({"_id": ObjectId(user_id)}, {"$set": updated_data})
    if result.matched_count > 0:
        return jsonify({"message": "Profile updated successfully"}), 200
    else:
        return jsonify({"error": "User not found"}), 404

# Endpoint to retrieve all users for admins
@app.route('/get_all_users', methods=['GET'])
def get_all_users():
    # Fetch all users from the MongoDB database
    users = users_collection.find()
    user_list = []

    for user_data in users:
        user_data['_id'] = str(user_data['_id'])  # Convert ObjectId to string
        user_list.append(user_data)

    return jsonify(user_list), 200

# PRODUCT APIs
# upload image
@app.route('/upload_image', methods=['POST'])
def upload_image():
    # Check if the 'file' is part of the request
    if 'file' not in request.files:
        return jsonify({"error": "No file part"}), 400

    file = request.files['file']

    # If the user does not select a file, the browser submits an empty part without a filename
    if file.filename == '':
        return jsonify({"error": "No selected file"}), 400

    # If the file is allowed, save it
    if file and allowed_file(file.filename):
        # Secure the filename to prevent any malicious code
        filename = secure_filename(file.filename)

        # Save the file to the upload folder
        file_path = os.path.join(app.config['UPLOAD_FOLDER'], filename)
        file.save(file_path)

        # Return the URL or path where the image is accessible
        image_url = f"uploads/images/{filename}"

        return jsonify({"image_url": image_url}), 200

    return jsonify({"error": "Invalid file format"}), 400

# Endpoint to create product
@app.route('/create_product', methods=['POST'])
def create_product():
    data = request.get_json()
    prod_name = data['prodName']
    prod_description = data.get('prodDescription', None)
    prod_category = data.get('prodCategory', None)
    sale_price = data['salePrice']
    cost_price = data['costPrice']
    quantity = data.get('quantity', None)
    image_url = data.get('image_url', None)

    # Create the product data dictionary without the prodID
    product_data = {
        "prodName": prod_name,
        "prodDescription": prod_description,
        "prodCategory": prod_category,
        "salePrice": sale_price,
        "costPrice": cost_price,
        "quantity": quantity,
        "image_url": image_url  # Store the image URL here
    }

    # Insert the product into MongoDB
    product = db.products.insert_one(product_data)

    # Return the inserted product's _id (MongoDB autogenerated)
    return jsonify({"message": "Product created successfully", "product_id": str(product.inserted_id)}), 201

# API to get the image from server backend folder
@app.route('/uploads/images/<filename>')
def uploaded_file(filename):
    return send_from_directory(os.path.join(app.root_path, 'uploads', 'images'), filename)

# Endpoint to retrieve all products
@app.route('/retrieve_all_products', methods=['GET'])
def retrieve_all_products():
    # Fetch all products from the MongoDB database
    products = products_collection.find()
    products_list = []

    # Convert MongoDB cursor to a list of dictionaries
    for product in products:
        product['_id'] = str(product['_id'])  # Convert ObjectId to string
        if product['image_url'].startswith('/uploads/images'):
            product['image_url'] = request.host_url + product['image_url']  #Append the full image URL for those images stored in the backend forlder
        products_list.append(product)

    return jsonify(products_list), 200

# Endpoint to retrieve a single product by its ID
@app.route('/retrieve_single_product/<product_id>', methods=['GET'])
def retrieve_single_product(product_id):
    try:
        # Convert the string to an ObjectId
        product = products_collection.find_one({"_id": ObjectId(product_id)})

        if product:
            # Convert the ObjectId to string for the response
            product['_id'] = str(product['_id'])
            if product['image_url'].startswith('/uploads/images'):
                product['image_url'] = request.host_url + product['image_url']  #Append the full image URL for those images stored in the backend forlder
            return jsonify(product), 200
        else:
            return jsonify({"error": "Product not found"}), 404
    except Exception as e:
        return jsonify({"error": f"Invalid ID format: {str(e)}"}), 400

# Endpoint to delete a single product by its ID
@app.route('/delete_single_product/<product_id>', methods=['DELETE'])
def delete_single_product(product_id):
    try:
        # Convert the string product_id to ObjectId
        result = products_collection.delete_one({"_id": ObjectId(product_id)})

        if result.deleted_count > 0:
            return jsonify({"message": "Product deleted successfully"}), 200
        else:
            return jsonify({"error": "Product not found"}), 404
    except Exception as e:
        return jsonify({"error": f"Invalid ID format: {str(e)}"}), 400

# Endpoint to update a single product by its ID
@app.route('/update_single_product/<product_id>', methods=['PUT'])
def update_single_product(product_id):
    data = request.get_json()

    try:
        # Fetch the existing product from the database by ObjectId
        product = products_collection.find_one({"_id": ObjectId(product_id)})

        if not product:
            return jsonify({"error": "Product not found"}), 404

        # Prepare the updated product data
        updated_data = {
            "prodName": data.get('prodName', product['prodName']),
            "prodDescription": data.get('prodDescription', product['prodDescription']),
            "prodCategory": data.get('prodCategory', product['prodCategory']),
            "salePrice": data.get('salePrice', product['salePrice']),
            "costPrice": data.get('costPrice', product['costPrice']),
            "quantity": data.get('quantity', product['quantity']),
            "image_url": data.get('image_url', product['image_url'])
        }

        # Update the product in the MongoDB database
        result = products_collection.update_one({"_id": ObjectId(product_id)}, {"$set": updated_data})

        if result.matched_count > 0:
            return jsonify({"message": "Product updated successfully"}), 200
        else:
            return jsonify({"error": "Failed to update product"}), 400
    except Exception as e:
        return jsonify({"error": f"Invalid ID format: {str(e)}"}), 400

# Endpoint to update product quantity
@app.route('/update_productQuantity/<product_id>', methods=['PUT'])
def update_product_quantity(product_id):
    # Ensure that the product_id is a valid ObjectId
    if not ObjectId.is_valid(product_id):
        return jsonify({"error": "Invalid product ID format"}), 400

    # Parse the request body
    data = request.get_json()

    # Validate that the quantity is provided and is a valid string
    if 'quantity' not in data or not isinstance(data['quantity'], str):
        return jsonify({"error": "Invalid quantity provided"}), 400

    try:
        # Fetch the existing product by its ID
        product = products_collection.find_one({"_id": ObjectId(product_id)})

        # If the product is not found, return an error message
        if not product:
            return jsonify({"error": "Product not found"}), 404

        # Set the new quantity in the database (replace the existing quantity)
        result = products_collection.update_one(
            {"_id": ObjectId(product_id)},
            {"$set": {"quantity": data['quantity']}}  # Set new quantity (as a string)
        )

        # Check if the product was updated successfully
        if result.matched_count > 0:
            return jsonify({"message": f"Product quantity updated to {data['quantity']} successfully"}), 200
        else:
            return jsonify({"error": "Failed to update product quantity"}), 400

    except Exception as e:
        return jsonify({"error": f"An error occurred: {str(e)}"}), 500

# TRANSACTION APIs
# Endpoint to create transaction
@app.route('/create_transaction', methods=['POST'])
def create_transaction():
    data = request.get_json()
    user_id = data['user_id']  # User ID from the transaction request
    products = data['products']  # List of products in the transaction
    # Get the current date and time
    current_datetime = datetime.now()
    # Set 'trans_date' to today's date and 'trans_time' to the current time
    trans_date = current_datetime.date().strftime('%Y-%m-%d')  # Format date as 'YYYY-MM-DD'
    trans_time = current_datetime.strftime('%H:%M:%S')  # Format time as 'HH:MM:SS'

    # Iterate over each product in the transaction to update its quantity
    for product_data in products:
        prod_name = product_data['prodName']
        quantity_purchased = int(product_data['quantity'])  # Convert the purchased quantity to an integer

        # Find the product in the database by its name
        product = products_collection.find_one({"prodName": prod_name})

        if product:
            # Convert the stored quantity (string) to an integer
            current_quantity = int(product['quantity'])

            # Check if there is enough stock
            if current_quantity >= quantity_purchased:
                # Calculate the new quantity
                new_quantity = current_quantity - quantity_purchased

                # Update the product quantity in the database
                products_collection.update_one(
                    {"prodName": prod_name},
                    {"$set": {"quantity": str(new_quantity)}}  # Store the updated quantity as a string
                )
            else:
                return jsonify({"error": f"Not enough stock for {prod_name}"}), 400
        else:
            return jsonify({"error": f"Product {prod_name} not found"}), 404

    # You can also save the transaction data if needed
    transaction_data = {
        "user_id": user_id,
        "products": products,
        "trans_date": trans_date,
        "trans_time": trans_time
    }
    transactions_collection.insert_one(transaction_data)

    return jsonify({"message": "Transaction completed successfully"}), 201

# Endpoint to retrieve all transactions
@app.route('/retrieve_all_transactions', methods=['GET'])
def retrieve_all_transactions():
    transactions = transactions_collection.find()
    transactions_list = []

    for transaction in transactions:
        transaction['_id'] = str(transaction['_id'])  # Convert ObjectId to string
        transactions_list.append(transaction)

    return jsonify(transactions_list), 200

# Endpoint to retrieve a specific transaction by ID
@app.route('/retrieve_transaction_by_id/<transaction_id>', methods=['GET'])
def retrieve_transaction_by_id(transaction_id):
    transaction = transactions_collection.find_one({"_id": ObjectId(transaction_id)})

    if transaction:
        transaction['_id'] = str(transaction['_id'])
        return jsonify(transaction), 200
    else:
        return jsonify({"error": "Transaction not found"}), 404

# CART APIs
# Endpoint to add a product to the cart
@app.route("/cart/<user_id>/add", methods=["POST"])
def add_to_cart(user_id):
    data = request.json
    product_id = data.get("productId")

    if not product_id:
        return jsonify({"error": "Product ID is required"}), 400

    try:
        # Convert the product_id to ObjectId
        product_object_id = ObjectId(product_id)
    except Exception as e:
        return jsonify({"error": "Invalid Product ID format"}), 400

    # Check if the product exists in the database
    product = products_collection.find_one({"_id": product_object_id})
    if not product:
        return jsonify({"error": "Product not found"}), 404

    # Check if the product is already in the user's cart
    existing_item = cart_collection.find_one({"user_id": user_id, "product_id": product_id})
    if existing_item:
        return jsonify({"message": "Product already in cart"}), 200

    # Add product to the cart
    cart_item = {
        "user_id": user_id,
        "product_id": product_id
    }
    cart_collection.insert_one(cart_item)

    # Now increment the session counter
    session['username'] = user_id  # Assuming session is associated with user_id
    increment()

    return jsonify({"message": "Product added to cart"}), 201

# Endpoint to get all items in the cart
@app.route("/cart/<user_id>", methods=["GET"])
def get_cart_items(user_id):
    cart_items = list(cart_collection.find({"user_id": user_id}))
    if not cart_items:
        return jsonify([]), 200

    # Convert product_id strings to ObjectId
    product_ids = [ObjectId(item["product_id"]) for item in cart_items]

    # Retrieve product details for each product in the cart
    products = list(products_collection.find({"_id": {"$in": product_ids}}))

    # Convert MongoDB objects to JSON-friendly format
    for product in products:
        product["_id"] = str(product["_id"])

    return jsonify(products), 200

# Endpoint to remov one item from the cart
@app.route("/cart/<user_id>/<product_id>", methods=["DELETE"])
def clear_cart_item(user_id, product_id):
    result = cart_collection.delete_one({"user_id": user_id, "product_id": product_id})
    if result.deleted_count == 0:
        return jsonify({"error": "Item not found in cart"}), 404
    return jsonify({"message": "Item removed from cart"}), 200

# Endpoint to clear the cart
@app.route("/cart/<user_id>/clear", methods=["DELETE"])
def clear_cart(user_id):
    result = cart_collection.delete_many({"user_id": user_id})
    return jsonify({"message": f"Deleted {result.deleted_count} items from the cart"}), 200

if __name__ == '__main__':
    # Run the application on all available IPs on port 8888
    app.run(host='0.0.0.0', port=8888)
