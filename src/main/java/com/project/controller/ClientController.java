package com.project.controller;

import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.project.model.Image;
import com.project.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.imageio.ImageIO;
import javax.sql.rowset.serial.SerialException;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import java.util.regex.*;
@Controller
public class ClientController {
    @Autowired
    private ImageService imageService;

    @GetMapping("/ping")
    @ResponseBody
    public String helloWorld() {
        return "Hello World!";
    }

    // Display image
    @GetMapping("/display")
    public ResponseEntity<byte[]> displayImage(@RequestParam("id") long id) throws IOException, SQLException {
        Image image = imageService.viewById(id);
        byte[] imageBytes = image.getImage().getBytes(1, (int) image.getImage().length());
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(imageBytes);
    }

    // View all images
    @GetMapping("/")
    public ModelAndView home() {
        ModelAndView mv = new ModelAndView("index");
        List<Image> imageList = imageService.viewAll();
        mv.addObject("imageList", imageList);
        return mv;
    }

    // Add image - GET
    @GetMapping("/add")
    public ModelAndView addImage() {
        return new ModelAndView("addimage");
    }

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ModelAndView addImagePost(@RequestParam("image") MultipartFile file) throws IOException, SerialException, SQLException {
        ModelAndView modelAndView = new ModelAndView("addimage");

        byte[] bytes = file.getBytes();
        Blob blob = new javax.sql.rowset.serial.SerialBlob(bytes);

        // Perform QR code detection on the uploaded image
        BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();

        try {
            Result result = reader.decode(bitmap);
            String qrCodeData = result.getText();
            int qrCodeX = (int) result.getResultPoints()[0].getX();
            int qrCodeY = (int) result.getResultPoints()[0].getY();

            // Check if the QR code data matches the expected value
            if (!qrCodeData.equals("https://progres.mesrs.dz/api/infos/checkInscription/undefined")) {
                throw new IllegalArgumentException("Invalid QR code data.");
            }

            // Perform OCR on the image
            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata"); // Set the path to the tessdata directory
            tesseract.setLanguage("eng"); // Set the language for OCR (e.g., "eng" for English)

            // Rotate image if needed
            BufferedImage rotatedImage = rotateImage(bufferedImage, 90);

            String ocrResult = tesseract.doOCR(rotatedImage);
            System.out.println(ocrResult);

            Long matricule =Long.valueOf(extractMatricule(ocrResult));
            System.out.println(matricule);

           // Save the image, QR code data, and extracted information to the database
            Image image = new Image();
            image.setImage(blob);
            image.setQrCodeData(qrCodeData);
            image.setQrCodeX(qrCodeX);
            image.setQrCodeY(qrCodeY);
            image.setMatricule(matricule);
            imageService.create(image);
            modelAndView.addObject("successMessage", "Student added successfully.");
        } catch (NotFoundException e) {
            e.printStackTrace();
            modelAndView.addObject("errorMessage", "QR code not found in the uploaded image.");
        } catch (ChecksumException | FormatException e) {
            e.printStackTrace();
            modelAndView.addObject("errorMessage", "Invalid QR code format.");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            modelAndView.addObject("errorMessage", "Invalid QR code data.");
        } catch (TesseractException e) {
            e.printStackTrace();
            modelAndView.addObject("errorMessage", "OCR error occurred.");
        }

        return modelAndView;
    }

    private BufferedImage rotateImage(BufferedImage image, int angle) {
        double radians = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));
        int w = image.getWidth();
        int h = image.getHeight();
        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, image.getType());
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2, (newHeight - h) / 2);

        int x = w / 2;
        int y = h / 2;

        at.rotate(radians, x, y);

        AffineTransformOp rotateOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        rotateOp.filter(image, rotated);
        return rotated;
    }


    // Extract the matricule from the vertically oriented OCR result
    private String extractMatricule(String ocrResult) {
        Pattern pattern = Pattern.compile("UN16042023(\\d+)"); // Match "UN16042023" followed by one or more digits
        Matcher matcher = pattern.matcher(ocrResult);
        if (matcher.find()) {
            return matcher.group(1); // Return the captured digits
        }
        return "Not Found";
    }
}
