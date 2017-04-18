/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 *
 * @author lpj11535
 */
public class TimestampUtils {
    public static Instant toUTCInstant(String timestamp) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        ZonedDateTime utc = ZonedDateTime.ofInstant(format.parse(timestamp.replace("T", " ").replace("Z", "")).toInstant(), ZoneOffset.UTC);
        return utc.toInstant();
    }
}
