package com.son.CapstoneProject.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.*;
import org.apache.lucene.analysis.charfilter.MappingCharFilterFactory;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.hibernate.search.annotations.*;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Parameter;
//import org.codehaus.jackson.annotate.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
// As it is correctly suggested in previous answers, lazy loading means that when you
// fetch your object from the database, the nested objects are not fetched (and may be fetched later when required).
// Now Jackson tries to serialize the nested object (== make JSON out of it), but fails as it
// finds JavassistLazyInitializer instead of normal object. To fix this, use the annotation below
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

// When the Hibernate SessionFactory bootstraps, Hibernate Search looks for all
// mapped entities marked as @Indexed and processes them
@Indexed

@AnalyzerDef(
        name = "customAnalyzer",
        charFilters = {
                // Same as ASCIIFoldingFilterFactory but we can specify mapping file
                @CharFilterDef(factory = MappingCharFilterFactory.class, params = {
                        @Parameter(
                                name = "mapping",
                                value = "mapping.txt")
                })
        },
        tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
        filters = {
                @TokenFilterDef(factory = LowerCaseFilterFactory.class)
        }
)
public class Question implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    // default: index=Index.YES, analyze=Analyze.YES and store=Store.NO
    @Analyzer(definition = "customAnalyzer")
    @Field(store = Store.YES)
    @Column(columnDefinition = "nvarchar(100)")
    private String title;

    // TODO: change whether we should analise this field or not
    @Analyzer(definition = "customAnalyzer")
    @Field(store = Store.YES)
    @Column(columnDefinition = "ntext")
    private String content;

    // Many questions can be asked by an user
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clientEmail", foreignKey = @ForeignKey(name = "FK_QUESTION_CLIENT"))
    private Client client;

    // One question can have many answers
    // @JsonIgnore to ignore this field when parsing the request body to this class (deserialization)
    // "@JsonIgnore is used to ignore the logical property used IN SERIALIZATION AND DESERIALIZATION"
    @JsonIgnore
    // @JsonManagedReference means this list is shown in response,
    // and @JsonBackReference (for a single object) means
    // this will not be shown in response (avoid recursive)
    @JsonManagedReference
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "question")
    private List<Answer> answers = new ArrayList<>();

}
