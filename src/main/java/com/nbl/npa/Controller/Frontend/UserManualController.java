package com.nbl.npa.Controller.Frontend;


import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Controller
@RequestMapping("/manual")
@RequiredArgsConstructor
public class UserManualController {

    private final HttpSession session;

    @Value("${npa.report}")
    private String reportPath;

    private static final String FILE_NAME = "User_manual_Branch.pdf";

    /** Open manual inline in browser */
    @GetMapping("/branch")
    public ResponseEntity<Resource> viewManual() {
        if (!isSessionValid()) {
            return ResponseEntity.status(302).header(HttpHeaders.LOCATION, "/logout").build();
        }

        try {
            Resource resource = loadManual();
            if (resource == null || !resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + FILE_NAME + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error serving manual: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/branch/download")
    public ResponseEntity<Resource> downloadManual() {
        if (!isSessionValid()) {
            return ResponseEntity.status(302).header(HttpHeaders.LOCATION, "/logout").build();
        }

        try {
            Resource resource = loadManual();
            if (resource == null || !resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + FILE_NAME + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error downloading manual: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /** Helpers */
    private boolean isSessionValid() {
        return session.getAttribute("is_session") != null && (Boolean) session.getAttribute("is_session");
    }

    private Resource loadManual() throws MalformedURLException {
        if (reportPath.startsWith("classpath:")) {
            String path = reportPath.substring("classpath:".length());
            if (!path.endsWith("/")) path += "/";
            return new ClassPathResource(path + FILE_NAME);
        }
        Path filePath = Paths.get(reportPath, FILE_NAME);
        return new UrlResource(filePath.toUri());
    }
}
