import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.warehousemanagementsystem.Product
import com.example.warehousemanagementsystem.R


class ProductsAdapter(
    private var productList: List<Product>,
    private val onAddToCart: (Product) -> Unit,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductsAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(R.id.productName)
        val productCategory: TextView = view.findViewById(R.id.productCategory)
        val productPrice: TextView = view.findViewById(R.id.productPrice)
        val productImage: ImageView = view.findViewById(R.id.productImage)
        val addToCartButton: ImageView = view.findViewById(R.id.addToCartButton)

        fun bind(product: Product) {
            productName.text = product.prodName
            productCategory.text = product.prodCategory ?: "Unknown"
            productPrice.text = "$${product.salePrice}"

            // Get the image URL, removing the base URL before "https://"
            val imageUrl = product.image_url?.let {
                // Find where the URL starts with "https://"
                val startIndex = it.indexOf("https://")
                if (startIndex != -1) {
                    it.substring(startIndex) // Keep everything after "https://"
                } else {
                    it // If no "https://" is found, use the original URL
                }
            }

            // Load the image URL using Glide
            Glide.with(itemView)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable disk caching temporarily
                .error(R.drawable.placeholder)
                .into(productImage)

            Log.d("ProductAdapter", "Image URL: $imageUrl")

            addToCartButton.setOnClickListener { onAddToCart(product) }
            itemView.setOnClickListener { onItemClick(product) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(productList[position])
    }

    override fun getItemCount(): Int = productList.size

    fun updateList(newList: List<Product>) {
        productList = newList
        notifyDataSetChanged()
    }
}
