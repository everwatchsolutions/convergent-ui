/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.acesinc.convergentui.content;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

/**
 *
 * @author andrewserff
 */
public class BufferedImageHttpMessageConverter extends AbstractHttpMessageConverter<BufferedImage> {

    private static final Logger log = LoggerFactory.getLogger(BufferedImageHttpMessageConverter.class);

    @Override
    public boolean canRead(Class<?> type, MediaType mt) {
        log.debug("Can we read: Class[ " + type + "] & ContentType[ " + mt + " ]");
        if (BufferedImage.class.isAssignableFrom(type) || (mt != null && "image".equalsIgnoreCase(mt.getType()) )) {
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    protected boolean supports(Class<?> type) {
        //I don't believe this is actually used because we overrode canRead
//        return BufferedImage.class.isAssignableFrom(type);
        return true;
    }

    @Override
    protected BufferedImage readInternal(Class<? extends BufferedImage> type, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return ImageIO.read(inputMessage.getBody());
    }

    @Override
    protected void writeInternal(BufferedImage t, HttpOutputMessage hom) throws IOException, HttpMessageNotWritableException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
