package ru.job4j.articles.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.articles.model.Article;
import ru.job4j.articles.model.Word;
import ru.job4j.articles.service.generator.ArticleGenerator;
import ru.job4j.articles.store.Store;

import java.lang.ref.WeakReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SimpleArticleService implements ArticleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleArticleService.class.getSimpleName());
    private static final String GENERATE_INFO = "Геренация статей в количестве {}";
    private static final String RESULT_INFO = "Сгенерирована статья № {}";

    private final ArticleGenerator articleGenerator;

    public SimpleArticleService(ArticleGenerator articleGenerator) {
        this.articleGenerator = articleGenerator;
    }

    @Override
    public void generate(Store<Word> wordStore, int count, Store<Article> articleStore) {
        LOGGER.info(GENERATE_INFO, count);
        var words = wordStore.findAll();
        IntStream.iterate(0, i -> i < count, i -> i + 1)
                .peek(i -> LOGGER.info(RESULT_INFO, i))
                .mapToObj(x -> articleGenerator.generate(words))
                .forEach(articleStore::save);
    }
}
