package com.nbl.npa.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class ReportSources {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(ReportSources.class);
	
	//..........
	@Value("${npa.report}")
	public String REPORT_SOURCE_DIR;
	


	@Value("${npa.report}")
	public String REPORT_COMPILE_DIR;


    /*
	@Value("${report.image.dir}")
	public String REPORT_IMAGE_DIR;*/

	@Value("${npa.report.output}")
	public String REPORT_OUTPUT_DIR;

	//............
	//Image classpath file name

	//.........
	public String getSourceReport(String filename) {
		return String.format("%s%s", REPORT_SOURCE_DIR, filename);
	}
	


	public String getCompiledReport(String filename) {
		return String.format("%s%s", REPORT_COMPILE_DIR, filename);
	}
	
	/*public String getImage(String image) {
		return String.format("%s%s", REPORT_IMAGE_DIR, image);
	}*/
	
	public String getOutputReport(String filename) {
		return String.format("%s%s", REPORT_OUTPUT_DIR, filename);
	}

}
