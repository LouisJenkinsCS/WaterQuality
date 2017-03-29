/*
    A data value that was entered by an admin manually
*/
package common;

import java.io.Serializable;
import java.time.LocalDateTime;
import org.json.simple.JSONObject;

/**
 *
 * @author Tyler Mutzek
 */
public class ManualDataValue implements Serializable, DataInt
{
    private int entryID; //id number distinguishing this data entry (auto incremented)
    private String name;//name of this data type (e.g. temperature, pressure)
    private String units;//units this data value is in (e.g. celcius)
    private String submittedBy;//name distinguishing this sensor from others
    private LocalDateTime time;//time the data value was recorded
    private float value;//the value the sensor detected

    public ManualDataValue()
    {
        
    }
    
    public ManualDataValue(int entryID, String name, String units, String submittedBy, LocalDateTime time, float value)
    {
        this.entryID = entryID;
        this.name = name;
        this.units = units;
        this.submittedBy = submittedBy;
        this.time = time;
        this.value = value;
    }
    /**
     * @return the entryID
     */
    public int getEntryID() {
        return entryID;
    }

    /**
     * @param entryID the entryID to set
     */
    public void setEntryID(int entryID) {
        this.entryID = entryID;
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
     * @return the units
     */
    public String getUnits() {
        return units;
    }

    /**
     * @param units the units to set
     */
    public void setUnits(String units) {
        this.units = units;
    }

    /**
     * @return the submittedBy
     */
    public String getSubmittedBy() {
        return submittedBy;
    }

    /**
     * @param submittedBy the submittedBy to set
     */
    public void setSubmittedBy(String submittedBy) {
        this.submittedBy = submittedBy;
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

    public JSONObject toJSON() 
    {
        JSONObject jO = new JSONObject();
        jO.put("entryID", entryID);
        jO.put("name", name);
        jO.put("units", units);
        jO.put("submittedBy", submittedBy);
        jO.put("value", value);
        jO.put("time",time.toString());
        return jO;
    }
}
