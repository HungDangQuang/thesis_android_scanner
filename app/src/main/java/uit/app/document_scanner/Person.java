package uit.app.document_scanner;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Person {
    @PrimaryKey
    public int uid;

    @ColumnInfo(name = "id")
    public String personId;

    @ColumnInfo(name = "full_name")
    public String firstName;

    @ColumnInfo(name = "dob")
    public String dob;

    @ColumnInfo(name = "hometown")
    public String hometown;

    @ColumnInfo(name = "address")
    public String address;
}