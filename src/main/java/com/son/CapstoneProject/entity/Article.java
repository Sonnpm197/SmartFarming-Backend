package com.son.CapstoneProject.entity;

import com.fasterxml.jackson.annotation.*;
import com.son.CapstoneProject.entity.login.AppUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.util.List;

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

//@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
//        property  = "articleId",
//        scope     = Long.class)
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

    @Column(columnDefinition = "nvarchar(50)")
    private String category;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", foreignKey = @ForeignKey(name = "FK_ARTICLE_APPUSER"))
    private AppUser appUser;

    // Also save to "tag" table
    @ManyToMany(fetch = FetchType.LAZY/*, cascade = CascadeType.ALL*/)
    private List<Tag> tags;

    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date utilTimestamp;

    @ElementCollection
    private List<String> fileDownloadUris;
}
