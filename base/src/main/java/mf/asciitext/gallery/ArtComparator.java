package mf.asciitext.gallery;

import java.util.Comparator;

public class ArtComparator implements Comparator<ArtPiece> {
    @Override
    public int compare(ArtPiece o1, ArtPiece o2) {
        return o1.isFullWidth() && !o2.isFullWidth() ? 1 :
                !o1.isFullWidth() && o2.isFullWidth() ? -1 :
                        (o1.getCategory().equalsIgnoreCase(o2.getCategory()) ?
                                o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase()) :
                                o1.getCategory().compareTo(o2.getCategory()));
    }
}
