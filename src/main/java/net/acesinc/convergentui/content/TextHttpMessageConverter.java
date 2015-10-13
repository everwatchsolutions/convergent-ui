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
public class TextHttpMessageConverter extends AbstractHttpMessageConverter<String> {

    private static final Logger log = LoggerFactory.getLogger(TextHttpMessageConverter.class);

    @Override
    public boolean canRead(Class<?> type, MediaType mt) {
        return mt != null && ("text".equalsIgnoreCase(mt.getType()) || (mt.getSubtype() != null && mt.getSubtype().contains("javascript")));
    }

    @Override
    protected boolean supports(Class<?> type) {
        //I don't believe this is actually used because we overrode canRead
        return true;
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @Override
    protected void writeInternal(String t, HttpOutputMessage hom) throws IOException, HttpMessageNotWritableException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected String readInternal(Class<? extends String> type, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return convertStreamToString(inputMessage.getBody());
    }

}
