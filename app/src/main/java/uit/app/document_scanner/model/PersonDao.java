package uit.app.document_scanner.model;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PersonDao {
    private DatabaseReference databaseReference;

    public PersonDao(){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference(Person.class.getSimpleName());
    }

    public Task<Void> add(Person p){
        return databaseReference.push().setValue(p);
    }
}
