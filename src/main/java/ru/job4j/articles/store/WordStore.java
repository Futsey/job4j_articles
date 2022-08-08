package ru.job4j.articles.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.articles.model.Word;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class WordStore implements Store<Word>, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(WordStore.class.getSimpleName());
    private static final String UNABLE_TO_COMPLETE = "Не удалось загрузить настройки. { }";
    private static final String CONNECT = "Подключение к базе данных слов";
    private static final String INIT_TABLE = "Создание схемы таблицы слов";
    private static final String INIT_WORDS = "Заполнение таблицы слов";
    private static final String SAVE_INFO = "Добавление слова в базу данных";
    private static final String FIND_ALL = "Загрузка всех слов";


    private final Properties properties;

    private Connection connection;

    public WordStore(Properties properties) {
        this.properties = properties;
        initConnection();
        initScheme();
        initWords();
    }

    private void initConnection() {
        LOGGER.info(CONNECT);
        try {
            connection = DriverManager.getConnection(
                    properties.getProperty("url"),
                    properties.getProperty("username"),
                    properties.getProperty("password")
            );
        } catch (SQLException e) {
            LOGGER.error(UNABLE_TO_COMPLETE, e.getCause());
            throw new IllegalStateException();
        }
    }

    private void initScheme() {
        LOGGER.info(INIT_TABLE);
        try (var statement = connection.createStatement()) {
            var sql = Files.readString(Path.of("db/scripts", "dictionary.sql"));
            statement.execute(sql);
        } catch (Exception e) {
            LOGGER.error(UNABLE_TO_COMPLETE, e.getCause());
            throw new IllegalStateException();
        }
    }

    private void initWords() {
        LOGGER.info(INIT_WORDS);
        try (var statement = connection.createStatement()) {
            var sql = Files.readString(Path.of("db/scripts", "words.sql"));
            statement.executeLargeUpdate(sql);
        } catch (Exception e) {
            LOGGER.error(UNABLE_TO_COMPLETE, e.getCause());
            throw new IllegalStateException();
        }
    }

    @Override
    public Word save(Word model) {
        LOGGER.info(SAVE_INFO);
        var sql = "insert into dictionary(word) values(?);";
        try (var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, model.getValue());
            statement.executeUpdate();
            var key = statement.getGeneratedKeys();
            if (key.next()) {
                model.setId(key.getInt(1));
            }
        } catch (Exception e) {
            LOGGER.error(UNABLE_TO_COMPLETE, e.getCause());
            throw new IllegalStateException();
        }
        return model;
    }

    @Override
    public List<Word> findAll() {
        LOGGER.info(FIND_ALL);
        var sql = "select * from dictionary";
        var words = new ArrayList<Word>();
        try (var statement = connection.prepareStatement(sql)) {
            var selection = statement.executeQuery();
            while (selection.next()) {
                words.add(new Word(
                        selection.getInt("id"),
                        selection.getString("word")
                ));
            }
        } catch (Exception e) {
            LOGGER.error(UNABLE_TO_COMPLETE, e.getCause());
            throw new IllegalStateException();
        }
        return words;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

}
