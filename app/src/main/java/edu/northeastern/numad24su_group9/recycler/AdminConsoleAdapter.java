package edu.northeastern.numad24su_group9.recycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

import edu.northeastern.numad24su_group9.R;
import edu.northeastern.numad24su_group9.firebase.repository.database.EventRepository;
import edu.northeastern.numad24su_group9.firebase.repository.storage.EventImageRepository;
import edu.northeastern.numad24su_group9.model.Event;

public class AdminConsoleAdapter extends RecyclerView.Adapter<AdminConsoleAdapter.ViewHolder> {
    private List<Event> events;
    private EventAdapter.OnItemClickListener listener;
    private EventAdapter.OnItemSelectListener selectListener;
    private Context context;

    public AdminConsoleAdapter(Context context) {
        this.context = context;
    }

    public void updateData(List<Event> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_console_recycler_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);

        EventImageRepository eventImageRepository = new EventImageRepository();

        if (holder.imageView.getTag() == null || !holder.imageView.getTag().equals(event.getImage())) {
            Glide.with(holder.imageView.getContext())
                    .load(eventImageRepository.getEventImage(event.getImage()))
                    .placeholder(R.drawable.placeholder_image)
                    .override(300, 300)
                    .into(holder.imageView);
            holder.imageView.setTag(event.getImage());
        }        holder.titleTextView.setText(event.getTitle());
        holder.descriptionTextView.setText(event.getDescription());
        holder.approveButton.setOnClickListener(v -> {
            event.setIsReported(false);
            EventRepository eventRepository = new EventRepository();
            DatabaseReference eventRef = eventRepository.getEventRef().child(event.getEventID());
            eventRef.setValue(event);
            // Remove the event from the list
            events.remove(position);
            // Notify the adapter that the item has been removed
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, events.size());});

        // Delete the event from the database
        holder.rejectButton.setOnClickListener(v -> {
            EventRepository eventRepository = new EventRepository();
            DatabaseReference eventRef = eventRepository.getEventRef().child(event.getEventID());
            eventRef.removeValue()
                    .addOnSuccessListener(aVoid -> {
                        // Handle success
                        // Remove the event from the list
                        events.remove(position);
                        // Notify the adapter that the item has been removed
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, events.size());
                        Toast.makeText(context, "Event deleted successfully", Toast.LENGTH_LONG).show();
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure
                        Toast.makeText(context, "Failed to delete event", Toast.LENGTH_LONG).show();
                    });
            ;         });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView titleTextView;
        public TextView descriptionTextView;
        public Button approveButton;
        public Button rejectButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.event_image);
            titleTextView = itemView.findViewById(R.id.event_name);
            descriptionTextView = itemView.findViewById(R.id.event_description);
            approveButton = itemView.findViewById(R.id.approve_event_button);
            rejectButton = itemView.findViewById(R.id.remove_event_button);
        }
    }
}
