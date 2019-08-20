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
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Indexed
@AnalyzerDef(
        name = "TagCustomAnalyzer",
        tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
        filters = {
                @TokenFilterDef(factory = LowerCaseFilterFactory.class),
                @TokenFilterDef(factory = ASCIIFoldingFilterFactory.class),
        }
)
public class Tag {

    @Id
    @GeneratedValue
    private Long tagId;

    @Analyzer(definition = "TagCustomAnalyzer")
    @Field(store = Store.YES)
    @Column(columnDefinition = "nvarchar(255)")
    private String name;

//    @Analyzer(definition = "TagCustomAnalyzer")
//    @Field(store = Store.YES)
//    @Column(columnDefinition = "ntext")
    private String description;

//    @JsonIgnore
    @JsonBackReference(value = "questions")
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "tags")
    private List<Question> questions;

//    @JsonIgnore
    @JsonBackReference(value = "articles")
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "tags")
    private List<Article> articles;

    private int reputation;

    private int viewCount;

    private Integer viewCountOneWeekAgo;

    private Integer increasementOneWeekAgoTillNow;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return Objects.equals(tagId, tag.tagId) && Objects.equals(name, tag.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagId);
    }
}
