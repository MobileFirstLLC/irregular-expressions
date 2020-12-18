package mf.irregex.settings

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import mf.irregex.R
import mf.irregex.styles.AvailableStyles.getStyles
import mf.irregex.styles.AvailableStyles.setNewPosition
import mf.irregex.styles.AvailableStyles.toggleEnabled


class StyleConfigFragment : Fragment() {

    private val EXTRA_RVSTATE = "recyclerview_state"
    private var adapter: StyleConfigAdapter? = null
    private var mRecyclerView: RecyclerView? = null

    private fun restoreRecyclerViewState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null && mRecyclerView != null &&
            savedInstanceState.containsKey(EXTRA_RVSTATE)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    private fun columnCount(): Int {
        return resources.getInteger(R.integer.style_config_column_count)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_style_config, container, false)
        val ctx: Context? = activity
        val styles = getStyles()
        mRecyclerView = view.findViewById(R.id.recyclerView)
        adapter = StyleConfigAdapter(styles, onClick())
        val layoutManager = GridLayoutManager(ctx, columnCount())
        mRecyclerView!!.layoutManager = layoutManager
        mRecyclerView!!.adapter = adapter
        val callback: ItemTouchHelper.Callback = SimpleItemTouchHelperCallback(adapter!!)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(mRecyclerView)
        restoreRecyclerViewState(savedInstanceState)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.config, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity?.finish()
                return true
            }
            R.id.help -> HelpDialogFragment().show(
                requireActivity().supportFragmentManager, "help"
            )
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onClick(): StyleConfigAdapter.OnItemClickListener {
        return object : StyleConfigAdapter.OnItemClickListener {
            override fun onDragComplete(id: String, position: Int) {
                setNewPosition(id, position)
            }

            override fun onReorderClick(id: String, position: Int) {
                setNewPosition(id, position)
                adapter!!.updateFonts(getStyles())
            }

            override fun onEnableClick(id: String) {
                toggleEnabled(id)
            }
        }
    }

    internal class SimpleItemTouchHelperCallback(private val adapter: StyleConfigAdapter) :
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

    class HelpDialogFragment : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
            builder.setMessage(R.string.config_instructions)
            builder.setTitle(R.string.configure_styles_help)
            builder.setPositiveButton(
                R.string.ok
            ) { dialog, _ -> dialog.dismiss() }
            return builder.create()
        }
    }
}