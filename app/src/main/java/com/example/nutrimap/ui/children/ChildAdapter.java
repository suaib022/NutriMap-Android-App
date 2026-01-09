package com.example.nutrimap.ui.children;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutrimap.R;
import com.example.nutrimap.data.repository.ChildRepository;
import com.example.nutrimap.data.repository.VisitRepository;
import com.example.nutrimap.domain.model.Child;
import com.example.nutrimap.domain.model.Visit;
import com.example.nutrimap.domain.util.NutritionRiskCalculator;

/**
 * Adapter for children RecyclerView.
 */
public class ChildAdapter extends ListAdapter<Child, ChildAdapter.ViewHolder> {

    private final OnChildActionListener listener;

    public interface OnChildActionListener {
        void onViewChild(Child child);
        void onEditChild(Child child);
        void onDeleteChild(Child child);
    }

    private static final DiffUtil.ItemCallback<Child> DIFF_CALLBACK = new DiffUtil.ItemCallback<Child>() {
        @Override
        public boolean areItemsTheSame(@NonNull Child oldItem, @NonNull Child newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Child oldItem, @NonNull Child newItem) {
            return oldItem.getName().equals(newItem.getName());
        }
    };

    public ChildAdapter(OnChildActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_child, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Child child = getItem(position);
        holder.bind(child, listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewName;
        private final TextView textViewRisk;
        private final TextView textViewAge;
        private final TextView textViewGender;
        private final TextView textViewLocation;
        private final TextView textViewLastVisit;
        private final ImageButton buttonView;
        private final ImageButton buttonEdit;
        private final ImageButton buttonDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewRisk = itemView.findViewById(R.id.textViewRisk);
            textViewAge = itemView.findViewById(R.id.textViewAge);
            textViewGender = itemView.findViewById(R.id.textViewGender);
            textViewLocation = itemView.findViewById(R.id.textViewLocation);
            textViewLastVisit = itemView.findViewById(R.id.textViewLastVisit);
            buttonView = itemView.findViewById(R.id.buttonView);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }

        void bind(Child child, OnChildActionListener listener) {
            textViewName.setText(child.getName());
            textViewAge.setText(child.getAgeString());
            textViewGender.setText(child.getGender());
            textViewLocation.setText("Division " + child.getDivisionId() + ", District " + child.getDistrictId());

            // Get risk level
            String riskLevel = ChildRepository.getInstance().getRiskLevelForChild(child.getId());
            textViewRisk.setText(riskLevel);
            textViewRisk.setBackgroundResource(NutritionRiskCalculator.getRiskBackgroundResource(riskLevel));
            textViewRisk.setTextColor(ContextCompat.getColor(itemView.getContext(), 
                    NutritionRiskCalculator.getRiskTextColorResource(riskLevel)));

            // Get last visit
            Visit lastVisit = VisitRepository.getInstance().getLatestVisitForChild(child.getId());
            if (lastVisit != null) {
                textViewLastVisit.setText("Last visit: " + lastVisit.getVisitDate());
            } else {
                textViewLastVisit.setText("No visits yet");
            }

            // Click listeners
            buttonView.setOnClickListener(v -> listener.onViewChild(child));
            buttonEdit.setOnClickListener(v -> listener.onEditChild(child));
            buttonDelete.setOnClickListener(v -> listener.onDeleteChild(child));
            itemView.setOnClickListener(v -> listener.onViewChild(child));
        }
    }
}
