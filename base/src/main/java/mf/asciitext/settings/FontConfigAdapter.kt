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
import java.util.*

class FontConfigAdapter(
    private var dataSource: List<AppFont>?,
    private val mItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<FontConfigAdapter.ItemViewHolder>() {

    interface OnItemClickListener {
        fun onEnableClick(fontId: String)
        fun onReorderClick(fontId: String, position: Int)
        fun onDragComplete(fontId: String, position: Int)
    }

    fun updateFonts(newDataSource: List<AppFont>?) {
        dataSource = newDataSource
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_config_font, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val value = dataSource!![position]
        holder.mTextView.text = value.styledName
        holder.mCheckbox.isChecked = value.isEnabled
        holder.mTextView.alpha = if (value.isEnabled) 1f else 0.5f
        holder.mButtonDown.visibility = if (value.isEnabled) VISIBLE else GONE
        holder.mButtonUp.visibility = if (value.isEnabled) VISIBLE else GONE
    }

    override fun getItemCount(): Int {
        return dataSource?.size ?: 0
    }

    inner class ItemViewHolder internal constructor(v: View) : RecyclerView.ViewHolder(v),
        View.OnClickListener {
        val mTextView: TextView = v.findViewById(R.id.text_value)
        val mCheckbox: Switch = v.findViewById(R.id.enable_box)
        val mButtonUp: MaterialButton = v.findViewById(R.id.order_btn_up)
        val mButtonDown: MaterialButton = v.findViewById(R.id.order_btn_down)

        override fun onClick(v: View) {
            try {
                val index = adapterPosition
                val style = dataSource?.get(index) ?: return

                if (v.id == R.id.enable_box) {
                    mItemClickListener.onEnableClick(style.fontId)
                    notifyItemChanged(index)
                }
                if (v.id == R.id.order_btn_up) {
                    mItemClickListener.onReorderClick(style.fontId, 0)
                }
                if (v.id == R.id.order_btn_down) {
                    mItemClickListener.onReorderClick(style.fontId, itemCount - 1)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        init {
            mCheckbox.setOnClickListener(this)
            mButtonUp.setOnClickListener(this)
            mButtonDown.setOnClickListener(this)
        }
    }

    fun onItemDismiss(position: Int) {
        // mItems.remove(position);
        // notifyItemRemoved(position);
    }

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        val style = dataSource?.get(fromPosition) ?: return
        mItemClickListener.onDragComplete(style.fontId, toPosition)

        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(dataSource, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(dataSource, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }
}