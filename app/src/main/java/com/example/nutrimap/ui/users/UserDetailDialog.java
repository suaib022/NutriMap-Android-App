package com.example.nutrimap.ui.users;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.nutrimap.R;
import com.example.nutrimap.data.repository.BranchRepository;
import com.example.nutrimap.domain.model.Branch;
import com.example.nutrimap.domain.model.User;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * Dialog to display user details.
 */
public class UserDetailDialog extends DialogFragment {

    private static final String ARG_USER_ID = "user_id";
    private static final String ARG_USER_NAME = "user_name";
    private static final String ARG_USER_EMAIL = "user_email";
    private static final String ARG_USER_ROLE = "user_role";
    private static final String ARG_USER_BRANCH_ID = "user_branch_id";

    public static UserDetailDialog newInstance(User user) {
        UserDetailDialog dialog = new UserDetailDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_USER_ID, user.getId());
        args.putString(ARG_USER_NAME, user.getName());
        args.putString(ARG_USER_EMAIL, user.getEmail());
        args.putString(ARG_USER_ROLE, user.getRole());
        args.putString(ARG_USER_BRANCH_ID, user.getBranchId());
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_user_detail, null);

        Bundle args = getArguments();
        if (args != null) {
            TextView textViewName = view.findViewById(R.id.textViewName);
            TextView textViewEmail = view.findViewById(R.id.textViewEmail);
            TextView textViewRole = view.findViewById(R.id.textViewRole);
            TextView textViewBranch = view.findViewById(R.id.textViewBranch);
            MaterialButton buttonClose = view.findViewById(R.id.buttonClose);

            textViewName.setText(args.getString(ARG_USER_NAME, ""));
            textViewEmail.setText(args.getString(ARG_USER_EMAIL, ""));
            textViewRole.setText(args.getString(ARG_USER_ROLE, ""));

            // Load branch name
            String branchId = args.getString(ARG_USER_BRANCH_ID);
            if (branchId != null && !branchId.isEmpty()) {
                textViewBranch.setText("Loading...");
                BranchRepository.getInstance().getBranches(new BranchRepository.BranchCallback() {
                    @Override
                    public void onSuccess(List<Branch> branches) {
                        for (Branch b : branches) {
                            if (b.getId().equals(branchId)) {
                                textViewBranch.setText(b.getName());
                                return;
                            }
                        }
                        textViewBranch.setText("N/A");
                    }

                    @Override
                    public void onError(String message) {
                        textViewBranch.setText("N/A");
                    }
                });
            } else {
                textViewBranch.setText("N/A");
            }

            buttonClose.setOnClickListener(v -> dismiss());
        }

        return new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();
    }
}
