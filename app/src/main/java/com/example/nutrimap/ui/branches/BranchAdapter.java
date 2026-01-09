package com.example.nutrimap.ui.branches;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutrimap.R;
import com.example.nutrimap.domain.model.Branch;

public class BranchAdapter extends ListAdapter<Branch, BranchAdapter.ViewHolder> {

    private final OnBranchClickListener listener;

    public interface OnBranchClickListener {
        void onOpenUrl(Branch branch);
    }

    private static final DiffUtil.ItemCallback<Branch> DIFF_CALLBACK = new DiffUtil.ItemCallback<Branch>() {
        @Override
        public boolean areItemsTheSame(@NonNull Branch oldItem, @NonNull Branch newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Branch oldItem, @NonNull Branch newItem) {
            return oldItem.getName().equals(newItem.getName());
        }
    };

    public BranchAdapter(OnBranchClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_branch, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewName, textViewLocation, textViewArea;
        private final ImageButton buttonOpenUrl;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewLocation = itemView.findViewById(R.id.textViewLocation);
            textViewArea = itemView.findViewById(R.id.textViewArea);
            buttonOpenUrl = itemView.findViewById(R.id.buttonOpenUrl);
        }

        void bind(Branch branch, OnBranchClickListener listener) {
            textViewName.setText(branch.getName());
            textViewLocation.setText(branch.getFullLocation());
            textViewArea.setText("Areas: " + branch.getArea());
            buttonOpenUrl.setOnClickListener(v -> listener.onOpenUrl(branch));
        }
    }
}
