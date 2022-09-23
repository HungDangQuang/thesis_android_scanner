package uit.app.document_scanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

//    List<String> filenames;
    List<File> images;
    LayoutInflater inflater;

    public Adapter(Context context, List<File> images){
//        this.filenames = filenames;
        this.images = images;
        this.inflater = LayoutInflater.from(context);
        if (!hasObservers()){
//            Log.d("has observer", "Adapter: has observer ");
            setHasStableIds(true);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = inflater.inflate(R.layout.custom_grid_layout,parent,false);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_grid_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        File file = images.get(position);
        holder.filename.setText(file.getName());
        Picasso.get().load(file).into(holder.img);
        Log.d("binding", "onBindViewHolder: " + getItemId(position));
    }

    @Override
    public int getItemCount() {
        return images.size();
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

//    @Override
//    public void setHasStableIds(boolean hasStableIds) {
//        super.setHasStableIds(hasStableIds);
//    }


    @Override
    public long getItemId(int position) {
        return images.get(position).hashCode();
    }
}
