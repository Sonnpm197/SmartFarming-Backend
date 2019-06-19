package com.son.CapstoneProject.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.hibernate.search.annotations.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

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
public class Article {

    @Id
    @GeneratedValue
    private Long id;

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
}
