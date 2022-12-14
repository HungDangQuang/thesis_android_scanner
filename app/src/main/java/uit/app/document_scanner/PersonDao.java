package uit.app.document_scanner;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PersonDao {
    @Query("SELECT * FROM person")
    List<Person> getAll();

    @Query("SELECT * FROM person WHERE uid IN (:userIds)")
    List<Person> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM person WHERE full_name LIKE :first")
    Person findByName(String first);

    @Insert
    void insertAll(Person... people);

    @Delete
    void delete(Person person);

    void insertPerson(Person person);
}