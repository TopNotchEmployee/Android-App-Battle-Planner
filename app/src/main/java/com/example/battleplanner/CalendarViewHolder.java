package com.example.battleplanner;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public final TextView dayOfMonth;
    public final TextView eventInfo;
    private final calendar_adapter.OnItemListener onItemListener;

    public CalendarViewHolder(@NonNull View itemView, calendar_adapter.OnItemListener onItemListener) {
        super(itemView);
        dayOfMonth = itemView.findViewById(R.id.cellDayText);
        eventInfo = itemView.findViewById(R.id.cellEventInfoText); // Add this line to reference the event info TextView
        this.onItemListener = onItemListener;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

    }
}

