package uit.app.document_scanner.model;

public class Person {
    public String id;
    public String fullName;
    public String dob;
    public String hometown;
    public String address;

    public Person(){}

    public Person(String id, String fullName, String dob, String hometown, String address){
        this.id = id;
        this.fullName = fullName;
        this.dob = dob;
        this.hometown = hometown;
        this.address = address;
    }

}
