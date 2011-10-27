package net.slashie.utils;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageUtils {
	public static  BufferedImage createImage (String filename) throws IOException  {
		BufferedImage im =  ImageIO.read(new File(filename));
	    return im;
	}
	
	public static Image crearImagen(String filename, Component tracker) throws Exception{
    	//Debug.say("estoy cargando una imagen "+filename);
    	if (!FileUtil.fileExists(filename)){
    		Exception e = new Exception("Archivo Inexistente " +filename);
    		Debug.exitExceptionally(e);
    		throw e;
    	}
        Image image = Toolkit.getDefaultToolkit().getImage(filename);
        MediaTracker mediaTracker = new MediaTracker(tracker);
        mediaTracker.addImage(image, 0);
        try{
            mediaTracker.waitForID(0);
        }catch(InterruptedException ex){
        	System.out.println(ex.getMessage());
        }
        //Debug.exitMethod(image);
        return image;
    }
	
	public static BufferedImage createImage(String filename, int x, int y, int width, int height) throws IOException{
		BufferedImage tempImage = createImage(filename);
		return crearImagen(tempImage, x, y, width, height);
    }
	
	public static BufferedImage crearImagen(String filename, int x, int y, int width, int height) throws IOException{
		return createImage(filename, x, y, width, height);
    }

	public static BufferedImage crearImagen(BufferedImage tempImage, int x, int y, int width, int height) {
		return createImage(tempImage, x, y, width, height);
	}

	public static BufferedImage createImage(BufferedImage tempImage, int x, int y, int width, int height) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	    GraphicsConfiguration gc = ge.getDefaultScreenDevice( ).getDefaultConfiguration( );

	    int transparency = tempImage.getColorModel().getTransparency( );

	    BufferedImage ret =gc.createCompatibleImage(width,height,transparency);
        // create a graphics context
        Graphics2D retGC = ret.createGraphics();

        // copy image
        retGC.drawImage(tempImage,
                    0,0, width,height, x,y,x+width,y+height,null);
        retGC.dispose();
        return ret;
    }

	public static BufferedImage hFlip(BufferedImage image){
		AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
		tx.translate(0, -image.getHeight(null));
	    AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
	    return op.filter(image, null);
	}
	
	public static BufferedImage vFlip(BufferedImage image){
		AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
		tx.translate(-image.getWidth(null), 0);
	    AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
	    return op.filter(image, null);
	}
	
	public static BufferedImage rotate(BufferedImage bufferedImage, double radians){
		AffineTransform tx = new AffineTransform();
	    tx.rotate(radians, bufferedImage.getWidth()/2, bufferedImage.getHeight()/2);
	    AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
	    return op.filter(bufferedImage, null);
	}
	
	public static BufferedImage overlay(BufferedImage base, BufferedImage overlay, int offsetx, int offsety){
		BufferedImage ret = new BufferedImage(base.getWidth(),base.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D retGC = ret.createGraphics();
        //retGC.drawImage(base,offsetx, offsety, null);
        retGC.drawImage(base,0, 0, null);
        retGC.drawImage(overlay,offsetx, offsety, null);
        retGC.dispose();
        return ret;
	}

	
	public static BufferedImage createEmptyImage(int width, int height) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gs = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gs.getDefaultConfiguration();
		return gc.createCompatibleImage(width, height, Transparency.BITMASK);
	}
}
