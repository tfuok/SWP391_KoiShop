package com.example.demo.service;

import com.example.demo.entity.Certificate;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;

@Service
public class CertificatePdfGenerator {

    public File createCertificatePdf(Certificate certificate) throws Exception {
        String fileName = "certificate_" + certificate.getKoi().getId() + ".pdf";
        File pdfFile = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);

        // Initialize PdfWriter and PdfDocument
        PdfWriter pdfWriter = new PdfWriter(new FileOutputStream(pdfFile));
        PdfDocument pdfDocument = new PdfDocument(pdfWriter);
        Document document = new Document(pdfDocument);

        // Add certificate details
        document.add(new Paragraph("Certification of Breeding").setFontSize(24).setBold());
        document.add(new Paragraph("We hereby certify that the Koi shown in the photo was bred by the following breeder and sold by Kodama Koi Farm"));

        Table table = new Table(2);
        table.addCell("Variety:");
        table.addCell(certificate.getVariety());
        table.addCell("Breeder:");
        table.addCell(certificate.getBreeder());
        table.addCell("Born In:");
        table.addCell(String.valueOf(certificate.getBornIn()));
        table.addCell("Size:");
        table.addCell(String.valueOf(certificate.getSize()) + " cm");
        table.addCell("Issue Date:");
        table.addCell(certificate.getIssueDate().toString());

        document.add(table);

        // Add Koi image
        if (certificate.getImageUrl() != null) {
            Image koiImage = new Image(ImageDataFactory.create(certificate.getImageUrl()));
            koiImage.setAutoScale(true);
            document.add(koiImage);
        }

        document.close();

        return pdfFile;
    }
}
