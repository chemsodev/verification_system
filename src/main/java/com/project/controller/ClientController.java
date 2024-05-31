package com.project.controller;

import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.project.model.ResponseData;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.core.io.ClassPathResource;
import org.apache.commons.io.FileUtils;
import javax.imageio.ImageIO;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
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

import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ClientController implements ErrorController {
    @GetMapping("")
    public ModelAndView home() {
        return new ModelAndView("addimage");
    }
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseData> addImagePost(@RequestParam("image") MultipartFile file) throws IOException {
        ResponseData responseData = processImage(file);
        return new ResponseEntity<>(responseData, responseData.getStatus() == HttpStatus.OK.value() ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }


    private ResponseData processImage(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();

        BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();

        try {
            Result result = reader.decode(bitmap);
            String qrCodeData = result.getText();
            if (!qrCodeData.equals("https://progres.mesrs.dz/api/infos/checkInscription/undefined")) {
                return new ResponseData("Invalid QR code data.", HttpStatus.BAD_REQUEST.value(), null);
            }

            ITesseract tesseract = new Tesseract();

            // Load tessdata directory from resources
            ClassPathResource resource = new ClassPathResource("tessdata");
            InputStream inputStream = resource.getInputStream();
            File tempDir = Files.createTempDirectory("tessdata").toFile();

            // Copy tessdata directory to a temporary directory
            FileUtils.copyDirectory(resource.getFile(), tempDir);

            // Set the temporary directory containing tessdata as the data path for Tesseract
            tesseract.setDatapath(tempDir.getAbsolutePath());
            tesseract.setLanguage("eng");
            BufferedImage rotatedImage;
            String matricule = null;
            for (int i = 1; i <= 4; i++) {
                rotatedImage = rotateImage(bufferedImage, 90 * i);
                String ocrResult = tesseract.doOCR(rotatedImage);
                System.out.println("ocrResult: ");
                System.out.println(ocrResult);
                System.out.println("----------------------------");
                matricule = extractMatricule(ocrResult);
                if (matricule != null) {
                    System.out.println("matricule:" + matricule);
                    break;
                } else {
                    System.out.println("matricule not found, trying again");
                }
            }
            if (matricule != null) {
                return new ResponseData("Student confirmed.", HttpStatus.OK.value(), matricule);
            } else {
                return new ResponseData("Matricule not found in OCR results.", HttpStatus.NOT_FOUND.value(), null);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
            return new ResponseData("QR code not found in the uploaded image.", HttpStatus.NOT_FOUND.value(), null);
        } catch (ChecksumException | FormatException e) {
            e.printStackTrace();
            return new ResponseData("Invalid QR code format.", HttpStatus.BAD_REQUEST.value(), null);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return new ResponseData("Invalid QR code data.", HttpStatus.BAD_REQUEST.value(), null);
        } catch (TesseractException e) {
            e.printStackTrace();
            return new ResponseData("OCR error occurred.", HttpStatus.INTERNAL_SERVER_ERROR.value(), null);
        }
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

    private String extractMatricule(String ocrResult) {
        Pattern pattern = Pattern.compile("UN16042023(\\d+)");
        Matcher matcher = pattern.matcher(ocrResult);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }
    @RequestMapping("/error")
    public String handleError() {
        // Forward to the error page
        return "error";
    }
}
