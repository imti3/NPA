package com.nbl.npa.Config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@AllArgsConstructor
@Component
public class Credentials {
	private final String redirectURL = "https://sso.nblbd.com/sso/";
	  
//	  TEST SSO
//	  private String redirectURL ="http://192.168.0.127/sso/";


}