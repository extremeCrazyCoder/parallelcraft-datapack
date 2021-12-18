package com.parallelcraft.datapack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * @author extremeCrazyCoder
 */
public class StreamMapper implements Runnable {
    private BufferedReader source;
    private OutputStream target;
    private boolean closeAfterwards;
    
    public StreamMapper(InputStream source, OutputStream target, boolean closeAfterwards) {
        this.source = new BufferedReader(new InputStreamReader(source));
        this.target = target;
        this.closeAfterwards = closeAfterwards;
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            String line;
            while((line = source.readLine()) != null) {
                line+= System.lineSeparator();
                target.write(line.getBytes());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if(closeAfterwards) {
                try {
                    target.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                try {
                    source.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
}
