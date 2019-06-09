package com.son.CapstoneProject.repository;

import com.son.CapstoneProject.domain.Article;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
public class ArticleRepositoryTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Test
    public void testSaveArticle() {
        Article article = new Article();
        article.setTitle("Định đưa lợn Móng Cái ra đảo... tránh dịch");
        article.setContent("Theo ông Đông, dịch tả lợn châu Phi tấn công dẫn tới thiệt hại lớn," +
                " đặc biệt là đối với giống lợn Móng Cái, bởi hiện tại Móng Cái chỉ" +
                " còn khoảng 300 con thuần chủng. Chính vì thế, tỉnh Quảng Ninh đang " +
                "tính tới việc đưa đàn lợn này ra ngoài đảo xa để hạn chế sự lây lan " +
                "của dịch tả lợn châu Phi.");
        article.setCategory("Pig");
        Article article1 = articleRepository.save(article);

        // Evaluate
        Assert.assertEquals(article, article1);
    }
}