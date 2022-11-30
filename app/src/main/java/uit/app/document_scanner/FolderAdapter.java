package uit.app.document_scanner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {
    private List<String> folderNamesList;

    public FolderAdapter(List<String> namesList){
        folderNamesList = namesList;
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
}

