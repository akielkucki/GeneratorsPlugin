package com.gungens.generators.db;

import com.gungens.generators.Generators;
import com.gungens.generators.cache.GeneratorCache;
import com.gungens.generators.models.Generator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class DbManager {
    private static final String DATABASE_URL = "jdbc:sqlite:"+ Generators.instance.getDataFolder().getAbsolutePath()+"/database.db";
    private Dao<Generator, String> generatorDao;
    @SuppressWarnings("all")
    public DbManager() {
        File dbFile = new File(Generators.instance.getDataFolder().getAbsolutePath()+"/database.db");

        try {

            dbFile.getParentFile().mkdirs();
            dbFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (ConnectionSource connectionSource = new JdbcConnectionSource(DATABASE_URL)) {

            TableUtils.createTableIfNotExists(connectionSource, Generator.class);

            generatorDao = DaoManager.createDao(connectionSource, Generator.class);

            Bukkit.getLogger().log(Level.INFO, "Database created/connected with url: " + DATABASE_URL);
        } catch (SQLException | IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Error while inserting data", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Dao<Generator, String> getGeneratorDao() {
        return generatorDao;
    }
    public void loadGenerators() throws SQLException {
        GeneratorCache cache = GeneratorCache.instance;
        cache.addAll(generatorDao.queryForAll());
    }

    public void flushDirtyGenerators() throws SQLException {
        GeneratorCache cache = GeneratorCache.instance;
        Set<String> dirtyGenerators = cache.getDirtyGenerators();
        Set<String> removedGenerators = cache.getRemovedGenerators();
        for (String id : new HashSet<>(dirtyGenerators)) {
            Generator gen = cache.getGeneratorById(id);
            if (gen != null) {
                gen.setDropItems(gen.getDropItems());
                generatorDao.createOrUpdate(gen);
                dirtyGenerators.remove(id);
            }
        }
        for (String id : new HashSet<>(removedGenerators)) {
            generatorDao.deleteById(id);
            removedGenerators.remove(id);
        }
    }

}
