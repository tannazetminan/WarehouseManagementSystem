import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
            Glide.with(itemView).load(product.image_url).into(productImage)

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


//
//class ProductsAdapter(
//    private var productList: List<Product>,
//    private val onItemClick: (Product) -> Unit
//) : RecyclerView.Adapter<ProductsAdapter.ProductViewHolder>() {
//
//    inner class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val productName: TextView = view.findViewById(R.id.productName)
//        val productCategory: TextView = view.findViewById(R.id.productCategory)
//        val productPrice: TextView = view.findViewById(R.id.productPrice)
//        val productImage: ImageView = view.findViewById(R.id.productImage)
//
//        fun bind(product: Product) {
//            productName.text = product.prodName
//            productCategory.text = product.prodCategory ?: "Unknown"
//            productPrice.text = "$${product.salePrice}"
//
//            // Load image with Glide
//            Glide.with(itemView)
//                .load(product.image_url)  // URL from the API
//                .placeholder(R.drawable.placeholder)  // Optional placeholder image
//                .error(R.drawable.error)  // Optional error image
//                .into(productImage)
//
//            // Handle click event
//            itemView.setOnClickListener { onItemClick(product) }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
//        return ProductViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
//        holder.bind(productList[position])
//    }
//
//    override fun getItemCount(): Int = productList.size
//
//    fun updateList(newList: List<Product>) {
//        productList = newList
//        notifyDataSetChanged()
//    }
//}
