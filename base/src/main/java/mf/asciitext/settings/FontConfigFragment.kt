package mf.asciitext.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import mf.asciitext.R
import mf.asciitext.fonts.AvailableFonts.getFonts
import mf.asciitext.fonts.AvailableFonts.setNewPosition
import mf.asciitext.fonts.AvailableFonts.toggleEnabled

class FontConfigFragment : Fragment() {
    private val EXTRA_RVSTATE = "recyclerview_state"
    private var adapter: FontConfigAdapter? = null
    private var mRecyclerView: RecyclerView? = null
    private fun restoreRecyclerViewState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null && mRecyclerView != null && savedInstanceState.containsKey(
                EXTRA_RVSTATE
            )
        ) mRecyclerView!!.scrollToPosition(savedInstanceState.getInt(EXTRA_RVSTATE))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        var scrollPosition = 0
        if (mRecyclerView!!.layoutManager != null) {
            val lm =
                mRecyclerView!!.layoutManager as LinearLayoutManager?
            scrollPosition = lm!!.findFirstCompletelyVisibleItemPosition()
        }
        outState.putInt(EXTRA_RVSTATE, scrollPosition)
        super.onSaveInstanceState(outState)
    }

    private fun columnCount(): Int {
        return resources.getInteger(R.integer.font_config_column_count)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        restoreRecyclerViewState(savedInstanceState)
        val ctx: Context? = activity
        val fonts = getFonts()
        mRecyclerView = RecyclerView(ctx!!)
        adapter = FontConfigAdapter(fonts, onClick())
        val layoutManager = GridLayoutManager(ctx, columnCount())
        mRecyclerView!!.layoutManager = layoutManager
        mRecyclerView!!.adapter = adapter
        mRecyclerView!!.layoutParams = RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.MATCH_PARENT
        )
        val callback: ItemTouchHelper.Callback = SimpleItemTouchHelperCallback(adapter!!)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(mRecyclerView)
        mRecyclerView!!.addItemDecoration(DividerItemDecoration(ctx, layoutManager.orientation))
        val BgColor =
            ContextCompat.getColor(mRecyclerView!!.context, R.color.settings_background)
        mRecyclerView!!.setBackgroundColor(BgColor)
        return mRecyclerView
    }

    private fun onClick(): FontConfigAdapter.OnItemClickListener {
        return object : FontConfigAdapter.OnItemClickListener {
            override fun onDragComplete(fontId: String, position: Int) {
                setNewPosition(fontId, position)
            }

            override fun onReorderClick(fontId: String, position: Int) {
                setNewPosition(fontId, position)
                adapter!!.updateFonts(getFonts())
            }

            override fun onEnableClick(fontId: String) {
                toggleEnabled(fontId)
            }
        }
    }

    internal class SimpleItemTouchHelperCallback(private val adapter: FontConfigAdapter) :
        ItemTouchHelper.Callback() {
        override fun isLongPressDragEnabled(): Boolean {
            return true
        }

        override fun isItemViewSwipeEnabled(): Boolean {
            return false
        }

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
            return makeMovementFlags(
                dragFlags,
                swipeFlags
            )
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            adapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(
            viewHolder: RecyclerView.ViewHolder,
            direction: Int
        ) {
            adapter.onItemDismiss(viewHolder.adapterPosition)
        }

    }
}