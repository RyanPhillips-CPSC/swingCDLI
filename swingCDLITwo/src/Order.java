public class Order {

    private String odate;
    private String completed;

    public Order(String odate, String completed) {
        this.odate = odate;
        this.completed = completed;
    }

    public String getOdate() {
        return odate;
    }

    public void setOdate(String odate) {
        this.odate = odate;
    }

    public String getCompleted() {
        return completed;
    }

    public void setCompleted(String completed) {
        this.completed = completed;
    }
}
