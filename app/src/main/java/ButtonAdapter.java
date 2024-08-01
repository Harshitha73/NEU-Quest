//import android.view.LayoutInflater;
//import android.view.ViewGroup;
//import android.widget.Button;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.firebase.database.core.Context;
//import com.google.firebase.database.core.view.View;
//
//import edu.northeastern.numad24su_group9.R;
//
//public class ButtonAdapter extends RecyclerView.Adapter<ButtonAdapter.ButtonViewHolder> {
//
//    private String[] buttonTitles;
//    private Context context;
//
//    public ButtonAdapter(Context context, String[] buttonTitles) {
//        this.context = context;
//        this.buttonTitles = buttonTitles;
//    }
//
//    @NonNull
//    @Override
//    public ButtonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(context).inflate(R.layout.item_button, parent, false);
//        return new ButtonViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ButtonViewHolder holder, int position) {
//        holder.button.setText(buttonTitles[position]);
//    }
//
//    @Override
//    public int getItemCount() {
//        return buttonTitles.length;
//    }
//
//    public static class ButtonViewHolder extends RecyclerView.ViewHolder {
//        Button button;
//
//        public ButtonViewHolder(@NonNull View itemView) {
//            super(itemView);
//            button = itemView.findViewById(R.id.button);
//        }
//    }
//}
