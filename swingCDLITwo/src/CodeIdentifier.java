public class CodeIdentifier {

    private String title;
    private String gdate;

    public CodeIdentifier(String title, String gdate) {
        this.title = title;
        this.gdate = gdate;
    }

    public String getTitle() {
        return title;
    }

    public String getGdate() {
        return gdate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setGdate(String gdate) {
        this.gdate = gdate;
    }
}
