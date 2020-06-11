package mf.asciitext.settings

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import mf.asciitext.R
import mf.asciitext.fonts.AppFont

class FontConfigAdapter(
    private var dataSource: List<AppFont>?,
    private val mItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<FontConfigAdapter.ItemViewHolder>() {

    interface OnItemClickListener {
        fun onEnableClick(index: Int)
        fun onReorderClick(index: Int)
    }

    fun updateFonts(newDataSource: List<AppFont>?){
        dataSource = newDataSource
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_config_font, parent, false)
        )
    }

    override fun getItemViewType(position: Int): Int {
        return -1
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val value = dataSource!![position]
        holder.mTextView.text = value.styledName
        holder.mCheckbox.isChecked = value.isEnabled
        holder.mTextView.alpha = if (value.isEnabled) 1f else 0.5f
        holder.mButton.visibility = if (position == 0 || !value.isEnabled) GONE else VISIBLE
    }

    override fun getItemCount(): Int {
        return dataSource?.size ?: 0
    }

    inner class ItemViewHolder internal constructor(v: View) : RecyclerView.ViewHolder(v),
        View.OnClickListener {
        val mTextView: TextView = v.findViewById(R.id.text_value)
        val mCheckbox: Switch = v.findViewById(R.id.enable_box)
        val mButton: MaterialButton = v.findViewById(R.id.order_btn)

        override fun onClick(v: View) {
            try {
                val index = adapterPosition
                if (v.id == R.id.enable_box) {
                    mItemClickListener.onEnableClick(index)
                }
                if (v.id == R.id.order_btn) {
                    mItemClickListener.onReorderClick(index)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        init {
            mCheckbox.setOnClickListener(this)
            mButton.setOnClickListener(this)
        }
    }
}