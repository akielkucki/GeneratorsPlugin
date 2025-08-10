package com.gungens.generators.db;

import com.gungens.generators.Generators;
import com.gungens.generators.cache.BreakableGeneratorCache;
import com.gungens.generators.cache.GeneratorCache;
import com.gungens.generators.models.BreakableGenerator;
import com.gungens.generators.models.Generator;
import com.gungens.generators.services.GeneratorService;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class DbManager {
    private static final Logger log = LoggerFactory.getLogger(DbManager.class);
    private Dao<Generator, String> generatorDao;
    private Dao<BreakableGenerator, String> breakableGeneratorDao;
    @SuppressWarnings("all")
    public DbManager() {
        String DATABASE_URL = "jdbc:postgresql://147.135.31.124:5432/gungens?user=gungens&password=Buildcr33k*";
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
            TableUtils.createTableIfNotExists(connectionSource, BreakableGenerator.class);
            breakableGeneratorDao = DaoManager.createDao(connectionSource, BreakableGenerator.class);

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

        BreakableGeneratorCache breakableGeneratorCache = BreakableGeneratorCache.instance;
        breakableGeneratorCache.addAll(breakableGeneratorDao.queryForAll());
        for (BreakableGenerator breakableGenerator : breakableGeneratorCache.getBreakableGenerators()) {
            breakableGenerator.setHealth(breakableGenerator.getMaxHealth());
            breakableGenerator.setBlockType(Material.valueOf(breakableGenerator.getBlockTypeName()));
            GeneratorService.getInstance().spawnHealthBarIfNotPresent(breakableGenerator);
        }
    }

    public void flushDirtyGenerators() throws SQLException {
        GeneratorCache cache = GeneratorCache.instance;
        BreakableGeneratorCache breakableGeneratorCache = BreakableGeneratorCache.instance;

        Set<String> dirtyGenerators = cache.getDirtyGenerators();
        Set<String> removedGenerators = cache.getRemovedGenerators();

        Set<String> dirtyBreakableGenerators = breakableGeneratorCache.getDirtyBreakableGenerators();
        Set<String> removedBreakableGenerators = breakableGeneratorCache.getRemovedGenerators();


        for (String id : new HashSet<>(dirtyGenerators)) {
            Generator gen = cache.getGeneratorById(id);
            if (gen != null) {
                gen.setDropItems(gen.getDropItems());
                gen.setBlockType(Material.valueOf(gen.getBlockTypeName()));
                gen.getLocation().getBlock().setType(gen.getBlockType());
                try {
                    generatorDao.createOrUpdate(gen);
                } catch (Exception e) {
                    Throwable root = e;
                    while (root.getCause() != null) root = root.getCause();
                    Bukkit.getLogger().log(Level.SEVERE, "Insert failed, root cause: " + root.getMessage(), root);
                    throw e;
                }

                dirtyGenerators.remove(id);
            }
        }
        for (String id : new HashSet<>(removedGenerators)) {
            generatorDao.deleteById(id);
            removedGenerators.remove(id);
        }

        for (String id : new HashSet<>(dirtyBreakableGenerators)) {
            BreakableGenerator breakableGenerator = breakableGeneratorCache.getGeneratorById(id);
            log.info("Updating breakable generator: {}", id);
            if (breakableGenerator != null) {
                breakableGenerator.setDropItems(breakableGenerator.getDropItems());
                breakableGenerator.setHealth(breakableGenerator.getMaxHealth());
                breakableGenerator.setBlockType(breakableGenerator.getLocation().getBlock().getType() );
                breakableGeneratorDao.createOrUpdate(breakableGenerator);
                dirtyBreakableGenerators.remove(id);
            }
        }
        for (String id : removedBreakableGenerators) {
            log.info("Removing breakable generator: {}", id);

            breakableGeneratorDao.deleteById(id);
            removedBreakableGenerators.remove(id);
        }
    }

}
