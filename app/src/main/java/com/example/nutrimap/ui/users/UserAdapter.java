package com.example.nutrimap.ui.users;

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
import com.example.nutrimap.domain.model.User;

public class UserAdapter extends ListAdapter<User, UserAdapter.ViewHolder> {

    private final OnUserActionListener listener;

    public interface OnUserActionListener {
        void onViewUser(User user);
    }

    private static final DiffUtil.ItemCallback<User> DIFF_CALLBACK = new DiffUtil.ItemCallback<User>() {
        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getEmail().equals(newItem.getEmail());
        }
    };

    public UserAdapter(OnUserActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewName, textViewEmail, textViewRole;
        private final ImageButton buttonView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewEmail = itemView.findViewById(R.id.textViewEmail);
            textViewRole = itemView.findViewById(R.id.textViewRole);
            buttonView = itemView.findViewById(R.id.buttonView);
        }

        void bind(User user, OnUserActionListener listener) {
            textViewName.setText(user.getName());
            textViewEmail.setText(user.getEmail());
            textViewRole.setText(user.getRole());

            buttonView.setOnClickListener(v -> listener.onViewUser(user));
            itemView.setOnClickListener(v -> listener.onViewUser(user));
        }
    }
}
