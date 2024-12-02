package com.example.warehousemanagementsystem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.util.Locale

class InventoryAdapter(
    private var productList: List<Product>,
    private val onUpdateQuantity: (Product, Int) -> Unit,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<InventoryAdapter.InventoryItemViewHolder>() {

    inner class InventoryItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(R.id.inventoryProductName)
        val productCategory: TextView = view.findViewById(R.id.inventoryProductCategory)
        val productDescription: TextView = view.findViewById(R.id.inventoryProductDescription)
        val productCostPrice: TextView = view.findViewById(R.id.inventoryProductCostPrice)
        val productSalePrice: TextView = view.findViewById(R.id.inventoryProductSalePrice)
        val productImage: ImageView = view.findViewById(R.id.inventoryProductImage)
        val tvCurrentQuantity: EditText = view.findViewById(R.id.tvCurrentQuantity) // Editable for manual entry
        val btnIncreaseInventory: Button = view.findViewById(R.id.btnIncreaseInventory)
        val btnDecreaseInventory: Button = view.findViewById(R.id.btnDecreaseInventory)
        val btnUpdateInventory: Button = view.findViewById(R.id.btnUpdateInventory)

        fun bind(product: Product) {
            val productCostPriceStr = String.format(
                Locale.US, "$ %.2f", product.costPrice)
            val productSalePriceStr = String.format(
                Locale.US, "$ %.2f", product.salePrice)
            productName.text = product.prodName + ". id: " + product._id
            productCategory.text = product.prodCategory ?: "Unknown"
            productDescription.text = product.prodDescription ?: "Unknown"
            productCostPrice.text = productCostPriceStr
            productSalePrice.text = productSalePriceStr
            tvCurrentQuantity.setText(product.quantity?.toString() ?: "0") // Editable field

            Glide.with(itemView)
                .load(product.image_url)
                .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable disk caching temporarily
                .error(R.drawable.placeholder)
                .into(productImage)

            // Increment button: Update UI but don't call API
            btnIncreaseInventory.setOnClickListener {
                val currentQuantity = tvCurrentQuantity.text.toString().toIntOrNull() ?: 0
                val newQuantity = currentQuantity + 1
                tvCurrentQuantity.setText(newQuantity.toString()) // Only update UI
            }

            // Decrement button: Update UI but don't call API
            btnDecreaseInventory.setOnClickListener {
                val currentQuantity = tvCurrentQuantity.text.toString().toIntOrNull() ?: 0
                if (currentQuantity > 0) {
                    val newQuantity = currentQuantity - 1
                    tvCurrentQuantity.setText(newQuantity.toString()) // Only update UI
                } else {
                    Toast.makeText(itemView.context, "Quantity cannot be negative.", Toast.LENGTH_SHORT).show()
                }
            }

            // Manual entry: Update UI but don't call API
            tvCurrentQuantity.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) { // When the field loses focus, validate and update
                    val newQuantity = tvCurrentQuantity.text.toString().toIntOrNull()
                    if (newQuantity != null && newQuantity >= 0) {
                        tvCurrentQuantity.setText(newQuantity.toString()) // Only update UI
                    } else {
                        tvCurrentQuantity.setText(product.quantity?.toString() ?: "0")
                        Toast.makeText(itemView.context, "Invalid quantity value.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Update button
            btnUpdateInventory.setOnClickListener {
                val updatedQuantity = tvCurrentQuantity.text.toString().toIntOrNull()
                if (updatedQuantity != null && updatedQuantity >= 0) {
                    updateQuantity(product, updatedQuantity)
                } else {
                    Toast.makeText(itemView.context, "Invalid quantity value.", Toast.LENGTH_SHORT).show()
                }
            }

            itemView.setOnClickListener { onItemClick(product) }
        }

        private fun updateQuantity(product: Product, newQuantity: Int) {
            onUpdateQuantity(product, newQuantity) // Notify the parent activity or fragment
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryItemViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_inventory, parent, false)
        return InventoryItemViewHolder(view)
    }

    override fun getItemCount() = productList.size

    override fun onBindViewHolder(holder: InventoryItemViewHolder, position: Int) {
        holder.bind(productList[position])
    }

    fun updateList(newList: List<Product>) {
        productList = newList
        notifyDataSetChanged()
    }
}