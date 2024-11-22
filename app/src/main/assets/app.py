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


from flask import Flask, jsonify, request
from pymongo import MongoClient
import bcrypt
import os
from werkzeug.utils import secure_filename
from bson import ObjectId  # Import ObjectId from bson

app = Flask(__name__)

# MongoDB Atlas connection string (replace <username>, <password>, and <cluster-url> with your own)
client = MongoClient("mongodb+srv://tannaz:tannaz@cluster0.hpqre.mongodb.net")

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
            "type": user["type"],
            "user_id": str(user["_id"])  # Include user ID in the response
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
        image_url = f"/uploads/images/{filename}"

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

# Endpoint to retrieve all products
@app.route('/retrieve_all_products', methods=['GET'])
def retrieve_all_products():
    # Fetch all products from the MongoDB database
    products = products_collection.find()
    products_list = []

    # Convert MongoDB cursor to a list of dictionaries
    for product in products:
        product['_id'] = str(product['_id'])  # Convert ObjectId to string
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

    # Validate that the quantity is provided and is a positive integer
    if 'quantity' not in data or not isinstance(data['quantity'], int) or data['quantity'] <= 0:
        return jsonify({"error": "Invalid quantity provided"}), 400

    try:
        # Fetch the existing product by its ID
        product = products_collection.find_one({"_id": ObjectId(product_id)})

        # If the product is not found, return an error message
        if not product:
            return jsonify({"error": "Product not found"}), 404

        # Use $inc to increment the quantity in the database
        result = products_collection.update_one(
            {"_id": ObjectId(product_id)},
            {"$inc": {"quantity": data['quantity']}}  # Increment quantity by the provided value
        )

        # Check if the product was updated successfully
        if result.matched_count > 0:
            return jsonify({"message": f"Product quantity increased by {data['quantity']} successfully"}), 200
        else:
            return jsonify({"error": "Failed to update product quantity"}), 400

    except Exception as e:
        return jsonify({"error": f"An error occurred: {str(e)}"}), 500



# Endpoint to create transaction
@app.route('/transaction', methods=['POST'])
def create_transaction():
    data = request.get_json()
    user = data['user']
    products = data['products']  # List of product objects
    trans_date = data['trans_date']
    trans_time = data['trans_time']

    # Calculate transTotal
    trans_total = sum([product['salePrice'] * product['quantity'] for product in products])

    transaction = {
        "user": user,
        "products": products,
        "transDate": trans_date,
        "transTime": trans_time,
        "transTotal": trans_total
    }

    # Save transaction to MongoDB (or any other DB you're using)
    db.transactions.insert_one(transaction)

    return jsonify({"message": "Transaction created successfully"}), 201


# Endpoint to add a product to the cart
@app.route('/cart/<user_id>', methods=['POST'])
def add_to_cart(user_id):
    data = request.get_json()
    product_id = data['product_id']

    user = users_collection.find_one({"_id": ObjectId(user_id)})
    if not user:
        return jsonify({"error": "User not found"}), 404

    # Add the product to the user's cart
    cart = user.get("cart", [])
    if product_id not in cart:
        cart.append(product_id)
        users_collection.update_one({"_id": ObjectId(user_id)}, {"$set": {"cart": cart}})
        return jsonify({"message": "Product added to cart"}), 200
    else:
        return jsonify({"message": "Product is already in cart"}), 400

# Endpoint to get the user's cart
@app.route('/cart/<user_id>', methods=['GET'])
def get_cart(user_id):
    user = users_collection.find_one({"_id": ObjectId(user_id)})
    if user:
        cart_product_ids = user.get("cart", [])
        products = products_collection.find({"_id": {"$in": [ObjectId(prod_id) for prod_id in cart_product_ids]}})
        product_list = [product for product in products]
        return jsonify(product_list), 200
    else:
        return jsonify({"error": "User not found"}), 404

# Endpoint to remove a product from the cart
@app.route('/cart/<user_id>/<product_id>', methods=['DELETE'])
def remove_from_cart(user_id, product_id):
    user = users_collection.find_one({"_id": ObjectId(user_id)})
    if not user:
        return jsonify({"error": "User not found"}), 404

    cart = user.get("cart", [])
    if product_id in cart:
        cart.remove(product_id)
        users_collection.update_one({"_id": ObjectId(user_id)}, {"$set": {"cart": cart}})
        return jsonify({"message": "Product removed from cart"}), 200
    else:
        return jsonify({"error": "Product not found in cart"}), 404




if __name__ == '__main__':
    # Run the application on all available IPs on port 8888
    app.run(host='0.0.0.0', port=8888)
