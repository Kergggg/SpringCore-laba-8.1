package com.yet.spring.core.loggers;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.yet.spring.core.beans.Event;

public class DBLogger extends AbstractLogger {

    private static final Logger logger = LoggerFactory.getLogger(DBLogger.class);

    private JdbcTemplate jdbcTemplate;
    private String schema;

    public DBLogger(JdbcTemplate jdbcTemplate, String schema) {
        this.jdbcTemplate = jdbcTemplate;
        this.schema = schema.toUpperCase();
    }

    public void init() {
        logger.info("Initializing DBLogger with schema: {}", schema);
        createDBSchema();
        createTableIfNotExists();
        updateEventAutoId();
    }

    public void destroy() {
        int totalEvents = getTotalEvents();
        logger.info("Total events in the DB: {}", totalEvents);
        System.out.println("Total events in the DB: " + totalEvents);

        List<Event> allEvents = getAllEvents();
        String allEventIds = allEvents.stream()
                .map(Event::getId)
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
        logger.debug("All DB Event ids: {}", allEventIds);
        System.out.println("All DB Event ids: " + allEventIds);
    }

    private void createDBSchema() {
        try {
            jdbcTemplate.update("CREATE SCHEMA IF NOT EXISTS " + schema);
            logger.info("Schema {} created or already exists", schema);
            System.out.println("Schema " + schema + " created or already exists");
        } catch (DataAccessException e) {
            logger.warn("Could not create schema: {}", e.getMessage());
            System.out.println("Could not create schema: " + e.getMessage());
        }
    }

    private void createTableIfNotExists() {
        try {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS " + schema + ".t_event (" +
                    "id INT NOT NULL PRIMARY KEY, " +
                    "date TIMESTAMP, " +
                    "msg VARCHAR(255)" +
                    ")";
            jdbcTemplate.update(createTableSQL);
            logger.info("Table t_event created");
            System.out.println("Created table t_event");
        } catch (DataAccessException e) {
            logger.error("Could not create table: {}", e.getMessage());
            System.out.println("Could not create table: " + e.getMessage());
        }
    }

    private void updateEventAutoId() {
        int maxId = getMaxId();
        Event.initAutoId(maxId + 1);
        logger.debug("Initialized Event.AUTO_ID to {}", maxId + 1);
        System.out.println("Initialized Event.AUTO_ID to " + (maxId + 1));
    }

    private int getMaxId() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(MAX(id), 0) FROM " + schema + ".t_event",
                    Integer.class);
            return count != null ? count : 0;
        } catch (DataAccessException e) {
            logger.warn("Could not get max ID: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public void logEvent(Event event) {
        try {
            String sql = "INSERT INTO " + schema + ".t_event (id, date, msg) VALUES (?,?,?)";
            jdbcTemplate.update(sql, event.getId(), event.getDate(), event.toString());
            logger.info("Saved to DB event with id {}", event.getId());
            System.out.println("Saved to DB event with id " + event.getId());
        } catch (DataAccessException e) {
            logger.error("Failed to save event to DB", e);
        }
    }

    public int getTotalEvents() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM " + schema + ".t_event",
                    Integer.class);
            return count != null ? count : 0;
        } catch (DataAccessException e) {
            logger.warn("Could not get total events: {}", e.getMessage());
            return 0;
        }
    }

    public List<Event> getAllEvents() {
        String sql = "SELECT * FROM " + schema + ".t_event";
        List<Event> list = jdbcTemplate.query(sql, new RowMapper<Event>() {
            @Override
            public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
                Integer id = rs.getInt("id");
                Date date = rs.getDate("date");
                String msg = rs.getString("msg");
                Event event = new Event(id, new Date(date.getTime()), msg);
                return event;
            }
        });
        logger.debug("Retrieved {} events from DB", list.size());
        return list;
    }
}