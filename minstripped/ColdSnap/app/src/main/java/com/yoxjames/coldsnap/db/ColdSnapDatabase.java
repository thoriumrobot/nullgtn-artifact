package com.yoxjames.coldsnap.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import com.yoxjames.coldsnap.db.image.PlantImageRow;
import com.yoxjames.coldsnap.db.image.PlantImageRowDAO;
import com.yoxjames.coldsnap.db.plant.PlantRow;
import com.yoxjames.coldsnap.db.plant.PlantRowDAO;
import com.yoxjames.coldsnap.db.weather.ForecastHourRow;
import com.yoxjames.coldsnap.db.weather.ForecastHourRowDAO;

@// TODO: Maybe we should export the schema?
Database(// TODO: Maybe we should export the schema?
entities = { PlantRow.class, ForecastHourRow.class, PlantImageRow.class }, // TODO: Maybe we should export the schema?
version = 3, exportSchema = false)
public abstract class ColdSnapDatabase extends RoomDatabase {

    public abstract PlantRowDAO plantRowDAO();

    public abstract PlantImageRowDAO plantImageRowDAO();

    public abstract ForecastHourRowDAO forecastHourRowDAO();
}
