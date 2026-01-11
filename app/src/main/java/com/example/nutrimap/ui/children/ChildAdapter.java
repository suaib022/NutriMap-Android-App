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
import com.example.nutrimap.data.repository.LocationRepository;
import com.example.nutrimap.domain.model.Child;
import com.example.nutrimap.domain.model.District;
import com.example.nutrimap.domain.model.Division;
import com.example.nutrimap.domain.model.Union;
import com.example.nutrimap.domain.model.Upazila;
import com.example.nutrimap.domain.model.Visit;
import com.example.nutrimap.domain.util.NutritionRiskCalculator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Adapter for children RecyclerView.
 */
public class ChildAdapter extends ListAdapter<Child, ChildAdapter.ViewHolder> {

    private final OnChildActionListener listener;
    
    // Cache for location names
    private Map<String, String> divisionNames = new HashMap<>();
    private Map<String, String> districtNames = new HashMap<>();
    private Map<String, String> upazilaNames = new HashMap<>();
    private Map<String, String> unionNames = new HashMap<>();
    
    // Cache for visit data
    private Map<String, Visit> latestVisits = new HashMap<>();

    public interface OnChildActionListener {
        void onViewChild(Child child);
        void onEditChild(Child child);
        void onDeleteChild(Child child);
    }

    private static final DiffUtil.ItemCallback<Child> DIFF_CALLBACK = new DiffUtil.ItemCallback<Child>() {
        @Override
        public boolean areItemsTheSame(@NonNull Child oldItem, @NonNull Child newItem) {
            String oldId = oldItem.getDocumentId();
            String newId = newItem.getDocumentId();
            if (oldId == null || newId == null) {
                return oldItem.getId() == newItem.getId();
            }
            return oldId.equals(newId);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Child oldItem, @NonNull Child newItem) {
            return oldItem.getName().equals(newItem.getName());
        }
    };

    public ChildAdapter(OnChildActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        loadLocationNames();
    }
    
    /**
     * Load visits and cache them for display.
     */
    public void loadVisitsData(List<Visit> visits) {
        latestVisits.clear();
        for (Visit v : visits) {
            String childDocId = v.getChildDocumentId();
            if (childDocId != null) {
                Visit existing = latestVisits.get(childDocId);
                if (existing == null || v.getVisitDate().compareTo(existing.getVisitDate()) > 0) {
                    latestVisits.put(childDocId, v);
                }
            }
        }
        notifyDataSetChanged();
    }
    
    private void loadLocationNames() {
        // Load divisions
        LocationRepository.getInstance().getDivisions(new LocationRepository.DivisionCallback() {
            @Override
            public void onSuccess(List<Division> divisions) {
                for (Division d : divisions) {
                    divisionNames.put(d.getId(), d.getName());
                }
                notifyDataSetChanged();
            }
            @Override
            public void onError(String message) {}
        });
        
        // Load districts
        LocationRepository.getInstance().getDistricts(new LocationRepository.DistrictCallback() {
            @Override
            public void onSuccess(List<District> districts) {
                for (District d : districts) {
                    districtNames.put(d.getId(), d.getName());
                }
                notifyDataSetChanged();
            }
            @Override
            public void onError(String message) {}
        });
        
        // Load upazilas
        LocationRepository.getInstance().getUpazilas(new LocationRepository.UpazilaCallback() {
            @Override
            public void onSuccess(List<Upazila> upazilas) {
                for (Upazila u : upazilas) {
                    upazilaNames.put(u.getId(), u.getName());
                }
                notifyDataSetChanged();
            }
            @Override
            public void onError(String message) {}
        });
        
        // Load unions
        LocationRepository.getInstance().getUnions(new LocationRepository.UnionCallback() {
            @Override
            public void onSuccess(List<Union> unions) {
                for (Union u : unions) {
                    unionNames.put(u.getId(), u.getName());
                }
                notifyDataSetChanged();
            }
            @Override
            public void onError(String message) {}
        });
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
        Visit latestVisit = child.getDocumentId() != null ? latestVisits.get(child.getDocumentId()) : null;
        holder.bind(child, listener, divisionNames, districtNames, upazilaNames, unionNames, latestVisit);
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

        void bind(Child child, OnChildActionListener listener,
                  Map<String, String> divisionNames, Map<String, String> districtNames,
                  Map<String, String> upazilaNames, Map<String, String> unionNames,
                  Visit latestVisit) {
            
            textViewName.setText(child.getName());
            textViewAge.setText(child.getAgeString());
            textViewGender.setText(child.getGender());
            
            // Build area string: Union, Upazila, District, Division
            String area = buildAreaString(child, unionNames, upazilaNames, districtNames, divisionNames);
            textViewLocation.setText("Area: " + area);

            // Get risk level from latest visit
            String riskLevel = "N/A";
            if (latestVisit != null) {
                riskLevel = NutritionRiskCalculator.calculateRiskFromMuac(latestVisit.getMuacMm());
            }
            textViewRisk.setText(riskLevel);
            textViewRisk.setBackgroundResource(NutritionRiskCalculator.getRiskBackgroundResource(riskLevel));
            textViewRisk.setTextColor(ContextCompat.getColor(itemView.getContext(), 
                    NutritionRiskCalculator.getRiskTextColorResource(riskLevel)));

            // Display last visit with formatted date
            if (latestVisit != null) {
                String formattedDate = formatDate(latestVisit.getVisitDate());
                textViewLastVisit.setText("Last Visit: " + formattedDate);
            } else {
                textViewLastVisit.setText("No visits yet");
            }

            // Click listeners
            buttonView.setOnClickListener(v -> listener.onViewChild(child));
            buttonEdit.setOnClickListener(v -> listener.onEditChild(child));
            buttonDelete.setOnClickListener(v -> listener.onDeleteChild(child));
            itemView.setOnClickListener(v -> listener.onViewChild(child));
        }
        
        private String buildAreaString(Child child, 
                                       Map<String, String> unionNames, 
                                       Map<String, String> upazilaNames,
                                       Map<String, String> districtNames, 
                                       Map<String, String> divisionNames) {
            StringBuilder sb = new StringBuilder();
            
            // Union
            String unionName = unionNames.get(child.getUnionId());
            if (unionName != null && !unionName.isEmpty()) {
                sb.append(unionName);
            }
            
            // Upazila
            String upazilaName = upazilaNames.get(child.getUpazilaId());
            if (upazilaName != null && !upazilaName.isEmpty()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(upazilaName);
            }
            
            // District
            String districtName = districtNames.get(child.getDistrictId());
            if (districtName != null && !districtName.isEmpty()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(districtName);
            }
            
            // Division
            String divisionName = divisionNames.get(child.getDivisionId());
            if (divisionName != null && !divisionName.isEmpty()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(divisionName);
            }
            
            return sb.length() > 0 ? sb.toString() : "N/A";
        }
        
        private String formatDate(String dateStr) {
            // Input format: yyyy-MM-dd (e.g., 2024-01-19)
            // Output format: 19 January, 2024
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("d MMMM, yyyy", Locale.getDefault());
                Date date = inputFormat.parse(dateStr);
                return outputFormat.format(date);
            } catch (ParseException e) {
                return dateStr; // Return original if parsing fails
            }
        }
    }
}
