package com.nbl.npa.Config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.crypto.Cipher;

@AllArgsConstructor
@Component
public class SSOInterceptor implements HandlerInterceptor {

    // Add a logger instance
    private static final Logger logger = LoggerFactory.getLogger(SSOInterceptor.class);
    private Credentials creds;



    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("is_session") == null) {
            response.sendRedirect(creds.getRedirectURL());
            return false;
        }

        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0
        response.setDateHeader("Expires", 0); // Proxies


        try {
            // Decrypt essential fields
            String decryptedUser = AES256.processCrypto(session.getAttribute("userName").toString(), Cipher.DECRYPT_MODE);
            String decryptedBranch = AES256.processCrypto(session.getAttribute("brName").toString(),Cipher.DECRYPT_MODE);
            String decryptedUserID = AES256.processCrypto(session.getAttribute("userId").toString(),Cipher.DECRYPT_MODE);
            String decryptedUserType = AES256.processCrypto(session.getAttribute("userType").toString(),Cipher.DECRYPT_MODE);




            // Attach to request (or session, whichever is appropriate)
            request.setAttribute("userNameDecrypted", decryptedUser);
            request.setAttribute("brNameDecrypted", decryptedBranch);
            request.setAttribute("decryptedUserID", decryptedUserID);
            request.setAttribute("decryptedUserType", decryptedUserType);
            request.setAttribute("userType", session.getAttribute("userType"));
            request.setAttribute("brCode", session.getAttribute("brCode"));

            session.setAttribute("decryptedUserName", decryptedUser);
            session.setAttribute("decryptedBranch", decryptedBranch);



        } catch (Exception e) {
            session.invalidate();
            logger.error("Error during session attribute decryption in SSOInterceptor: ", e);
            response.sendRedirect(creds.getRedirectURL());
            return false;
        }

        return true;
    }
}
