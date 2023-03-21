public class Person {

    private String firstname;
    private String lastname;
    private String fullname;
    private String email;
    private String phone;
    private String address;

    public Person(String firstname, String lastname, String email, String phone, String address) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.fullname = lastname + ", " + firstname;
    }

    public String getFullname() {
        return fullname;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
