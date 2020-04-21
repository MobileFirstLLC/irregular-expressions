package mf.asciitext

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mf.asciitext.FontPickerAdapter.ItemViewHolder
import mf.asciitext.fonts.AppFont

class FontPickerAdapter(
    private val dataSource: List<AppFont>?,
    private val mItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<ItemViewHolder>() {
    private val activeItemType = 1
    private var selectedFont = 0

    interface OnItemClickListener {
        fun onItemClick(item: AppFont?, index: Int)
    }

    fun setSelectedFont(index: Int) {
        selectedFont = index
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutRes =
            if (viewType == activeItemType) R.layout.item_font_selected else R.layout.item_font
        return ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        )
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == selectedFont && selectedFont >= 0) activeItemType else -1
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val value = dataSource!![position]
        holder.mTextView.text = value.styledName
    }

    override fun getItemCount(): Int {
        return dataSource?.size ?: 0
    }

    inner class ItemViewHolder internal constructor(v: View) :
        RecyclerView.ViewHolder(v), View.OnClickListener {
        val mTextView: TextView = v.findViewById(R.id.text_value)
        override fun onClick(v: View) {
            try {
                val index = adapterPosition
                val item = dataSource!![index]
                mItemClickListener.onItemClick(item, index)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        init {
            val container =
                v.findViewById<View>(R.id.text_container)

            container.setOnClickListener(this)
        }
    }
}