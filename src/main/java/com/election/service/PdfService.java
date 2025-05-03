package com.election.service;

import com.election.model.Candidate;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PdfService {
    private static final String OUTPUT_DIR = "output";
    
    private static PdfService instance;
    
    private PdfService() {
        // Create output directory if it doesn't exist
        File outputDir = new File(OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
    }
    
    public static PdfService getInstance() {
        if (instance == null) {
            instance = new PdfService();
        }
        return instance;
    }
    
    public String generateSelectionReport(List<Candidate> selectedCandidates) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = OUTPUT_DIR + "/selection_" + timestamp + ".pdf";
        
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();
            
            // Add title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            Paragraph title = new Paragraph("Election Selection Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            
            // Add timestamp
            Font timeFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.GRAY);
            Paragraph timeP = new Paragraph("Generated: " + 
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), timeFont);
            timeP.setAlignment(Element.ALIGN_CENTER);
            document.add(timeP);
            document.add(Chunk.NEWLINE);
            
            // Create selection table
            PdfPTable table = new PdfPTable(4); // Order, Name, List, Index
            table.setWidthPercentage(100);
            
            // Add table headers
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
            BaseColor headerBg = new BaseColor(44, 62, 80);
            
            addTableHeader(table, "Selection Order", headerFont, headerBg);
            addTableHeader(table, "Candidate Name", headerFont, headerBg);
            addTableHeader(table, "List", headerFont, headerBg);
            addTableHeader(table, "List Position", headerFont, headerBg);
            
            // Add data rows
            Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
            BaseColor altRowBg = new BaseColor(240, 240, 240);
            
            for (Candidate candidate : selectedCandidates) {
                boolean isAlternateRow = selectedCandidates.indexOf(candidate) % 2 == 1;
                BaseColor rowBg = isAlternateRow ? altRowBg : BaseColor.WHITE;
                
                addTableCell(table, String.valueOf(candidate.getSelectionOrder()), rowFont, rowBg);
                addTableCell(table, candidate.getName(), rowFont, rowBg);
                addTableCell(table, candidate.getList(), rowFont, rowBg);
                addTableCell(table, String.valueOf(candidate.getIndex() + 1), rowFont, rowBg);
            }
            
            document.add(table);
            document.close();
            
            return filename;
            
        } catch (Exception e) {
            System.err.println("Error generating PDF: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private void addTableHeader(PdfPTable table, String text, Font font, BaseColor bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        table.addCell(cell);
    }
    
    private void addTableCell(PdfPTable table, String text, Font font, BaseColor bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        table.addCell(cell);
    }
} 