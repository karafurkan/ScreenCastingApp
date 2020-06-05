package com.FurkanKara;


import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileSystemView;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;

public class VideoCreater {

    private static Dimension screenBounds;


    
    private static String homePath;

    public VideoCreater() {

    	setHomePath();
    	File file = new File(homePath + "/Desktop/photos");
    	file.mkdirs();
    	
    }

    public void setHomePath() {
    	
		FileSystemView filesys = FileSystemView.getFileSystemView();
		File[] roots = filesys.getRoots();
		homePath = filesys.getHomeDirectory().toString();
			
    }
    
    
    public int getImageNumber() {
    	File file = new File(homePath + "/Desktop/photos");
    	return file.list().length;
    }
    
    public void createVideo() {
    	
		SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss");
		Date date = new Date(System.currentTimeMillis());
		String fileName = "lecture-" + formatter.format(date) + ".mp4";
		
    	
        final IMediaWriter writer = ToolFactory.makeWriter(homePath + "/Desktop/" + fileName);
        screenBounds = Toolkit.getDefaultToolkit().getScreenSize();
        writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4,
                screenBounds.width / 2, screenBounds.height / 2);
        long startTime = System.nanoTime();

        
        int imageNumber = getImageNumber();
        
        for (int index = 0; index < imageNumber; index++) {

            BufferedImage bgrScreen = getVideoImage(index+1);

            bgrScreen = convertToType(bgrScreen, BufferedImage.TYPE_3BYTE_BGR);

            writer.encodeVideo(0, bgrScreen, System.nanoTime() - startTime,
                    TimeUnit.NANOSECONDS);
            try {
                Thread.sleep((long) (100));
            } catch (InterruptedException e) {
            }
        }
        writer.close();
		
    	
    }
    
    private static BufferedImage getVideoImage(int indexVideo) {

        File imgLoc = new File(homePath + "/Desktop/photos/" + indexVideo + ".png");
        System.out.println(imgLoc.toString());
        BufferedImage img = null;
        try {
            img = ImageIO.read(imgLoc);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(imgLoc.getName());

        return img;
    }

    public static BufferedImage convertToType(BufferedImage sourceImage,
            int targetType) {

        BufferedImage image;

        // if the source image is already the target type, return the source
        // image
        if (sourceImage.getType() == targetType) {
            image = sourceImage;
        }
        // otherwise create a new image of the target type and draw the new
        // image
        else {
            image = new BufferedImage(sourceImage.getWidth(),
                    sourceImage.getHeight(), targetType);
            image.getGraphics().drawImage(sourceImage, 0, 0, null);
        }

        return image;

    }

}