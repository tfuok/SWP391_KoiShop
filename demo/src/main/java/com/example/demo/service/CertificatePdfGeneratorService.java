package com.example.demo.service;

import com.example.demo.entity.Certificate;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.itextpdf.html2pdf.HtmlConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.UUID;

@Service
public class CertificatePdfGeneratorService {

    private final ResourceLoader resourceLoader;


    @Autowired
    public CertificatePdfGeneratorService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public String generateHtml(Certificate certificate) {
        int bornInDate = certificate.getKoi().getBornYear();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        String formattedIssueDate = dateFormatter.format(certificate.getIssueDate());
        String Koiid = Long.toString(certificate.getKoi().getId());

        Resource resource = resourceLoader.getResource("classpath:image/background.png");
        String backgroundImagePath;

        try {
            backgroundImagePath = resource.getFile().getPath();
        } catch (Exception e) {
            e.printStackTrace();
            backgroundImagePath = "";
        }

        Resource fontResource = resourceLoader.getResource("classpath:static/NotoSansJP-VariableFont_wght.ttf");
        String fontFilePath;

        try {
            fontFilePath = fontResource.getFile().getPath();
        } catch (Exception e) {
            e.printStackTrace();
            fontFilePath = "";
        }

        return "<!DOCTYPE html>" +
                "<html lang=\"en\">" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "    <title>Certificate of Breeding</title>" +
                "    <link href=\"https://fonts.googleapis.com/css2?family=Noto+Sans+JP&display=swap\" rel=\"stylesheet\">" +
                "    <style>" +
                "        body {" +
                "            font-family: 'Noto Sans JP', Arial, sans-serif;" +
                "            margin: 0;" +
                "            padding: 0;" +
                "            text-align: center;" +
                "        }" +
                "        .container {" +
                "            width: 100%;" +
                "            max-width: 800px;" +
                "            margin: 0 auto;" +
                "            padding: 20px;" +
                "        }" +
                "        h1 {" +
                "            font-size: 24px;" +
                "            margin-bottom: 20px;" +
                "        }" +
                "        .content {" +
                "            display: flex;" +
                "            align-items: center;" +
                "            justify-content: flex-start;" +
                "        }" +
                "        .koi-image {" +
                "            width: 150px;" +
                "            height: auto;" +
                "            margin-right: 20px;" +
                "        }" +
                "        .details-table {" +
                "            margin-left: 20px;" +
                "            text-align: left;" +
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
                "        <div class=\"content\">" +
                "            <img class=\"koi-image\" src=\"" + certificate.getKoi().getImages() + "\" alt=\"Koi Fish Image\">" +
                "            <table class=\"details-table\">" +
                "                <tr>" +
                "                    <td><strong>Variety:</strong></td>" +
                "                    <td>" + certificate.getKoi().getName() + "</td>" +
                "                </tr>" +
                "                <tr>" +
                "                    <td><strong>Breeder:</strong></td>" +
                "                    <td>" + certificate.getKoi().getVendor() + "</td>" +
                "                </tr>" +
                "                <tr>" +
                "                    <td><strong>Born In:</strong></td>" +
                "                    <td>" + bornInDate + "</td>" +
                "                </tr>" +
                "                <tr>" +
                "                    <td><strong>Size:</strong></td>" +
                "                    <td>" + certificate.getKoi().getSize() + " cm</td>" +
                "                </tr>" +
                "                <tr>" +
                "                    <td><strong>Id:</strong></td>" +
                "                    <td>" + Koiid + "</td>" +
                "                </tr>" +
                "                <tr>" +
                "                    <td><strong>Date of Issue:</strong></td>" +
                "                    <td>" + formattedIssueDate + "</td>" +
                "                </tr>" +
                "                <tr>" +
                "                    <td><strong>Signature:</strong></td>" +
                "                    <td style=\"font-family: 'Noto Sans JP';\">樹神太郎</td>" +
                "                </tr>" +
                "            </table>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";




    }

    public byte[] createCertificatePdf(Certificate certificate) throws Exception {
    String htmlContent = generateHtml(certificate);
    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
        HtmlConverter.convertToPdf(htmlContent, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
}

    public class FirebaseInitializer {

        public static void initializeFirebase() throws IOException {
            ClassLoader classLoader = FirebaseInitializer.class.getClassLoader();
            InputStream serviceAccount = classLoader.getResourceAsStream("firebase-admin.json");

            if (serviceAccount == null) {
                throw new IllegalArgumentException("Không tìm thấy tệp firebase-admin.json");
            }
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket("koimanagement-cd9bd.appspot.com")
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        }
    }
    private String uploadFileToFirebaseStorage(MultipartFile file, String firebasePath) throws IOException {
        FirebaseInitializer.initializeFirebase();
        String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
        String contentType = file.getContentType();
        BlobInfo blobInfo = BlobInfo.newBuilder("koimanagement-cd9bd.appspot.com", firebasePath)
                .setContentType(contentType)
                .build();
        Storage storage = StorageOptions.getDefaultInstance().getService();
        storage.create(blobInfo, file.getBytes());
        return String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                "koimanagement-cd9bd.appspot.com",
                firebasePath.replace("/", "%2F")); // Encode '/' to '%2F' in the URL
    }

    private void savePdfLocally(String filePath, byte[] pdfBytes) throws Exception {
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(pdfBytes);
        }
    }



}
