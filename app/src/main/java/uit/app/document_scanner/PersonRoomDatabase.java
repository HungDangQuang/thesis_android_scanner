package uit.app.document_scanner;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Person.class}, version = 1,exportSchema = false)
public abstract class PersonRoomDatabase extends RoomDatabase {
    private static final String DB_NAME = "person_db";
    private static PersonRoomDatabase instance;

    public static synchronized PersonRoomDatabase getInstance(Context context){
        if (instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),PersonRoomDatabase.class,DB_NAME)
                        .build();
        }
        return instance;
    }
    public abstract PersonDao personDao();

}
