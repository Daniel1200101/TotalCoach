package com.example.totalcoach.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.totalcoach.R
import com.example.totalcoach.models.PaymentRecord
import com.google.android.material.chip.Chip

class PaymentAdapter(
    private val context: Context,
    private val paymentList: List<PaymentRecord>
) : RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.payment_item, parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val payment = paymentList[position]

        holder.clientNameTextView.text = payment.clientName
        holder.trainingTypeTextView.text = payment.trainingType
        holder.dateTextView.text = payment.date
        holder.amountTextView.text = String.format("$%.2f", payment.amount)

        // Set the status chip
        holder.statusChip.text = payment.status

        // Set chip color based on status
        val backgroundColor = when (payment.status.lowercase()) {
            "paid" -> R.color.status_paid
            "pending" -> R.color.status_pending
            "overdue" -> R.color.status_overdue
            else -> R.color.status_pending
        }

        holder.statusChip.chipBackgroundColor = ContextCompat.getColorStateList(context, backgroundColor)
    }

    override fun getItemCount(): Int = paymentList.size

    class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val clientNameTextView: TextView = itemView.findViewById(R.id.clientNameTextView)
        val trainingTypeTextView: TextView = itemView.findViewById(R.id.trainingTypeTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val amountTextView: TextView = itemView.findViewById(R.id.amountTextView)
        val statusChip: Chip = itemView.findViewById(R.id.statusChip)
    }
}