package org.desktop.estol.skeleton.commons;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.desktop.estol.skeleton.debug.DebugUtilities;
/**
 * Opens and plays audio files on their own thread.
 * Can loop them, can modify the gain.
 * 
 * @author estol
 */
public class WavePlayer implements Runnable
{
    private final int BUFFER_SIZE = 192000;
    private File soundFile;
    private AudioInputStream audioStream;
    private AudioFormat audioFormat;
    private SourceDataLine sourceLine;
    private FloatControl gainControl;
    private String file;
    private boolean looped = false;
    
    
    /**
     * after initialization, plays the file defined in soundFile
     */
    private void startPlayback()
    {        
        // I know this is bad practice, but I won't make thousands of indents
        // to catch all exceptions near their individual occurence
        try
        {
            soundFile = new File(file);
            audioStream = AudioSystem.getAudioInputStream(soundFile);
            audioFormat = audioStream.getFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
            sourceLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceLine.open(audioFormat);
            sourceLine.start();
            gainControl = (FloatControl) sourceLine.getControl(FloatControl.Type.MASTER_GAIN);
            
            int nBytesRead = 0;
            byte[] abData = new byte[BUFFER_SIZE]; // in a nutshell: read the buffer full, write the buffer to the audio output
            while (nBytesRead != -1)               // repeat while there is more file to read.
            {
                nBytesRead = audioStream.read(abData, 0, abData.length);
                if (nBytesRead >= 0)
                {
                    sourceLine.write(abData, 0, nBytesRead);
                }
            }
            sourceLine.drain();
            sourceLine.close();
            if (looped)
            {
                startPlayback();
            }
        }
        catch (NullPointerException | UnsupportedAudioFileException | IOException | LineUnavailableException e)
        {
            DebugUtilities.addDebugMessage(e.getMessage());
            return;
        }
    }

    
    
    /**
     * Sets the gain, globally throughout of all WavePlayer instances.
     * 6.0206 max
     * -80.0 min
     * @param gain 
     */
    public void setVolume(float gain)
    {
        gainControl.setValue(gain);
    }
    
    /**
     * Not actual volume, but gain.
     * Retruns the current gain.
     * @return 
     */
    public float getVolume()
    {
        return gainControl.getValue();
    }
    
    /**
     * stops the playback duh
     */
    public void stopPlayback()
    {
        disableLooping();
        sourceLine.stop();
        sourceLine.flush();
        sourceLine.close();
    }
    
    /**
     * when called, sets the looped flag true, causing the startPlayback() method
     * to recursively call itself, until disableLooping is called.
     */
    public void setLooping()
    {
        looped = true;
    }
    
    /**
     * when called sets the looped flag false, causing the startPlayback method
     * to return when the playback is finished.
     */
    public void disableLooping()
    {
        looped = false;
    }
    
    /**
     * sets up the WavePlayer instance's file field with the parameter filename
     * and spawns a new thread.
     * @param filename 
     */
    public void playSound(String filename)
    {
        file = filename;
        new Thread(this).start();
    }
    
    /**
     * renames the thread, and starts the playback.
     */
    @Override
    public void run() {
        Thread.currentThread().setName("Wave player for " + file);
        startPlayback();
    }
}
