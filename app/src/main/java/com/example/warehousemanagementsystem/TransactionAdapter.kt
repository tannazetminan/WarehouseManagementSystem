package com.example.warehousemanagementsystem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale


class TransactionAdapter(
    private var transactionList: List<Transaction>,
    private val onItemClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val transactionId: TextView = view.findViewById(R.id.item_transactionId)
        val transactionDate: TextView = view.findViewById(R.id.item_transactionDate)
        val transactionTotal: TextView = view.findViewById(R.id.item_transactionTotal)

        fun bind(transaction: Transaction) {
            // Format the transaction date to a more readable format
            val date = formatTransactionDate(transaction.transDate)
            transactionId.text = "Transaction ID: ${transaction.transId}"
            transactionDate.text = "Date: $date"
            transactionTotal.text = "Total: $${transaction.calculateTransTotal()}" //updated to call the function, not property

            itemView.setOnClickListener { onItemClick(transaction) }
        }

        // Helper function to format the transaction date
        private fun formatTransactionDate(dateString: String): String {
            return try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = dateFormat.parse(dateString)
                val formattedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                date?.let { formattedDate.format(it) } ?: "Unknown Date"
            } catch (e: Exception) {
                "Invalid Date"
            }
        }
    }

    // Create a new ViewHolder instance for each transaction item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    // Bind data for the transaction at the specified position
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactionList[position])
    }

    // Return the total number of items in the transaction list
    override fun getItemCount(): Int = transactionList.size

    // Function to update the transaction list when new data is available
    fun updateList(newList: List<Transaction>) {
        transactionList = newList
        notifyDataSetChanged() // Notify the adapter that the data has changed
    }
}