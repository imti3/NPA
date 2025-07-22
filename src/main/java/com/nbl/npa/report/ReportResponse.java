package com.nbl.npa.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


@Component
public class ReportResponse {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(ReportResponse.class);

	@Autowired
	ResourceLoader resourceLoader;

	@Autowired
	ReportSources reportSources;

	public ResponseEntity<Resource> getPDFResponse(String filename) throws IOException {
		Resource file = resourceLoader.getResource("file:" + reportSources.getOutputReport(filename));
		Path path = file.getFile()
				.toPath();

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(path))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);
	}


}
