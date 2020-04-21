package mf.asciitext.gallery;

public class ArtPiece extends ArtCategory {

    Boolean premium;
    Boolean fullWidth;
    String category;
    String text;

    public ArtPiece(String id, String name, String text, String category, Boolean fullWidth, Boolean premium) {
        super(id, name);
        this.text = text;
        this.category = category;
        this.fullWidth = fullWidth;
        this.premium = premium;
    }

    public String getCategory() {
        return category;
    }

    public Boolean isFullWidth() {
        return fullWidth;
    }
    public Boolean isPremium() {
        return premium;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
