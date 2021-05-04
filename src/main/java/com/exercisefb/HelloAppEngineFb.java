package com.exercisefb;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;

@WebServlet(
    name = "HelloAppEngineFb",
    urlPatterns = {"/hello"}
)
public class HelloAppEngineFb extends HttpServlet {

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		
		final String basePath = "C:/Users/anupo/Documents/MS/Web Systems/GAE1/ExerciseFB/src/main/webapp/FbExample/";
		//final String basePath = "/tmp/";
		OutputStream out = null;
		InputStream filecontent = null;
		final String path = System.getProperty("user.dir")+"/";
		String fileName = "input.jpg";
		
		// Saving selected FB image to local
		try (BufferedInputStream inputStream = new BufferedInputStream(new URL(request.getParameter("hiddenField")).openStream());
			FileOutputStream fileOS = new FileOutputStream(basePath + fileName)) {
			byte data[] = new byte[1024];
			int byteContent;
			while ((byteContent = inputStream.read(data, 0, 1024)) != -1) {
				fileOS.write(data, 0, byteContent);
			}
		} catch (IOException e) {
			response.getOutputStream().println("<br/> ERROR: " + e.getMessage());
		}
				
		try {
			// If uploaded file is png, convert it to jpg
			if(fileName.contains(".png") || fileName.contains(".PNG")) {
				System.out.println(fileName);
				String[] fileNameParts = fileName.split("\\.");
				ConvertPngToJpg(basePath, fileNameParts[0],fileNameParts[1]);
				fileName = fileNameParts[0] +".jpg";
			}
			
	        LinkedHashMap<String,Float> userMap = new LinkedHashMap<>();  //Order is maintained in linkedhashmap
	        DetectProperties.detectProperties(basePath+ fileName,userMap); // Calculating user image properties
	        
	        // Choosing the file based on dominant color
	        float maxVal = userMap.get("red");
	        String mergeFile = "red.jpg";
	        if(userMap.get("green") > maxVal) {
	        	mergeFile = "green.jpg";
	        	maxVal = userMap.get("green");
	        }
	        if(userMap.get("blue") > maxVal) {
	        	mergeFile = "blue.jpg";
	        }
	        
	        // Resizing both images
	        int scaledWidth = 521;
	        int scaledHeight = 384;
	        System.out.println(basePath+ fileName);
	        resize(basePath+ fileName,basePath+ fileName, scaledWidth, scaledHeight);
	        resize(path + "ImageMapping/" + mergeFile, basePath + mergeFile, scaledWidth, scaledHeight);

	        // Merging user image and selected image
	        int type;
	        int chunkWidth, chunkHeight;
	        BufferedImage[] buffImages = new BufferedImage[2];
	        buffImages[0] = ImageIO.read(new File(basePath + fileName));
	        buffImages[1] = ImageIO.read(new File(basePath + mergeFile));
	        type = buffImages[0].getType();
	        chunkWidth = buffImages[0].getWidth();
	        chunkHeight = buffImages[0].getHeight();
	        
	        //Initializing the final image
	        BufferedImage finalImg = new BufferedImage(chunkWidth*2, chunkHeight, type);
	        
	        finalImg.createGraphics().drawImage(buffImages[0],0,0, null);
	        finalImg.createGraphics().drawImage(buffImages[1],chunkWidth,0, null);
	        ImageIO.write(finalImg, "jpg", new File(basePath + "finalImg.jpg"));
	        
	        response.setContentType("image/jpg");  
	        ServletOutputStream sout;   //taking final image to servlet output stream
	        sout = response.getOutputStream();  
	        FileInputStream fin = new FileInputStream(basePath + "finalImg.jpg");  
	        
	        
	        BufferedInputStream bin = new BufferedInputStream(fin);  
	        BufferedOutputStream bout = new BufferedOutputStream(sout);  
	        int ch =0; ;  
	        while((ch=bin.read())!=-1)  
	        {  
	        bout.write(ch);  
	        }  
	        bin.close();  
	        fin.close();  
	        bout.close();  	
	        sout.close();  

	        //response.getOutputStream().println("<p>Thanks! Here's the image you uploaded:</p>");
			//response.getOutputStream().println("<img src=\""  + "/tmp/finalImg.jpg" +"?r="+Math.random() + "\" />");
			//response.getOutputStream().println("<p>Upload another image <a href=\"http://localhost:8080/index.html\">here</a>.</p>");
			
		} catch (Exception fne) {
			response.getOutputStream().println("<br/> ERROR: " + fne.getMessage());

	    } finally {
	        if (out != null) {
	            out.close();
	        }
	        if (filecontent != null) {
	            filecontent.close();
	        }
	        
	    }
	}
	
	public static void resize(String inputImagePath,
            String outputImagePath, double percent) throws IOException {
        File inputFile = new File(inputImagePath);
        BufferedImage inputImage = ImageIO.read(inputFile);
        int scaledWidth = (int) (inputImage.getWidth() * percent);
        int scaledHeight = (int) (inputImage.getHeight() * percent);
        resize(inputImagePath, outputImagePath, scaledWidth, scaledHeight);
    }
	
	public static void resize(String inputImagePath,
            String outputImagePath, int scaledWidth, int scaledHeight)
            throws IOException {
        // reads input image
        File inputFile = new File(inputImagePath);
        BufferedImage inputImage = ImageIO.read(inputFile);
 
        // creates output image
        BufferedImage outputImage = new BufferedImage(scaledWidth,
                scaledHeight, inputImage.getType());
 
        // scales the input image to the output image
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();
 
        // extracts extension of output file
        String formatName = outputImagePath.substring(outputImagePath
                .lastIndexOf(".") + 1);
 
        // writes to output file
        ImageIO.write(outputImage, formatName, new File(outputImagePath));
    }
	
	public void ConvertPngToJpg(String path, String name, String extension) throws Exception {
	        
		System.out.println(path + name +"."+extension);
		Path source = Paths.get(path + name +"."+extension);
	        Path target = Paths.get(path + name +".jpg");

	        BufferedImage originalImage = ImageIO.read(source.toFile());

	        // jpg needs BufferedImage.TYPE_INT_RGB
	        // png needs BufferedImage.TYPE_INT_ARGB

	        // create a blank, RGB, same width and height
	        BufferedImage newBufferedImage = new BufferedImage(
	                originalImage.getWidth(),
	                originalImage.getHeight(),
	                BufferedImage.TYPE_INT_RGB);

	        // draw a white background and puts the originalImage on it.
	        newBufferedImage.createGraphics()
	                .drawImage(originalImage,
	                        0,
	                        0,
	                        Color.WHITE,
	                        null);

	        // save an image
	        ImageIO.write(newBufferedImage, "jpg", target.toFile());
	}

	
}