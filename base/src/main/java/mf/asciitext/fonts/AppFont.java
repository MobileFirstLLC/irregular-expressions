package mf.asciitext.fonts;

public abstract class AppFont {

    protected String id;
    protected String name;
    private boolean premium;
    private boolean favorite = false;
    protected String text;

    public AppFont(String id, String name, boolean premium) {
        this.id = id;
        this.name = name;
        this.premium = premium;
    }

    public String GetId() {
        return id;
    }

    public String GetName() {
        return name;
    }

    public String GetText() {
        return text;
    }

    public boolean isReversed() {
        return false;
    }

    public String GetStyledName() {
        return encode(name);
    }

    public boolean IsPremium() {
        return premium;
    }

    public boolean IsFavorite() {
        return favorite;
    }

    public void SetFavorite(boolean value) {
        favorite = value;
    }

    boolean isEmpty(String text) {
        boolean isEmpty = (text == null || text.length() == 0);
        if (isEmpty) {
            SetText(name);
        }
        return isEmpty;
    }

    public void SetText(String text) {
        this.text = encode(text);
    }

    public abstract String encode(String text);
}