package models.xml;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import models.utility.Config;

import java.util.List;

public class Publication {

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    enum Kind {
        ARTICLE, INPROCEEDINGS
    }

    private String paperId;
    private String title;
    private Kind kind;
    private String venue;

    private List<String> authors;
    private String pubYear;

    Publication() {
        authors = Lists.newArrayList();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add(Config.I_PAPER_ID, paperId)
                .add(Config.I_TITLE, title)
                .add(Config.I_KIND, kind)
                .add(Config.I_AUTHORS, authors)
                .add(Config.I_VENUE, venue)
                .add(Config.I_PUB_YEAR, pubYear)
                .toString();
    }

    public void validate() {
        Preconditions.checkArgument(paperId != null);
        Preconditions.checkArgument(title != null);
        Preconditions.checkArgument(kind != null);
        Preconditions.checkArgument(authors.size() >= 0);
        Preconditions.checkArgument(venue != null);
        Preconditions.checkArgument(pubYear != null && Ints.tryParse(pubYear) != null);
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    public String getPaperId() {
        return paperId;
    }

    public void setPaperId(String paperId) {
        this.paperId = paperId;
    }

    public String getPubYear() {
        return pubYear;
    }

    public void setPubYear(String pubYear) {
        this.pubYear = pubYear;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void addAuthor(String author) {
        authors.add(author);
    }
}
