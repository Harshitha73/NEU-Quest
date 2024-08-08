package edu.northeastern.numad24su_group9.recycler;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import edu.northeastern.numad24su_group9.R;
import edu.northeastern.numad24su_group9.firebase.repository.storage.EventImageRepository;
import edu.northeastern.numad24su_group9.model.Event;

public class TimelineEventAdapter extends RecyclerView.Adapter<TimelineEventAdapter.ViewHolder> {
    private List<Event> events;
    private TimelineEventAdapter.OnItemClickListener listener;

    public TimelineEventAdapter() {}

    public void updateData(List<Event> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timeline_event, parent, false);
        return new ViewHolder(view);
    }

    public interface OnItemClickListener {
        void onItemClick(Event event);
    }

    public void setOnItemClickListener(TimelineEventAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);
        Log.d("TimelineEventAdapter", "Event: " + event);
        EventImageRepository eventImageRepository = new EventImageRepository();

        Picasso.get().load(eventImageRepository.getEventImage(event.getImage())).into(holder.imageView);
        holder.titleTextView.setText(event.getTitle());
        holder.descriptionTextView.setText(event.getDescription());
        holder.timeTextView.setText(event.getStartTime());
        holder.itemView.setOnClickListener(v -> handleEventClick(event));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    private void handleEventClick(Event event) {
        // Handle the event click event
        if (listener != null) {
            listener.onItemClick(event);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView titleTextView;
        public TextView descriptionTextView;
        public TextView timeTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.event_image);
            titleTextView = itemView.findViewById(R.id.event_name);
            descriptionTextView = itemView.findViewById(R.id.event_description);
            timeTextView = itemView.findViewById(R.id.event_time);
        }
    }
}
