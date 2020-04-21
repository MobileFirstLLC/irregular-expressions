package mf.asciitext;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import mf.asciitext.fonts.AppFont;

public class FontPickerAdapter extends RecyclerView.Adapter<FontPickerAdapter.ItemViewHolder> {

    private final List<AppFont> dataSource;

    private final OnItemClickListener mItemClickListener;

    private final int activeItemType = 1;

    private int selectedFont;

    interface OnItemClickListener {
        void onItemClick(AppFont item, int index);
    }

    public FontPickerAdapter(List<AppFont> data, OnItemClickListener onItemClick) {
        dataSource = data;
        mItemClickListener = onItemClick;
    }

    public void setSelectedFont(int index) {
        selectedFont = index;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes = (viewType ==  activeItemType ? R.layout.item_font_selected : R.layout.item_font);
        return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return (position == selectedFont && selectedFont>=0) ? activeItemType : -1;
    }

    @Override
    public void onBindViewHolder(@NonNull final FontPickerAdapter.ItemViewHolder holder, int position) {
        final AppFont value = dataSource.get(position);
        holder.mTextView.setText(value.GetStyledName());
    }

    @Override
    public int getItemCount() {
        return dataSource == null ? 0 : dataSource.size();
    }

    @SuppressWarnings("unused")
    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView mTextView;

        ItemViewHolder(View v) {
            super(v);
            mTextView = v.findViewById(R.id.text_value);
            View container = v.findViewById(R.id.text_container);

            /* when user clicks on a recyclerView item */
            container.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            try {
                final int index = getAdapterPosition();
                AppFont item = dataSource.get(index);
                mItemClickListener.onItemClick(item, index);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}