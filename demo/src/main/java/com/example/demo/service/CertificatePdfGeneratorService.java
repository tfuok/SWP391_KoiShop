package com.example.demo.service;

import com.example.demo.entity.Certificate;
import com.itextpdf.html2pdf.HtmlConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;

import static java.lang.Long.parseLong;

@Service
public class CertificatePdfGeneratorService {

    private final ResourceLoader resourceLoader;

    @Autowired
    public CertificatePdfGeneratorService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public String generateHtml(Certificate certificate) {
        int bornInDate = certificate.getKoi().getBornYear(); // Assuming this is an integer year
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        String formattedIssueDate = dateFormatter.format(certificate.getIssueDate()); // Format the issue date
        String Koiid = Long.toString(certificate.getKoi().getId());

        // Load image as a resource
        Resource resource = resourceLoader.getResource("classpath:image/background.png");
        String backgroundImagePath;

        try {
            backgroundImagePath = resource.getFile().getPath(); // Convert resource to path
        } catch (Exception e) {
            e.printStackTrace();
            backgroundImagePath = ""; // Fallback if the image is not found
        }
        //
        Resource fontResource = resourceLoader.getResource("classpath:static/NotoSansJP-Regular.ttf");
        String fontFilePath;

        try {
            fontFilePath = fontResource.getFile().getPath(); // Convert resource to path
        } catch (Exception e) {
            e.printStackTrace();
            fontFilePath = ""; // Fallback if the font is not found
        }
        return "<!DOCTYPE html>" +
                "<html lang=\"en\">" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "    <title>Certificate of Breeding</title>" +
                "    <style>" +
                "        @font-face {" +
                "            font-family: 'Noto Sans JP';" +
                "            src: url('fonts/NotoSansJP-Regular.ttf');" + // Ensure the path is correct
                "        }" +
                "        body {" +
                "            font-family: 'Noto Sans JP', Arial, sans-serif;" + // Use the custom font
                "            margin: 0;" +
                "            padding: 0;" +
                "            background: url('" + backgroundImagePath + "') no-repeat center center;" +
                "            background-size: cover;" +
                "            text-align: center;" +
                "        }" +
                "        .container {" +
                "            width: 100%;" +
                "            max-width: 800px;" +
                "            margin: 0 auto;" +
                "            padding: 20px;" +
                "            background: rgba(255, 255, 255, 0.9);" +
                "        }" +
                "        h1 {" +
                "            font-size: 24px;" +
                "            margin-bottom: 20px;" +
                "        }" +
                "        .koi-image {" +
                "            width: 150px;" +
                "            height: auto;" +
                "            margin-right: 20px;" +
                "            float: left;" +
                "        }" +
                "        .details-table {" +
                "            margin: 0 auto;" +
                "            text-align: left;" +
                "            clear: both;" +
                "        }" +
                "        .details-table td {" +
                "            padding: 5px 10px;" +
                "            font-size: 14px;" +
                "        }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class=\"container\">" +
                "        <h1>Certification of Breeding</h1>" +
                "        <p>We hereby certify that the Koi shown in the photo was bred by the following breeder and sold by Kodama Koi Farm</p>" +
                "        <div style=\"display: flex; align-items: center;\">" +
                "            <img class=\"koi-image\" src='" + certificate.getKoi().getImages() + "' alt=\"Koi Fish Image\">" +
                "            <div>" +
                "                <table class=\"details-table\">" +
                "                    <tr>" +
                "                        <td><strong>Variety:</strong></td>" +
                "                        <td>" + certificate.getKoi().getName() + "</td>" +
                "                    </tr>" +
                "                    <tr>" +
                "                        <td><strong>Breeder:</strong></td>" +
                "                        <td>" + certificate.getKoi().getVendor() + "</td>" +
                "                    </tr>" +
                "                    <tr>" +
                "                        <td><strong>Born In:</strong></td>" +
                "                        <td>" + bornInDate + "</td>" +
                "                    </tr>" +
                "                    <tr>" +
                "                        <td><strong>Size:</strong></td>" +
                "                        <td>" + certificate.getKoi().getSize() + " cm</td>" +
                "                    </tr>" +
                "                    <tr>" +
                "                        <td><strong>Id:</strong></td>" +
                "                        <td>" + Koiid + "</td>" +
                "                    </tr>" +
                "                    <tr>" +
                "                        <td><strong>Date of Issue:</strong></td>" +
                "                        <td>" + formattedIssueDate + "</td>" +
                "                    </tr>" +
                "                    <tr>" +
                "                        <td><strong>Signature:</strong></td>" +
                "                        <td style=\"font-family: 'Noto Sans JP';\">樹神太郎</td>" + // Use custom font for signature
                "                    </tr>" +
                "                </table>" +
                "            </div>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";

    }

    public File createCertificatePdf(Certificate certificate) throws Exception {
        // Generate HTML content
        String htmlContent = generateHtml(certificate);

        // Define the file path for the PDF
        String outputPath = "D:/Download/Hoc/TestPDF/" + "Koi "+ certificate.getKoi().getId() + " Certificate.pdf";
        File pdfFile = new File(outputPath);

        // Convert HTML to PDF and save it
        HtmlConverter.convertToPdf(htmlContent, new FileOutputStream(pdfFile));

        return pdfFile;
    }
}
