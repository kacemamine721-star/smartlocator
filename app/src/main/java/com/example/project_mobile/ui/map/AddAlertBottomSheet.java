package com.example.project_mobile.ui.map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.project_mobile.R;
import com.example.project_mobile.data.remote.AlertRequest;
import com.example.project_mobile.data.remote.RetrofitClient;
import com.example.project_mobile.ui.contribution.MapPickerActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddAlertBottomSheet extends BottomSheetDialogFragment {

    private TextView locationDisplay;
    private double selectedLat = 0;
    private double selectedLng = 0;

    private final ActivityResultLauncher<Intent> mapPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedLat = result.getData().getDoubleExtra(MapPickerActivity.EXTRA_LAT, 0);
                    selectedLng = result.getData().getDoubleExtra(MapPickerActivity.EXTRA_LNG, 0);
                    if (locationDisplay != null) {
                        locationDisplay.setText(String.format(java.util.Locale.US, "Lat: %.5f\nLng: %.5f", selectedLat, selectedLng));
                        locationDisplay.setTextColor(requireContext().getColor(R.color.white));
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_add_alert_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Spinner typeSpinner = view.findViewById(R.id.alert_type_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.alert_types, R.layout.spinner_item_white);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_white);
        typeSpinner.setAdapter(adapter);

        locationDisplay = view.findViewById(R.id.alert_location_display);
        view.findViewById(R.id.btn_select_alert_location).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), MapPickerActivity.class);
            mapPickerLauncher.launch(intent);
        });

        view.findViewById(R.id.btn_submit_alert).setOnClickListener(v -> {
            if (selectedLat == 0 && selectedLng == 0) {
                Toast.makeText(requireContext(), "Please select a location first", Toast.LENGTH_SHORT).show();
                return;
            }

            String type = typeSpinner.getSelectedItem().toString();
            String desc = ((com.google.android.material.textfield.TextInputEditText) view.findViewById(R.id.alert_description_input)).getText().toString();

            AlertRequest request = new AlertRequest(type, desc, selectedLat, selectedLng);
            RetrofitClient.getApiService(requireContext()).submitAlert(request).enqueue(new retrofit2.Callback<Void>() {
                @Override
                public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), R.string.msg_alert_reported, Toast.LENGTH_SHORT).show();
                        dismiss();
                    } else {
                        Toast.makeText(requireContext(), "Failed to report alert", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                    Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
