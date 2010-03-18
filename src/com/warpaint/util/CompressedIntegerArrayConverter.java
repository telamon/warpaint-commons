/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.warpaint.util ;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.basic.ByteConverter;
import com.thoughtworks.xstream.core.util.Base64Encoder;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DeflaterOutputStream;

/**
 *
 * @author telamon
 */
public class CompressedIntegerArrayConverter implements Converter{
    private static final Base64Encoder base64 = new Base64Encoder();
    private static final ByteConverter byteConverter = new ByteConverter();

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext arg2) {
        final boolean array = source instanceof IntBuffer;
        int l = array?((IntBuffer)source).capacity():((int[])source).length;
        
        ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
        DeflaterOutputStream zipos = new DeflaterOutputStream(os);
        ByteBuffer conv = ByteBuffer.allocate(4);
        try {
            conv.putInt(l);
            zipos.write(conv.array());
            for(int p=0;p<l;p++){
                int i = array ? ((IntBuffer) source).get(p) : ((int[]) source)[p];
                conv.rewind();
                conv.putInt(i);
                zipos.write(conv.array());
            }
            zipos.finish();
        } catch (IOException ex) {
            Logger.getLogger(CompressedIntegerArrayConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
        writer.setValue(base64.encode(os.toByteArray()));
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(base64.decode(reader.getValue()));
            java.util.zip.InflaterInputStream zipin = new java.util.zip.InflaterInputStream(in);
            ByteBuffer conv = ByteBuffer.allocate(4);
            conv.order();
            zipin.read(conv.array());
            int l = conv.getInt();
            int[] buffer = new int[l];

            for(int i=0;i< buffer.length;i++){
                conv.rewind();
                for(int s=0; s<4;s++){
                    conv.put((byte)zipin.read());
                }
                conv.rewind();
                buffer[i] = conv.getInt();
            }
            return buffer;
        } catch (IOException ex) {
            Logger.getLogger(CompressedIntegerArrayConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new int[1];
    }

    @Override
     public boolean canConvert(Class type) {
        return (type.isArray() && type.getComponentType().equals(int.class));
    }

}
