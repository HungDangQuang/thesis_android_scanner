package uit.app.document_scanner;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "person")
public class Person {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "person_id")
    public String personID;

    @ColumnInfo(name = "person_name")
    public String personName;

    @ColumnInfo(name = "person_dob")
    public String personDOB;

    @ColumnInfo(name = "person_hometown")
    public String personHometown;

    @ColumnInfo(name = "person_address")
    public String personAddress;


    @Ignore
    public Person(int id, String personID,String personName, String personDOB, String personHometown, String personAddress){
        this.id = id;
        this.personID = personID;
        this.personName = personName;
        this.personDOB = personDOB;
        this.personHometown = personHometown;
        this.personAddress = personAddress;
    }


    public Person(String personID,String personName, String personDOB, String personHometown, String personAddress){
        this.personID = personID;
        this.personName = personName;
        this.personDOB = personDOB;
        this.personHometown = personHometown;
        this.personAddress = personAddress;
    }


    public Person(){
        this.id = 0;
        this.personID = "";
        this.personDOB = "";
        this.personHometown = "";
        this.personAddress = "";
    }
}
