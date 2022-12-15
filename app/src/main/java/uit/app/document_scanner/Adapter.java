package uit.app.document_scanner;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> implements Filterable {

//    List<String> filenames;
    List<File> images;
    List<File> filteredImages;
    LayoutInflater inflater;

    public Adapter(Context context, List<File> images){
//        this.filenames = filenames;
        this.images = images;
        this.filteredImages = images;
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
        String name = file.getName();
        holder.filename.setText(name.substring(0,name.lastIndexOf(".")));
        Picasso.get().load(file).into(holder.img);
        holder.filePath = file.getAbsolutePath();
        Log.d("binding", "onBindViewHolder: " + getItemId(position));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView filename;
        ImageView img;
        private Context context;
        String filePath;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            filename = itemView.findViewById(R.id.filename);
            img = itemView.findViewById(R.id.img);
            context = itemView.getContext();
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(context, ViewDocumentActivity.class);
            intent.putExtra("filePath", filePath);
            context.startActivity(intent);
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

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String content = charSequence.toString();

                if (!content.isEmpty()) {
                    List<File> list = new ArrayList<>();
                    if (images.size() == 0) {
                        images = filteredImages;
                    }
                    for (File image : images) {
                        if(image.getName().toLowerCase().contains(content.toLowerCase())){
                            list.add(image);
                        }
                    }
                    images = list;

                }
                else {
                    images = filteredImages;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = images;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                images = (List<File>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

}
