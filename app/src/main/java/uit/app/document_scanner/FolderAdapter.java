package uit.app.document_scanner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> implements Filterable {
    private List<String> folderNamesList;
    private List<String> filteredFolderNamesList;

    public FolderAdapter(List<String> namesList){
        folderNamesList = namesList;
        filteredFolderNamesList = folderNamesList;
    }

    @NonNull
    @Override
    public FolderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.folder_card_view,parent,false);
        return new FolderAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderAdapter.ViewHolder holder, int position) {
        holder.folderName.setText(folderNamesList.get(position));
    }

    @Override
    public int getItemCount() {
        return folderNamesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView folderName;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            folderName = itemView.findViewById(R.id.folderName);
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String content = charSequence.toString();

                if (!content.isEmpty()){
                    List<String> list = new ArrayList<>();

                    if (folderNamesList.size() == 0) {
                        folderNamesList = filteredFolderNamesList;
                    }
                    for (String folder : folderNamesList){
                        if (folder.contains(content.toLowerCase())){
                            list.add(folder);
                        }
                    }
                    folderNamesList = list;
                }

                else {
                    folderNamesList = filteredFolderNamesList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = folderNamesList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                folderNamesList = (List<String>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
}

