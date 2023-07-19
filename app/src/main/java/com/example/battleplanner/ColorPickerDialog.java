package com.example.battleplanner;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class ColorPickerDialog extends DialogFragment {

    private OnColorSelectedListener colorSelectedListener;
    private int initialColor;

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }

    public void setInitialColor(int color) {
        this.initialColor = color;
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        this.colorSelectedListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_color_picker, null);

        final SeekBar seekBarRed = view.findViewById(R.id.seekBarRed);
        final SeekBar seekBarGreen = view.findViewById(R.id.seekBarGreen);
        final SeekBar seekBarBlue = view.findViewById(R.id.seekBarBlue);
        final TextView textViewColor = view.findViewById(R.id.colorPreview);

        // Set the maximum values for the SeekBars
        seekBarRed.setMax(255);
        seekBarGreen.setMax(255);
        seekBarBlue.setMax(255);

        // Set the initial progress values for the SeekBars
        int red = Color.red(initialColor);
        int green = Color.green(initialColor);
        int blue = Color.blue(initialColor);
        seekBarRed.setProgress(red);
        seekBarGreen.setProgress(green);
        seekBarBlue.setProgress(blue);

        // SeekBar listeners to update the color preview
        seekBarRed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateColor(textViewColor, seekBarRed.getProgress(), seekBarGreen.getProgress(), seekBarBlue.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // No action needed when tracking starts
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // No action needed when tracking stops
            }
        });

        seekBarGreen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateColor(textViewColor, seekBarRed.getProgress(), seekBarGreen.getProgress(), seekBarBlue.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // No action needed when tracking starts
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // No action needed when tracking stops
            }
        });

        seekBarBlue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateColor(textViewColor, seekBarRed.getProgress(), seekBarGreen.getProgress(), seekBarBlue.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // No action needed when tracking starts
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // No action needed when tracking stops
            }
        });

        builder.setView(view)
                .setTitle("Color Picker")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Create the color using the RGB values from the SeekBars
                    int color = Color.rgb(seekBarRed.getProgress(), seekBarGreen.getProgress(), seekBarBlue.getProgress());
                    if (colorSelectedListener != null) {
                        // Notify the listener with the selected color
                        colorSelectedListener.onColorSelected(color);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Cancel the dialog when the "Cancel" button is clicked
                    dialog.cancel();
                });

        return builder.create();
    }


    private void updateColor(TextView textView, int red, int green, int blue) {
        // Create the color using the RGB values
        int color = Color.rgb(red, green, blue);
        // Set the background color of the color preview TextView
        textView.setBackgroundColor(color);
    }
}
