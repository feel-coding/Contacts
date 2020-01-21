package my.contacts.list;

public class Contact {
    long id;
    int profile;
    String name;
    String phone;

    public Contact(long id, int profile, String name, String phone) {
        this.id = id;
        this.profile = profile;
        this.name = name;
        this.phone = phone;
    }
}
