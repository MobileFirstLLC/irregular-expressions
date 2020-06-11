package mf.asciitext.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
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
        mRecyclerView.addItemDecoration(new DividerItemDecoration(ctx, layoutManager.getOrientation()));
        return mRecyclerView;
    }

    private FontConfigAdapter.OnItemClickListener onClick() {
        return new FontConfigAdapter.OnItemClickListener() {
            @Override
            public void onReorderClick(int index) {
                AvailableFonts.INSTANCE.setFirst(index);
                adapter.updateFonts(AvailableFonts.INSTANCE.getFonts());
            }

            @Override
            public void onEnableClick(int index) {
                AvailableFonts.INSTANCE.toggleEnabled(index);
                adapter.notifyItemChanged(index);
            }
        };
    }
}
