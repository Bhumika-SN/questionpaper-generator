package com.vtuu.questionpaper.controller;

import com.vtuu.questionpaper.service.QuestionBankService;
import com.vtuu.questionpaper.util.PdfGenerator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Controller
public class QuestionPaperController {

    private final QuestionBankService questionBankService;
    private final PdfGenerator pdfGenerator;

    @Autowired
    public QuestionPaperController(QuestionBankService questionBankService, PdfGenerator pdfGenerator) {
        this.questionBankService = questionBankService;
        this.pdfGenerator = pdfGenerator;
    }

    @GetMapping("/upload")
    public String showUploadPage() {
        return "upload";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   @RequestParam("sem") String sem,
                                   @RequestParam("monthYear") String monthYear,
                                   @RequestParam("subject") String subject,
                                   @RequestParam("subjectCode") String subjectCode,
                                   Model model) {
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a file to upload.");
            return "upload";
        }

        try {
            PDDocument document = PDDocument.load(file.getInputStream());
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();

            List<String> lines = Arrays.asList(text.split("\\r?\\n"));
            Map<String, List<String>> modules = parseModules(lines);

            if (modules.isEmpty()) {
                List<String> fallback = new ArrayList<>();
                for (String l : lines) {
                    if (l != null && !l.trim().isEmpty()) fallback.add(l.trim());
                }
                modules.put("Module-1", fallback);
            }

            questionBankService.saveQuestionsByModule(modules);

            model.addAttribute("message", "Uploaded & parsed into modules successfully.");
            model.addAttribute("questionsMap", modules);

            return "redirect:/paper?sem=" + sem + "&monthYear=" + monthYear +
                    "&subject=" + subject + "&subjectCode=" + subjectCode;

        } catch (IOException e) {
            model.addAttribute("message", "Error reading PDF: " + e.getMessage());
            return "upload";
        }
    }

    // ✅ Helper to detect modules
    private Map<String, List<String>> parseModules(List<String> lines) {
        Map<String, List<String>> moduleMap = new LinkedHashMap<>();
        String currentModule = null;
        List<String> buffer = new ArrayList<>();

        for (String raw : lines) {
            if (raw == null) continue;
            String line = raw.trim();
            if (line.isEmpty()) continue;

            String low = line.toLowerCase();
            if (low.contains("marks") || low.matches("^page\\s*\\d+$") || low.matches("^\\d+ of \\d+$")) {
                continue;
            }
            if (line.matches("(?i)^module\\s*[-:\\.]?\\s*\\d+.*")) {
                if (currentModule != null) {
                    moduleMap.put(currentModule, new ArrayList<>(buffer));
                    buffer.clear();
                }
                String num = line.replaceAll("(?i).*module\\s*[-:\\.]?\\s*(\\d+).*", "$1");
                currentModule = "Module-" + num;
                continue;
            }

            if (line.length() < 3) continue;
            if (line.matches("^[\\d\\)\\.a-zA-Z\\-\\s]{1,4}$")) continue;

            buffer.add(line);
        }

        if (currentModule != null && !buffer.isEmpty()) {
            moduleMap.put(currentModule, new ArrayList<>(buffer));
        }

        return moduleMap;
    }

    @GetMapping("/paper")
    public String generatePaper(@RequestParam String sem,
                                @RequestParam String monthYear,
                                @RequestParam String subject,
                                @RequestParam String subjectCode,
                                Model model) {
        Map<String, Map<String, List<String>>> combined = questionBankService.getCombinedPaper(2);

        if (combined.isEmpty()) {
            model.addAttribute("message", "No questions available. Upload a question bank first.");
            return "upload";
        }

        // ✅ Sort modules in order (Module-1 → Module-2 → …)
        List<String> sortedKeys = combined.keySet().stream()
                .sorted(Comparator.comparingInt(k -> Integer.parseInt(k.replaceAll("\\D", ""))))
                .toList();

        Map<String, Map<String, List<String>>> ordered = new LinkedHashMap<>();
        for (String key : sortedKeys) {
            ordered.put(key, combined.get(key));
        }

        model.addAttribute("paper", ordered);
        model.addAttribute("sem", sem);
        model.addAttribute("monthYear", monthYear);
        model.addAttribute("subject", subject);
        model.addAttribute("subjectCode", subjectCode);

        return "paper";
    }

    @GetMapping("/paper/pdf")
    public ResponseEntity<byte[]> downloadPaperPdf(@RequestParam String sem,
                                                   @RequestParam String monthYear,
                                                   @RequestParam String subject,
                                                   @RequestParam String subjectCode) throws IOException {
        Map<String, Map<String, List<String>>> combined = questionBankService.getCombinedPaper(2);

        if (combined.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        // ✅ Sort modules in order
        List<String> sortedKeys = combined.keySet().stream()
                .sorted(Comparator.comparingInt(k -> Integer.parseInt(k.replaceAll("\\D", ""))))
                .toList();

        Map<String, Map<String, List<String>>> ordered = new LinkedHashMap<>();
        for (String key : sortedKeys) {
            ordered.put(key, combined.get(key));
        }

        // ✅ FIXED: Correct signature
        byte[] pdf = pdfGenerator.generateVTUPaperFormatted(
                ordered,
                sem,
                monthYear,
                subject,
                subjectCode
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=questionpaper.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}