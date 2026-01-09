package com.example.nutrimap.ui.children;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.nutrimap.R;
import com.example.nutrimap.data.repository.ChildRepository;
import com.example.nutrimap.databinding.FragmentChildrenBinding;
import com.example.nutrimap.domain.model.Child;

import java.util.List;

/**
 * Fragment displaying list of children with search and CRUD operations.
 */
public class ChildrenFragment extends Fragment implements ChildAdapter.OnChildActionListener {

    private FragmentChildrenBinding binding;
    private ChildAdapter adapter;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChildrenBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        setupRecyclerView();
        setupSearch();
        setupButtons();
        loadChildren();
    }

    private void setupRecyclerView() {
        adapter = new ChildAdapter(this);
        binding.recyclerViewChildren.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewChildren.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterChildren(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupButtons() {
        binding.fabAddChild.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("childId", -1);
            navController.navigate(R.id.createChildFragment, args);
        });

        binding.buttonExportCsv.setOnClickListener(v -> 
            Toast.makeText(requireContext(), R.string.export_not_implemented, Toast.LENGTH_SHORT).show());

        binding.buttonExportPdf.setOnClickListener(v -> 
            Toast.makeText(requireContext(), R.string.export_not_implemented, Toast.LENGTH_SHORT).show());
    }

    private void loadChildren() {
        List<Child> children = ChildRepository.getInstance().getAllChildren();
        updateList(children);
    }

    private void filterChildren(String query) {
        List<Child> filtered = ChildRepository.getInstance().searchChildren(query);
        updateList(filtered);
    }

    private void updateList(List<Child> children) {
        adapter.submitList(children);
        binding.textViewEmpty.setVisibility(children.isEmpty() ? View.VISIBLE : View.GONE);
        binding.recyclerViewChildren.setVisibility(children.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onViewChild(Child child) {
        Bundle args = new Bundle();
        args.putInt("childId", child.getId());
        navController.navigate(R.id.childProfileFragment, args);
    }

    @Override
    public void onEditChild(Child child) {
        Bundle args = new Bundle();
        args.putInt("childId", child.getId());
        navController.navigate(R.id.createChildFragment, args);
    }

    @Override
    public void onDeleteChild(Child child) {
        ChildRepository.getInstance().deleteChild(child.getId());
        loadChildren();
        Toast.makeText(requireContext(), R.string.success_deleted, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadChildren();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
