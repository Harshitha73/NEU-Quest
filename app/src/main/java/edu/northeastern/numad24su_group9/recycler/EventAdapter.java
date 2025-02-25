package edu.northeastern.numad24su_group9.recycler;

import android.content.Context;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;

import edu.northeastern.numad24su_group9.R;
import edu.northeastern.numad24su_group9.firebase.repository.storage.EventImageRepository;
import edu.northeastern.numad24su_group9.model.Event;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    private List<Event> events;
    private EventAdapter.OnItemClickListener listener;
    private EventAdapter.OnItemSelectListener selectListener;
    private Context context; // Add this
    private ExecutorService executorService;
    private Handler mainHandler;


    // Constructor accepting Context
    public EventAdapter(Context context) {
        this.context = context;
        this.executorService = Executors.newFixedThreadPool(4); // A thread pool of 4 threads
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void updateData(List<Event> events) {
        this.events = events;
        // Prefetch images to cache
        for (Event event : events) {
            EventImageRepository eventImageRepository = new EventImageRepository();
            Glide.with(context)
                    .load(eventImageRepository.getEventImage(event.getImage()))
                    .preload(); // Prefetch the image
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new ViewHolder(view);
    }

    public interface OnItemClickListener {
        void onItemClick(Event event);
    }

    public interface OnItemSelectListener {
        void onItemSelect(Event event);
    }

    public void setOnItemSelectListener(EventAdapter.OnItemSelectListener listener) {
        this.selectListener = listener;
    }

    public void setOnItemClickListener(EventAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);
        EventImageRepository eventImageRepository = new EventImageRepository();

        // Execute heavy work in background
        executorService.execute(() -> {
            String imageUrl = String.valueOf(eventImageRepository.getEventImage(event.getImage()));

            // Update UI on the main thread
            mainHandler.post(() -> {
                Glide.with(holder.imageView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .thumbnail(0.25f) // Load a low-res version first (25% of the original size)
                        .override(300, 300)
                        .into(holder.imageView);

                holder.imageView.setTag(event.getImage());
                holder.titleTextView.setText(event.getTitle());
                holder.descriptionTextView.setText(event.getDescription());
            });
        });

        holder.itemView.setOnClickListener(v -> handleEventClick(event));
        holder.itemView.setOnLongClickListener(v -> {
            v.setSelected(!v.isSelected());
            handleEventSelect(event);
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
        return events.size();
    }

    private void handleEventClick(Event event) {
        if (listener != null) {
            listener.onItemClick(event);
        }
    }

    private void handleEventSelect(Event event) {
        if (selectListener != null) {
            selectListener.onItemSelect(event);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView titleTextView;
        public TextView descriptionTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.event_image);
            titleTextView = itemView.findViewById(R.id.event_name);
            descriptionTextView = itemView.findViewById(R.id.event_description);
        }
    }
}
