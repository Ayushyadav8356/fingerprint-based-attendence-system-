package com.attendance.service;

import com.attendance.model.Attendance;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CsvService {

    public ByteArrayInputStream generateAttendanceCsv(List<Attendance> logs) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(out)) {
            // Write UTF-8 BOM for Microsoft Excel compatibility
            writer.write('\ufeff');
            
            // Header row
            writer.println("Log ID,Roll Number,Student Name,Class Name,Subject,Timestamp,Status,Method");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (Attendance log : logs) {
                StringBuilder sb = new StringBuilder();
                sb.append(log.getId()).append(",");
                sb.append(escapeCsv(log.getStudent().getRollNumber())).append(",");
                sb.append(escapeCsv(log.getStudent().getUser().getFullName())).append(",");
                sb.append(escapeCsv(log.getClazz().getClassName())).append(",");
                sb.append(escapeCsv(log.getClazz().getSubject())).append(",");
                sb.append(log.getTimestamp() != null ? log.getTimestamp().format(formatter) : "N/A").append(",");
                sb.append(log.getStatus()).append(",");
                sb.append(log.getMethod());
                writer.println(sb.toString());
            }
            writer.flush();
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
