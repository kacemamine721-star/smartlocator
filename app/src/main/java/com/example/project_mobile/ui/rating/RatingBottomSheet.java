package com.example.project_mobile.ui.rating;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.project_mobile.R;
import com.example.project_mobile.data.StationRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

public class RatingBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_STATION_ID = "station_id";
    private static final String ARG_STATION_NAME = "station_name";

    private int selectedStars = 0;
    private ImageButton[] starButtons;

    public static RatingBottomSheet newInstance(int stationId, String stationName) {
        RatingBottomSheet sheet = new RatingBottomSheet();
        Bundle args = new Bundle();
        args.putInt(ARG_STATION_ID, stationId);
        args.putString(ARG_STATION_NAME, stationName);
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_rating, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int stationId = getArguments().getInt(ARG_STATION_ID);
        String stationName = getArguments().getString(ARG_STATION_NAME);

        TextView nameView = view.findViewById(R.id.rating_station_name);
        nameView.setText(stationName);

        starButtons = new ImageButton[]{
                view.findViewById(R.id.star_1),
                view.findViewById(R.id.star_2),
                view.findViewById(R.id.star_3),
                view.findViewById(R.id.star_4),
                view.findViewById(R.id.star_5)
        };

        for (int i = 0; i < starButtons.length; i++) {
            final int rating = i + 1;
            starButtons[i].setOnClickListener(v -> updateStars(rating));
        }

        TextInputEditText commentInput = view.findViewById(R.id.rating_comment_input);

        view.findViewById(R.id.btn_submit_rating).setOnClickListener(v -> {
            if (selectedStars == 0) {
                Toast.makeText(requireContext(), "Please select a rating", Toast.LENGTH_SHORT).show();
                return;
            }

            StationRepository repository = new StationRepository(requireActivity().getApplication());
            String comment = commentInput.getText() != null ? commentInput.getText().toString() : "";
            
            repository.submitRating(stationId, selectedStars, comment, new StationRepository.Callback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(requireContext(), "Thanks for your rating!", Toast.LENGTH_SHORT).show();
                    dismiss();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(requireContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void updateStars(int rating) {
        selectedStars = rating;
        int activeColor = ContextCompat.getColor(requireContext(), R.color.heatmap_yellow);
        int inactiveColor = ContextCompat.getColor(requireContext(), R.color.slate_950);

        for (int i = 0; i < starButtons.length; i++) {
            if (i < rating) {
                starButtons[i].setImageResource(android.R.drawable.btn_star_big_on);
                starButtons[i].setColorFilter(activeColor);
            } else {
                starButtons[i].setImageResource(android.R.drawable.btn_star_big_off);
                starButtons[i].setColorFilter(inactiveColor);
            }
        }
    }
}
