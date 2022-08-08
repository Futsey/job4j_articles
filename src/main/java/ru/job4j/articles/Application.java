package ru.job4j.articles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.articles.service.SimpleArticleService;
import ru.job4j.articles.service.generator.RandomArticleGenerator;
import ru.job4j.articles.store.ArticleStore;
import ru.job4j.articles.store.WordStore;

import java.io.InputStream;
import java.util.Properties;

public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class.getSimpleName());
    private static final String LOAD_APP = "Загрузка настроек приложения";
    private static final String PROPERTIES_FILE_NAME = "application.properties";
    private static final String UNABLE_TO_COMPLETE = "Не удалось загрузить настройки. { }";

    public static final int TARGET_COUNT = 1_000_000;

    public static void main(String[] args) {
        var properties = loadProperties();
        var articleService = new SimpleArticleService(new RandomArticleGenerator());
        articleService.generate(new WordStore(properties), TARGET_COUNT, new ArticleStore(properties));
    }

    private static Properties loadProperties() {
        LOGGER.info(LOAD_APP);
        var properties = new Properties();
        try (InputStream in = Application.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE_NAME)) {
            if (in != null) {
                properties.load(in);
            }
        } catch (Exception e) {
            LOGGER.error(UNABLE_TO_COMPLETE, e.getCause());
            throw new IllegalStateException();
        }
        return properties;
    }

}
