package com.example.propertymanager.ui.mainPage.properties.yourProperties.add.breakdown

import android.app.DatePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.propertymanager.data.model.RentBreakdown
import com.example.propertymanager.databinding.ItemMonthlyRentBreakdownBinding
import java.security.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RentBreakdownAdapter(
    private val context: Context,
    private val readOnly: Boolean = false
) : ListAdapter<RentBreakdown, RentBreakdownAdapter.RentViewHolder>(DiffCallback()) {

    inner class RentViewHolder(val binding: ItemMonthlyRentBreakdownBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RentViewHolder {
        val binding = ItemMonthlyRentBreakdownBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RentViewHolder, position: Int) {
        val item = getItem(position)
        val parsedDate = dateFormat.parse(item.dueDate)
        val calendar = Calendar.getInstance().apply { time = parsedDate ?: Date() }

        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        val monthYearText = SimpleDateFormat("MMMM - yyyy", Locale.getDefault()).format(calendar.time)

        holder.binding.tvMonthYear.text = monthYearText
        holder.binding.etRentDate.setText(item.dueDate)
        holder.binding.etRentAmount.setText(item.amount.toString())

        if(readOnly){
            holder.binding.etRentDate.isFocusable = false
            holder.binding.etRentDate.isClickable = false
            holder.binding.etRentAmount.isEnabled = false
        }
        else{
            holder.binding.etRentDate.setOnClickListener {
                val picker = DatePickerDialog(context, { _, y, m, d ->
                    if (y == year && m == month) {
                        val newDate = Calendar.getInstance().apply { set(y, m, d) }.time
                        item.dueDate = dateFormat.format(newDate)
                        holder.binding.etRentDate.setText(item.dueDate)
                    } else {
                        Toast.makeText(
                            context,
                            "Pick a date within $monthYearText",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }, year, month, calendar.get(Calendar.DAY_OF_MONTH))
                picker.show()
            }


            holder.binding.etRentAmount.doOnTextChanged { text, _, _, _ ->
                item.amount = text.toString().toDoubleOrNull() ?: 0.0
            }
        }


    }

    fun getUpdatedList(): List<RentBreakdown> = currentList.toList()

    class DiffCallback : DiffUtil.ItemCallback<RentBreakdown>() {
        override fun areItemsTheSame(oldItem: RentBreakdown, newItem: RentBreakdown): Boolean {
            return oldItem.dueDate == newItem.dueDate
        }

        override fun areContentsTheSame(oldItem: RentBreakdown, newItem: RentBreakdown): Boolean {
            return oldItem == newItem
        }
    }
}

