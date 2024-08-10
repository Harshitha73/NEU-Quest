package edu.northeastern.numad24su_group9.recycler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.northeastern.numad24su_group9.R;
import edu.northeastern.numad24su_group9.model.GeneratedEvent;

public class GeneratedEventsAdapter extends RecyclerView.Adapter<GeneratedEventsAdapter.ViewHolder> {
    private List<GeneratedEvent> generatedEvents;
    private GeneratedEventsAdapter.OnItemSelectListener selectListener;

    public GeneratedEventsAdapter() {}

    public void updateData(List<GeneratedEvent> generatedEvents) {
        this.generatedEvents = generatedEvents;
    }

    @NonNull
    @Override
    public GeneratedEventsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_generated_event, parent, false);
        return new GeneratedEventsAdapter.ViewHolder(view);
    }

    public interface OnItemSelectListener {
        void onItemSelect(GeneratedEvent event);
    }

    public void setOnItemSelectListener(GeneratedEventsAdapter.OnItemSelectListener listener) {
        this.selectListener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull GeneratedEventsAdapter.ViewHolder holder, int position) {
        GeneratedEvent event = generatedEvents.get(position);

        holder.titleTextView.setText(event.getTitle());
        holder.descriptionTextView.setText(event.getDescription());
        holder.itemView.setOnLongClickListener(v -> {
            v.setSelected(!v.isSelected());
            handleEventSelect(event);
            // Show the selected item in some way, e.g., change the background color or display it in a separate view
            if (v.isSelected()) {
                v.findViewById(R.id.selection_indicator).setVisibility(View.VISIBLE);
            } else {
                v.findViewById(R.id.selection_indicator).setVisibility(View.GONE);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return generatedEvents.size();
    }

    private void handleEventSelect(GeneratedEvent event) {
        if (selectListener != null) {
            selectListener.onItemSelect(event);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView descriptionTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.event_name);
            descriptionTextView = itemView.findViewById(R.id.event_description);
        }
    }
}