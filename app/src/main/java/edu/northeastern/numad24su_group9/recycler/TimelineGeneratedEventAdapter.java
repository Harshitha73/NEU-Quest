package edu.northeastern.numad24su_group9.recycler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.github.vipulasri.timelineview.TimelineView;

import java.util.List;

import edu.northeastern.numad24su_group9.R;
import edu.northeastern.numad24su_group9.model.GeneratedEvent;

public class TimelineGeneratedEventAdapter extends RecyclerView.Adapter<TimelineGeneratedEventAdapter.TimelineViewHolder> {
    private List<GeneratedEvent> events;

    public TimelineGeneratedEventAdapter() {}

    public void updateData(List<GeneratedEvent> events) {
        this.events = events;
    }

    @Override
    public int getItemViewType(int position) {
        return TimelineView.getTimeLineViewType(position, getItemCount());
    }

    @Override
    public TimelineGeneratedEventAdapter.TimelineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timeline_event, parent, false);
        return new TimelineGeneratedEventAdapter.TimelineViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(TimelineGeneratedEventAdapter.TimelineViewHolder holder, int position) {
        GeneratedEvent event = events.get(position);

        holder.message.setText(event.getTitle());
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class TimelineViewHolder extends RecyclerView.ViewHolder {

        final TextView message;
        final TimelineView timeline;

        TimelineViewHolder(View itemView, int viewType) {
            super(itemView);
            message = itemView.findViewById(R.id.text_timeline_title);
            timeline = itemView.findViewById(R.id.timeline);
            timeline.initLine(viewType);
        }
    }
}
