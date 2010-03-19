/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.warpaint.util.persistence ;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.basic.ByteConverter;
import com.thoughtworks.xstream.core.util.Base64Encoder;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DeflaterOutputStream;

/**
 *
 * @author telamon
 */
public class CompressedByteArrayConverter implements Converter{
    private static final Base64Encoder base64 = new Base64Encoder();
    private static final ByteConverter byteConverter = new ByteConverter();

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext arg2) {
        
        ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
        DeflaterOutputStream zipos = new DeflaterOutputStream(os);
        try {
            zipos.write((byte[])source);
            zipos.finish();
        } catch (IOException ex) {
            Logger.getLogger(CompressedByteArrayConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
        writer.setValue(base64.encode(os.toByteArray()));
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            java.util.zip.InflaterOutputStream gz = new java.util.zip.InflaterOutputStream(out);
            gz.write(base64.decode(reader.getValue()));
            gz.flush();
            return out.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(CompressedByteArrayConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new int[1];
    }

    @Override
    public boolean canConvert(Class type) {
        return (type.isArray() && type.getComponentType().equals(byte.class));
    }

}
