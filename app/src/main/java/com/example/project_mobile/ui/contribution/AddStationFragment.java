package com.example.project_mobile.ui.contribution;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.project_mobile.R;
import com.example.project_mobile.data.StationRepository;
import com.google.android.material.textfield.TextInputEditText;

public class AddStationFragment extends Fragment {

    private static final String ARG_LAT = "lat";
    private static final String ARG_LNG = "lng";

    public static AddStationFragment newInstance(double lat, double lng) {
        AddStationFragment fragment = new AddStationFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_LAT, lat);
        args.putDouble(ARG_LNG, lng);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_station, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        double lat = getArguments() != null ? getArguments().getDouble(ARG_LAT) : 0.0;
        double lng = getArguments() != null ? getArguments().getDouble(ARG_LNG) : 0.0;

        TextView coordsView = view.findViewById(R.id.add_station_coords);
        coordsView.setText(String.format("Lat: %.5f\nLng: %.5f", lat, lng));

        view.findViewById(R.id.add_station_toolbar).setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        Spinner speedSpinner = view.findViewById(R.id.add_station_speed_spinner);
        String[] speeds = new String[]{"Select speed...", "FAST", "SEMI-FAST", "SLOW"};
        ArrayAdapter<String> speedAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, speeds);
        speedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speedSpinner.setAdapter(speedAdapter);

        Spinner statusSpinner = view.findViewById(R.id.add_station_status_spinner);
        String[] statuses = new String[]{"Select status...", "Available", "Busy", "Unknown"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, statuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);

        TextInputEditText nameInput = view.findViewById(R.id.add_station_name_input);

        view.findViewById(R.id.btn_submit_contribution).setOnClickListener(v -> {
            String speed = speedSpinner.getSelectedItem().toString();
            String status = statusSpinner.getSelectedItem().toString();
            
            if (speedSpinner.getSelectedItemPosition() == 0 || statusSpinner.getSelectedItemPosition() == 0) {
                Toast.makeText(requireContext(), "Please select speed and status", Toast.LENGTH_SHORT).show();
                return;
            }

            String name = nameInput.getText() != null ? nameInput.getText().toString() : "";
            if (name.isEmpty()) name = "User Contributed Station";

            StationRepository repository = new StationRepository(requireActivity().getApplication());
            repository.submitContribution(lat, lng, name, speed, status, new StationRepository.Callback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(requireContext(), "Station submitted for review!", Toast.LENGTH_LONG).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(requireContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
