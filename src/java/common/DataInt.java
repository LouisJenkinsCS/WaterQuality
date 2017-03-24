/*
 * Interface for DataValues (manual and not)
 */
package common;

import org.json.simple.JSONObject;

/**
 *
 * @author Tyler Mutzek
 */
public interface DataInt 
{
    /*
        Allows all data values to be known to have a toJSON function
        Converts this class to a JSON object
    */
    JSONObject toJSON();
}
