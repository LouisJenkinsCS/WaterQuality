/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package async;

import org.json.simple.JSONObject;

/**
 *
 * @author lpj11535
 */
public class DataParameter {
    private final String sensor;
    private final long id;
    private final String name;
    private final String unit;

    public DataParameter(String sensor, long id, String name, String unit) {
        this.sensor = sensor;
        this.id = id;
        this.name = name;
        this.unit = unit;
    }
    
    public DataParameter(JSONObject data) {
        this.sensor = (String) data.get("sensor_name");
        this.id = (Long) data.get("id");
        this.name = (String) data.get("name");
        this.unit = (String) data.get("unit");
    }

    public String getSensor() {
        return sensor;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }
    
    public String getPrintableUnit() {
        return unit.replaceAll("\\P{Print}", "");
    }
}
