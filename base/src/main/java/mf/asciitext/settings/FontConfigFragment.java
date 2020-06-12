package mf.asciitext.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import mf.asciitext.R;
import mf.asciitext.fonts.AppFont;
import mf.asciitext.fonts.AvailableFonts;

public class FontConfigFragment extends Fragment {

    private final String EXTRA_RVSTATE = "recyclerview_state";
    private FontConfigAdapter adapter;
    private RecyclerView mRecyclerView;

    private void restoreRecyclerViewState(Bundle savedInstanceState) {
        if (savedInstanceState != null && mRecyclerView != null && savedInstanceState.containsKey(EXTRA_RVSTATE))
            mRecyclerView.scrollToPosition(savedInstanceState.getInt(EXTRA_RVSTATE));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        int scrollPosition = 0;
        if (mRecyclerView.getLayoutManager() != null) {
            LinearLayoutManager lm =
                    ((LinearLayoutManager) mRecyclerView.getLayoutManager());
            scrollPosition = lm.findFirstCompletelyVisibleItemPosition();
        }
        outState.putInt(EXTRA_RVSTATE, scrollPosition);
        super.onSaveInstanceState(outState);
    }

    private int columnCount() {
        return getResources().getInteger(R.integer.font_config_column_count);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        restoreRecyclerViewState(savedInstanceState);
        Context ctx = getActivity();
        List<AppFont> fonts = AvailableFonts.INSTANCE.getFonts();
        mRecyclerView = new RecyclerView(ctx);
        adapter = new FontConfigAdapter(fonts, onClick());
        GridLayoutManager layoutManager = new GridLayoutManager(ctx, columnCount());

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.MATCH_PARENT));

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerView);

        mRecyclerView.addItemDecoration(new DividerItemDecoration(ctx, layoutManager.getOrientation()));
        int BgColor = ContextCompat.getColor(mRecyclerView.getContext(), R.color.settings_background);
        mRecyclerView.setBackgroundColor(BgColor);
        return mRecyclerView;
    }

    private FontConfigAdapter.OnItemClickListener onClick() {
        return new FontConfigAdapter.OnItemClickListener() {
            @Override
            public void onDragComplete(String fontId, int position) {
                AvailableFonts.INSTANCE.setNewPosition(fontId, position);
            }

            @Override
            public void onReorderClick(String fontId, int position) {
                AvailableFonts.INSTANCE.setNewPosition(fontId, position);
                adapter.updateFonts(AvailableFonts.INSTANCE.getFonts());
            }

            @Override
            public void onEnableClick(String fontId) {
                AvailableFonts.INSTANCE.toggleEnabled(fontId);
            }
        };
    }

    static class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {

        private final FontConfigAdapter adapter;

        SimpleItemTouchHelperCallback(FontConfigAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return false;
        }

        @Override
        public int getMovementFlags(
                @NonNull RecyclerView recyclerView,
                @NonNull RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(
                @NonNull RecyclerView recyclerView,
                @NonNull RecyclerView.ViewHolder viewHolder,
                @NonNull RecyclerView.ViewHolder target) {
            adapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            adapter.onItemDismiss(viewHolder.getAdapterPosition());
        }

    }
}
