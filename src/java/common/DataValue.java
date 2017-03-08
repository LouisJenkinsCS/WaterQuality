/*
 * A modular java bean for storage for handling any kind of data value
 *
 */
package common;

import java.io.Serializable;
import java.time.LocalDateTime;
/**
 *
 * @author Tyler Mutzek
 */
public class DataValue implements Serializable
{
    private int entryID; //id number distinguishing this data entry (auto incremented)
    private String name;//name of this data type (e.g. temperature, pressure)
    private String units;//units this data value is in (e.g. celcius)
    private String sensor;//name distinguishing this sensor from others
    private LocalDateTime time;//time the data value was recorded
    private float value;//the value the sensor detected
    private float delta;
    
    public DataValue()
    {
        
    }
    public DataValue(int entryID, String name, String units, String sensor, LocalDateTime time, float value, float delta)
    {
        this.entryID = entryID;
        this.name = name;
        this.units = units;
        this.sensor = sensor;
        this.time = time;
        this.value = value;
        this.delta = delta;
    }
    /**
     * @return the entryID
     */
    public int getEntryID() {
        return entryID;
    }

    /**
     * @param entryID the id to set
     */
    public void setEntryID(int entryID) {
        this.entryID = entryID;
    }

    /**
     * @return the units
     */
    public String getUnits() {
        return units;
    }

    /**
     * @param units the unit to set
     */
    public void setUnits(String units) {
        this.units = units;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the sensor
     */
    public String getSensor() {
        return sensor;
    }

    /**
     * @param sensor the node_name to set
     */
    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    /**
     * @return the time
     */
    public LocalDateTime getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    /**
     * @return the value
     */
    public float getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(float value) {
        this.value = value;
    }
    
    /**
     * @return the value
     */
    public float getDelta() {
        return delta;
    }

    /**
     * @param value the value to set
     */
    public void setDelta(float delta) {
        this.delta = delta;
    }
    
    public String toString()
    {
        return name + ", " + units + ", " + sensor + ", " + value + ", " + delta + ", " + time;
    }
    
}
