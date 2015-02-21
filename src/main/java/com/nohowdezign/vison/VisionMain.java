package com.nohowdezign.vison;

import boofcv.alg.color.ColorHsv;
import boofcv.alg.color.ColorYuv;
import boofcv.alg.feature.detect.edge.CannyEdge;
import boofcv.alg.feature.detect.edge.EdgeContour;
import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.filter.binary.GThresholdImageOps;
import boofcv.alg.misc.GPixelMath;
import boofcv.alg.misc.ImageStatistics;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.factory.feature.detect.edge.FactoryEdgeDetectors;
import boofcv.gui.ListDisplayPanel;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.ConnectRule;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSInt16;
import boofcv.struct.image.ImageUInt8;
import boofcv.struct.image.MultiSpectral;

import java.awt.image.BufferedImage;
import java.util.List;

public class VisionMain {
	private String image = "./SampleImages/image20.jpg";
	
	public static void main( String args[] ) {
		VisionMain main = new VisionMain();
		main.cannyLine();
	}
	
	private void cannyLine() {
		BufferedImage image = UtilImageIO.loadImage(this.image);
		 
		ImageUInt8 gray = ConvertBufferedImage.convertFrom(image,(ImageUInt8)null);
		ImageUInt8 edgeImage = new ImageUInt8(gray.width,gray.height);
 
		// Create a canny edge detector which will dynamically compute the threshold based on maximum edge intensity
		// It has also been configured to save the trace as a graph.  This is the graph created while performing
		// hysteresis thresholding.
		CannyEdge<ImageUInt8,ImageSInt16> canny = FactoryEdgeDetectors.canny(2,true, true, ImageUInt8.class, ImageSInt16.class);
 
		// The edge image is actually an optional parameter.  If you don't need it just pass in null
		canny.process(gray,0.1f,0.3f,edgeImage);
 
		// First get the contour created by canny
		List<EdgeContour> edgeContours = canny.getContours();
		// The 'edgeContours' is a tree graph that can be difficult to process.  An alternative is to extract
		// the contours from the binary image, which will produce a single loop for each connected cluster of pixels.
		// Note that you are only interested in external contours.
		List<Contour> contours = BinaryImageOps.contour(edgeImage, ConnectRule.EIGHT, null);
 
		// display the results
		BufferedImage visualBinary = VisualizeBinaryData.renderBinary(edgeImage, null);
		BufferedImage visualCannyContour = VisualizeBinaryData.renderContours(edgeContours,null,
				gray.width,gray.height,null);
		BufferedImage visualEdgeContour = VisualizeBinaryData.renderExternal(contours, null,
				gray.width, gray.height, null);
 
		ShowImages.showWindow(visualBinary,"Binary Edges from Canny");
		ShowImages.showWindow(visualCannyContour,"Canny Trace Graph");
		ShowImages.showWindow(visualEdgeContour,"Contour from Canny Binary");
	}
	
	private void hsv() {
		BufferedImage image = UtilImageIO.loadImage(this.image);
		 
		// Convert input image into a BoofCV RGB image
		MultiSpectral<ImageFloat32> rgb = ConvertBufferedImage.convertFromMulti(image, null,true, ImageFloat32.class);
 
		//---- convert RGB image into different color formats
		MultiSpectral<ImageFloat32> hsv = new MultiSpectral<ImageFloat32>(ImageFloat32.class,rgb.width,rgb.height,3);
		ColorHsv.rgbToHsv_F32(rgb, hsv);
 
		MultiSpectral<ImageFloat32> yuv = new MultiSpectral<ImageFloat32>(ImageFloat32.class,rgb.width,rgb.height,3);
		ColorYuv.yuvToRgb_F32(rgb, yuv);
 
		//---- Convert individual pixels into different formats
		float[] pixelHsv = new float[3];
		ColorHsv.rgbToHsv(10,50.6f,120,pixelHsv);
		System.out.printf("Found RGB->HSV = %5.2f %5.3f %5.1f\n",pixelHsv[0],pixelHsv[1],pixelHsv[2]);
 
		float[] pixelRgb = new float[3];
		ColorHsv.hsvToRgb(pixelHsv[0],pixelHsv[1],pixelHsv[2],pixelRgb);
		System.out.printf("Found HSV->RGB = %5.1f %5.1f %5.1f expected 10 50.6 120\n",
				pixelRgb[0],pixelRgb[1],pixelRgb[2]);
 
		float[] pixelYuv = new float[3];
		ColorYuv.rgbToYuv(10,50.6f,120,pixelYuv);
		System.out.printf("Found RGB->YUV = %5.1f %5.1f %5.1f\n",pixelYuv[0],pixelYuv[1],pixelYuv[2]);
 
		ColorYuv.yuvToRgb(pixelYuv[0],pixelYuv[1],pixelYuv[2],pixelRgb);
		System.out.printf("Found YUV->RGB = %5.1f %5.1f %5.1f expected 10 50.6 120\n",
				pixelRgb[0],pixelRgb[1],pixelRgb[2]);
	}
	
	private void convertToGray() {
		BufferedImage input = UtilImageIO.loadImage(this.image);
		
		// convert the BufferedImage into a MultiSpectral
		MultiSpectral<ImageUInt8> image = ConvertBufferedImage.convertFromMulti(input,null,true,ImageUInt8.class);
 
		ImageUInt8 gray = new ImageUInt8( image.width,image.height);
 
		// creates a gray scale image by averaging intensity value across pixels
		GPixelMath.averageBand(image, gray);
		BufferedImage outputAve = ConvertBufferedImage.convertTo(gray,null);
 
		// create an output image just from the first band
		BufferedImage outputBand0 = ConvertBufferedImage.convertTo(image.getBand(0),null);
 
		ShowImages.showWindow(outputAve,"Average");
		ShowImages.showWindow(outputBand0,"Band 0");
	}
	
	public static void threshold( String imageName ) {
		BufferedImage image = UtilImageIO.loadImage(imageName);
 
		// convert into a usable format
		ImageFloat32 input = ConvertBufferedImage.convertFromSingle(image, null, ImageFloat32.class);
		ImageUInt8 binary = new ImageUInt8(input.width,input.height);
 
		// Display multiple images in the same window
		ListDisplayPanel gui = new ListDisplayPanel();
 
		// Global Methods
		GThresholdImageOps.threshold(input, binary, ImageStatistics.mean(input), true);
		gui.addImage(VisualizeBinaryData.renderBinary(binary, null),"Global: Mean");
		GThresholdImageOps.threshold(input, binary, GThresholdImageOps.computeOtsu(input, 0, 256), true);
		gui.addImage(VisualizeBinaryData.renderBinary(binary, null),"Global: Otsu");
		GThresholdImageOps.threshold(input, binary, GThresholdImageOps.computeEntropy(input, 0, 256), true);
		gui.addImage(VisualizeBinaryData.renderBinary(binary, null),"Global: Entropy");
 
		// Local method
		GThresholdImageOps.adaptiveSquare(input, binary, 28, 0, true, null, null);
		gui.addImage(VisualizeBinaryData.renderBinary(binary, null),"Adaptive: Square");
		GThresholdImageOps.adaptiveGaussian(input, binary, 42, 0, true, null, null);
		gui.addImage(VisualizeBinaryData.renderBinary(binary, null),"Adaptive: Gaussian");
		GThresholdImageOps.adaptiveSauvola(input, binary, 5, 0.30f, true);
		gui.addImage(VisualizeBinaryData.renderBinary(binary, null),"Adaptive: Sauvola");
 
		// Sauvola is tuned for text image.  Change radius to make it run better in others.
 
		// Show the image image for reference
		gui.addImage(ConvertBufferedImage.convertTo(input,null),"Input Image");
 
		String fileName =  imageName.substring(imageName.lastIndexOf('/')+1);
		ShowImages.showWindow(gui,fileName);
	}

}
