
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
    private String sensor;
    private long id;
    private String name;
    private String unit;
    private String description;

    public DataParameter(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public void fromJSON(JSONObject data) {
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

    public String getDescription() {
        return description.replaceAll("\\P{Print}", "");
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return "DataParameter{" + "sensor=" + sensor + ", id=" + id + ", name=" + name + ", unit=" + unit + ", description=" + description + '}';
    }
    
}
