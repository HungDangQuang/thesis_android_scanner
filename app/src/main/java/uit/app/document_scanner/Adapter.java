package uit.app.document_scanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    List<String> filenames;
    List<Bitmap> images;
    LayoutInflater inflater;

    public Adapter(Context context, List<String> filenames, List<Bitmap> images){
        this.filenames = filenames;
        this.images = images;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_grid_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.filename.setText(filenames.get(position));
        holder.img.setImageBitmap(images.get(position));
    }

    @Override
    public int getItemCount() {
        return filenames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView filename;
        ImageView img;
        public ViewHolder(@NonNull View itemView){
            super(itemView);
            filename = itemView.findViewById(R.id.filename);
            img = itemView.findViewById(R.id.img);

        }
    }
}
