package com.redhat.mapitspring.data;

import com.redhat.mapitspring.model.MapPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static org.springframework.core.io.ResourceLoader.CLASSPATH_URL_PREFIX;


@Component
public class MapData {
    private static final Logger LOG = LoggerFactory.getLogger(MapData.class);
    private static final String DATA_FILENAME = "airports.json";

    private JdbcTemplate jdbc;

    @Autowired
    private Environment env;

    @Autowired
    private ResourceLoader resourceLoader;

    @PostConstruct
    protected void init() {
        try {
            String dbName = env.getProperty("POSTGRES_DB", "mapitdb");
            String dbHost = env.getProperty("POSTGRES_HOST", "localhost");

            LOG.info("Connecting to database {}:5432/{}", dbHost, dbName);
            DriverManagerDataSource dm = new DriverManagerDataSource();
            dm.setDriverClassName("org.postgresql.Driver");
            dm.setUrl("jdbc:postgresql://" + dbHost + ":5432/" + dbName);
            dm.setUsername(env.getProperty("POSTGRES_USER", "postgresql"));
            dm.setPassword(env.getProperty("POSTGRES_PASSWORD", "postgresql"));

            jdbc = new JdbcTemplate(dm, false);

            createTables();
        } catch (Exception e) {
            LOG.error("Failed to connect to database", e);
        }

        if (jdbc != null) {
            Integer itemCount = 0;
            try {
                itemCount = jdbc.queryForObject("select count(*) from mappoint", Integer.class);

                if (itemCount == 0) {
                    loadAirports();
                }
            } catch (DataAccessException e) {
                LOG.warn(e.getMessage());
            }
        }
    }

    private void createTables() {
        try {
            // create table
            jdbc.execute("CREATE TABLE mappoint (" +
                    "id serial NOT NULL, " +
                    "name character varying(256), " +
                    "lat character varying(20), " +
                    "lon character varying(20), " +
                    "region character varying(10)," +
                    "description character varying(256), " +
                    "CONSTRAINT mappoint_pk PRIMARY KEY (id ));");

        } catch (Exception ex) {
            LOG.debug("Failed to create mappoint table, it might already exist!");
            LOG.debug(ex.getMessage());
        }
    }

    private void loadAirports() {
        LOG.info("Importing airport data");

        try {
            // import json data
            String json = getJsonContent(DATA_FILENAME);
            JsonParser parser = JsonParserFactory.getJsonParser();
            List<Object> jsonObjects = parser.parseList(json);
            List<String[]> mapPoints = new LinkedList<>();
            for (Object o : jsonObjects) {
                Map<String, String> map = (Map<String, String>) o;

                String name = map.get("name");
                String lat = map.get("lat");
                String lon = map.get("lon");
                String region = map.get("continent");
                String desc = map.get("iata");

                if ("airport".equals(map.get("type"))) {
                    mapPoints.add(new String[]{name, lat, lon, region, desc});
                }
            }

            jdbc.batchUpdate("insert into mappoint (name,lon,lat,region,description) values (?,?,?,?,?)",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setString(1, mapPoints.get(i)[0]);
                            ps.setString(2, mapPoints.get(i)[1]);
                            ps.setString(3, mapPoints.get(i)[2]);
                            ps.setString(4, mapPoints.get(i)[3]);
                            ps.setString(5, mapPoints.get(i)[4]);
                        }

                        @Override
                        public int getBatchSize() {
                            return mapPoints.size();
                        }
                    });
            LOG.info("{} airports data imported", mapPoints.size());

        } catch (IOException ex) {
            LOG.error("Failed to parse map data from {}", CLASSPATH_URL_PREFIX + DATA_FILENAME);
            ex.printStackTrace();
        }
    }

    private String getJsonContent(String fileName) throws IOException {
        StringBuilder json = new StringBuilder("");
        InputStream is = resourceLoader.getResource(CLASSPATH_URL_PREFIX + fileName).getInputStream();
        Scanner scanner = new Scanner(is);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            json.append(line).append("\n");
        }

        scanner.close();
        return json.toString();
    }

    public List<MapPoint> getAirports() {
        return jdbc.query(
                "select id, name, lon, lat, region, description from mappoint where region = 'AS' ORDER by id DESC LIMIT 100",
                (rs, i) -> MapPoint.of(rs.getString("name"),
                        rs.getString("lon"),
                        rs.getString("lat"),
                        rs.getString("description")));
    }
}
