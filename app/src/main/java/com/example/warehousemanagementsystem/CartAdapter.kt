

package com.example.warehousemanagementsystem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CartAdapter(
    private var cartItems: List<CartItem>,
    private val onRemoveFromCart: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(R.id.cartProductName)
        val productPrice: TextView = view.findViewById(R.id.cartProductPrice)
        val productQuantity: TextView = view.findViewById(R.id.cartProductQuantity)
        val productImage: ImageView = view.findViewById(R.id.cartProductImage)
        val removeButton: ImageView = view.findViewById(R.id.removeFromCartButton)

        fun bind(cartItem: CartItem) {
            val product = cartItem.product

            // Directly access the product object
            productName.text = product.prodName ?: "Unknown Product"
            productPrice.text = "$${product.salePrice}"
            productQuantity.text = "Qty: ${cartItem.quantity}"

            // Load product image using Glide
            Glide.with(itemView)
                .load(product.image_url ?: R.drawable.placeholder)  // Use placeholder image if image_url is null
                .into(productImage)

            // Remove item from cart
            removeButton.setOnClickListener { onRemoveFromCart(cartItem) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateCartItems(newItems: List<CartItem>) {
        cartItems = newItems
        notifyDataSetChanged()
    }
}
