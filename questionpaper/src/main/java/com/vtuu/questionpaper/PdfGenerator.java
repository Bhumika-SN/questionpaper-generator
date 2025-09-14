package com.vtuu.questionpaper.util;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class PdfGenerator {

    public byte[] generateVTUPaperFormatted(
            Map<String, Map<String, List<String>>> paper,
            String sem,
            String monthYear,
            String subject,
            String subjectCode
    ) throws IOException {

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(doc, page);
            float margin = 50;
            float y = page.getMediaBox().getHeight() - 70;

            // CBCS Scheme Header
            centerText(cs, page, "CBCS SCHEME", y, PDType1Font.HELVETICA_BOLD, 18);

            // USN Boxes
            y -= 30;
            cs.setLineWidth(0.5f);
            float boxSize = 15;
            float startX = margin;
            for (int i = 0; i < 10; i++) {
                cs.addRect(startX + (i * boxSize), y, boxSize, boxSize);
                cs.stroke();
            }

            // Subject Code (top right)
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
            cs.newLineAtOffset(page.getMediaBox().getWidth() - 120, y + 5);
            cs.showText(subjectCode);
            cs.endText();

            // Exam Details (centered)
            y -= 50;
            centerText(cs, page, sem + " B.E. Degree Examination, " + monthYear, y,
                    PDType1Font.HELVETICA_BOLD, 12);

            y -= 25;
            centerText(cs, page, subject, y, PDType1Font.HELVETICA_BOLD, 12);

            y -= 25;
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 12);
            cs.newLineAtOffset(margin, y);
            cs.showText("Time: 3 hrs");
            cs.endText();

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 12);
            cs.newLineAtOffset(page.getMediaBox().getWidth() - 150, y);
            cs.showText("Max. Marks: 100");
            cs.endText();

            // Note
            y -= 40;
            centerText(cs, page,
                    "Note: Answer any FIVE full questions, choosing ONE full question from each module.",
                    y, PDType1Font.HELVETICA, 11);

            // Questions
            int qNo = 1;
            y -= 50;
            for (Map.Entry<String, Map<String, List<String>>> moduleEntry : paper.entrySet()) {
                String module = moduleEntry.getKey();

                // Module Heading (centered)
                centerText(cs, page, module, y, PDType1Font.HELVETICA_BOLD, 12);
                y -= 30;

                for (Map.Entry<String, List<String>> partEntry : moduleEntry.getValue().entrySet()) {
                    String part = partEntry.getKey();

                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    cs.newLineAtOffset(margin, y);
                    cs.showText(part);
                    cs.endText();
                    y -= 20;

                    for (String q : partEntry.getValue()) {
                        String cleaned = q.replaceAll("^\\d+[.)\\s]*", ""); // remove old numbering
                        cs.beginText();
                        cs.setFont(PDType1Font.HELVETICA, 11);
                        cs.newLineAtOffset(margin + 20, y);
                        cs.showText(qNo + ". " + cleaned);
                        cs.endText();
                        y -= 18;
                        qNo++;

                        // Page Break
                        if (y < margin + 50) {
                            cs.close();
                            page = new PDPage(PDRectangle.A4);
                            doc.addPage(page);
                            cs = new PDPageContentStream(doc, page);
                            y = page.getMediaBox().getHeight() - margin;
                        }
                    }
                    y -= 20;
                }
                y -= 30;
            }

            cs.close();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    // Helper: Center text
    private void centerText(PDPageContentStream cs, PDPage page,
                            String text, float y,
                            PDType1Font font, int fontSize) throws IOException {
        float textWidth = (font.getStringWidth(text) / 1000) * fontSize;
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset((page.getMediaBox().getWidth() - textWidth) / 2, y);
        cs.showText(text);
        cs.endText();
    }
}