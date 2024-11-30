

package com.example.warehousemanagementsystem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CartAdapter(
    private var cartItems: List<Product>,
    private val onRemoveFromCart: (Product) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(R.id.cartProductName)
        val productPrice: TextView = view.findViewById(R.id.cartProductPrice)
        val productImage: ImageView = view.findViewById(R.id.cartProductImage)
        val removeButton: ImageView = view.findViewById(R.id.removeFromCartButton)

        fun bind(product: Product) {
            productName.text = product.prodName
            productPrice.text = "$${product.salePrice}"
            Glide.with(itemView)
                .load(product.image_url)
                .into(productImage)

            removeButton.setOnClickListener { onRemoveFromCart(product) }
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

    fun updateCartItems(newItems: List<Product>) {
        cartItems = newItems
        notifyDataSetChanged()
    }
}
