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

class InventoryAdapter(
    private var productList: List<Product>,
    private val onIncrease: (Product, Int) -> Unit,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<InventoryAdapter.InventoryItemViewHolder>() {

    inner class InventoryItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(R.id.inventoryProductName)
        val productCategory: TextView = view.findViewById(R.id.inventoryProductCategory)
        val productDescription: TextView = view.findViewById(R.id.inventoryProductDescription)
        val productCostPrice: TextView = view.findViewById(R.id.inventoryProductCostPrice)
        val productSalePrice: TextView = view.findViewById(R.id.inventoryProductSalePrice)
        val productImage: ImageView = view.findViewById(R.id.inventoryProductImage)
        val btnIncreaseInventory: Button = view.findViewById(R.id.btnIncreaseInventory)
        val etIncreaseQuantity: EditText = view.findViewById(R.id.etInventoryIncrease)

        fun bind(product: Product) {
            productName.text = product.prodName + ". id: " + product._id
            productCategory.text = product.prodCategory ?: "Unknown"
            productDescription.text = product.prodDescription ?: "Unknown"
            productCostPrice.text = "$${product.costPrice}"
            productSalePrice.text = "$${product.salePrice}"
            Glide.with(itemView).load(product.image_url).into(productImage)

            btnIncreaseInventory.setOnClickListener {  val increaseQuantityText = etIncreaseQuantity.text.toString()
                if (increaseQuantityText.isNotEmpty()) {
                    val increaseAmount = increaseQuantityText.toIntOrNull()
                    if (increaseAmount != null && increaseAmount > 0) {
                        // Call onIncrease with the product and the increase amount
                        onIncrease(product, increaseAmount)
                    } else {
                        Toast.makeText(itemView.context, "Please enter a valid quantity.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(itemView.context, "Please enter a quantity.", Toast.LENGTH_SHORT).show()
                } }
            itemView.setOnClickListener { onItemClick(product) }
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