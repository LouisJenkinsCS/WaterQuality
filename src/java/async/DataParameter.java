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
        
        // TODO: Do this in a more dynamic manner, I.E: A Configuration File
        // The below is salvaged from Tyler's InsertJSON origin DatabaseManager method..
        
        
        if(name.equals("Temperature")) {
            // Air temperature is stored as degrees C (unicode), which also needs to be stripped
            // L.J: Modified this to be extremely hard-coded, as these are too ambiguous and the
            // database is in too infantile of a state to handle this dynamically.
            if (unit.equals("℃")) {
                unit = "C";
                name = "Air Temperature";
                description = "This is how much heat is present in the air adjacent to the stream.";
            } else {
                name = "Water Temperature";
                description = "Not only does water temperature govern the metabolic rates of aquatic organisms, "
                        + "it also determines the amount of dissolved oxygen the water will hold.  Therefore, "
                        + "temperature often determines the types of organisms present.  For example, trout require "
                        + "cooler waters while bass can tolerate warmer conditions  ";
            }
        } else if(name.equals("HDO")) {
            //HDO refers to a method of checking dissolved oxygen
            
            if (unit.equals("mg/l")) {
                name = "Dissolved Oxygen";
                description = "Concentration of oxygen dissolved in the water.  "
                        + "Water gains and losses oxygen through photosynthesis, respiration and atmospheric exchange.  "
                        + "We typically see oxygen concentration increase during the day due to photosynthesis "
                        + "and decreases at night due to respiration and cessation of photosynthesis.  Different "
                        + "aquatic organisms have different oxygen requirements.  For example, brook trout require "
                        + "high oxygen levels (>7 mg/L), while few aquatic organisms tolerate levels less than 3 mg/L.  ";
            } else {
                name = "Dissolved Oxygen Saturation";
                description = "The current oxygen content of water compared to the maximum amount of oxygen the water will hold at "
                        + "the current temperature.  Cold water holds less oxygen than warm water.  It is not unusual for "
                        + "streams with high rates of photosynthesis to become supersaturated with oxygen (>100%) during daylight hours. ";
                
            }
        } else if(name.equals("Turbidity Dig")) {
            //dig only refers to digital, not something meaningful
            name = "Turbidity";
        }
        
        unit = unit.replace("μ","u");//special chars not allowed
        
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
