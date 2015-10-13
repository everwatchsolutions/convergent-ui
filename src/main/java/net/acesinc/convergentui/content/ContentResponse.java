/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.convergentui.content;

import java.io.Serializable;
import org.springframework.http.MediaType;

/**
 *
 * @author andrewserff
 */
public class ContentResponse implements Serializable {
    private Object content;
    private MediaType contentType;
    private boolean error = false;
    private String message;

    @Override
    public String toString() {
        if (error) {
            return "Content has ERROR: " + message + " \nContent: " + content;
        } else {
            return "Content: " + content;
        }
    }

    
    /**
     * @return the content
     */
    public Object getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(Object content) {
        this.content = content;
    }

    /**
     * @return the error
     */
    public boolean isError() {
        return error;
    }

    /**
     * @param error the error to set
     */
    public void setError(boolean error) {
        this.error = error;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the contentType
     */
    public MediaType getContentType() {
        return contentType;
    }

    /**
     * @param contentType the contentType to set
     */
    public void setContentType(MediaType contentType) {
        this.contentType = contentType;
    }
}
