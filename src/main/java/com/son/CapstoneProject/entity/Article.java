package com.son.CapstoneProject.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.son.CapstoneProject.entity.login.AppUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Indexed
@AnalyzerDef(
        name = "articleCustomAnalyzer",
        tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
        filters = {
                @TokenFilterDef(factory = LowerCaseFilterFactory.class),
                @TokenFilterDef(factory = ASCIIFoldingFilterFactory.class),
        }
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Article {

    @Id
    @GeneratedValue
    private Long articleId;

    @Analyzer(definition = "articleCustomAnalyzer")
    @Field(store = Store.YES)
    @Column(columnDefinition = "nvarchar(255)")
    private String title;

    @Analyzer(definition = "articleCustomAnalyzer")
    @Field(store = Store.YES)
    @Column(columnDefinition = "ntext")
    private String content;

    @Analyzer(definition = "articleCustomAnalyzer")
    @Field(store = Store.YES)
    @Column(columnDefinition = "nvarchar(50)")
    private String category;

    // Author
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", foreignKey = @ForeignKey(name = "FK_ARTICLE_APPUSER"))
    private AppUser appUser;

    // Also save to "tag" table
    @ManyToMany(fetch = FetchType.LAZY/*, cascade = CascadeType.ALL*/)
    private List<Tag> tags;

    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date utilTimestamp;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "article")
    private List<UploadedFile> uploadedFiles;

//    @JsonManagedReference
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "article")
    private List<Comment> comments;

    @ElementCollection
    private List<Long> upvotedUserIds;

    private Integer upvoteCount;

    private int viewCount;

//    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    private List<AppUser> subscribers = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Article article = (Article) o;
        return Objects.equals(articleId, article.articleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(articleId);
    }

    @Override
    public String toString() {
        return "Article{" +
                "articleId=" + articleId +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", category='" + category + '\'' +
                ", utilTimestamp=" + utilTimestamp +
                ", viewCount=" + viewCount +
                '}';
    }
}
