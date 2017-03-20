/*
    A class for holding an error
 */
package common;

import java.time.LocalDateTime;

/**
 *
 * @author Tyler Mutzek
 */
public class ErrorMessage 
{
    private LocalDateTime timeOccured;
    private String errorMessage;
    
    public ErrorMessage()
    {
        
    }
    public ErrorMessage(LocalDateTime timeOccured, String errorMessage)
    {
        this.timeOccured = timeOccured;
        this.errorMessage = errorMessage;
    }

    /**
     * @return the timeOccured
     */
    public LocalDateTime getTimeOccured() {
        return timeOccured;
    }

    /**
     * @param timeOccured the timeOccured to set
     */
    public void setTimeOccured(LocalDateTime timeOccured) {
        this.timeOccured = timeOccured;
    }

    /**
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param errorMessage the errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
