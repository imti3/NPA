package com.nbl.npa.Controller.Frontend;

import com.nbl.npa.Config.AES256;
import com.nbl.npa.Config.Credentials;
import com.nbl.npa.Service.NpaLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;

import javax.crypto.Cipher;
import java.io.IOException;
@Slf4j
@RequiredArgsConstructor
@Controller
public class MainController {

    private final HttpSession session;

    private final Credentials Creds;

    private final NpaLogService npaLogService;

    @GetMapping("/npa")
    public String ssoLogin(HttpServletRequest request, HttpSession session, HttpServletResponse response) throws IOException {
        String userName = request.getParameter("userName");
        String userId = request.getParameter("userId");
        String userType = request.getParameter("userType");
        String brName = request.getParameter("brName");
        String brCode = request.getParameter("brCode");
        String timeStamp = request.getParameter("timestamp");

        if (userName != null && session.getAttribute("is_session") == null) {
            try {

                session.setMaxInactiveInterval(5 * 60);
                session.setAttribute("userName", userName);
                session.setAttribute("userId", userId);
                session.setAttribute("userType", userType);
                session.setAttribute("brCode", brCode);
                session.setAttribute("brName", brName);
                session.setAttribute("timestamp", timeStamp);
                session.setAttribute("is_session", true);

                // Decrypt the values from session attributes
                String decryptedUser = AES256.processCrypto(session.getAttribute("userName").toString(), Cipher.DECRYPT_MODE);
                String decryptedBranch = AES256.processCrypto(session.getAttribute("brName").toString(),Cipher.DECRYPT_MODE);
                String decryptedUserID = AES256.processCrypto(session.getAttribute("userId").toString(),Cipher.DECRYPT_MODE);
                String decryptedUserType = AES256.processCrypto(session.getAttribute("userType").toString(),Cipher.DECRYPT_MODE);
                Integer decryptedBrCode = Integer.parseInt(AES256.processCrypto(session.getAttribute("brCode").toString(),Cipher.DECRYPT_MODE));
                session.setAttribute("decryptedBrCode", decryptedBrCode);
                String decryptedTimeStamp = AES256.processCrypto(session.getAttribute("timestamp").toString(),Cipher.DECRYPT_MODE);


                // If any decryption fails (returns null), invalidate session and redirect
                if (decryptedUser == null || decryptedBranch == null || decryptedUserID == null || decryptedUserType == null
                        || decryptedBrCode == null || decryptedTimeStamp == null) {
                    session.invalidate();
                    response.sendRedirect(Creds.getRedirectURL());
                    return null;
                }

                request.setAttribute("decryptedUserID", decryptedUserID);
                request.setAttribute("userNameDecrypted", decryptedUser);
                request.setAttribute("brNameDecrypted", decryptedBranch);

                MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
                request.getParameterMap().forEach((key, values) -> {
                    for (String value : values) {
                        requestMap.add(key, value);
                    }
                });

                npaLogService.saveLog(
                        null,
                        null,
                       null,
                        null,
                        decryptedBrCode,
                        "/sso",
                        requestMap,
                        null,
                        decryptedUserID
                );



            } catch (Exception e) {
                // Log the error for debugging purposes
                log.error("Decryption or session error: {}", e.getMessage(), e);
                session.invalidate();
                response.sendRedirect(Creds.getRedirectURL());  // Redirect to the appropriate URL
                return null; // Prevent further execution
            }
        }


        return "redirect:/";  // Redirect to the dashboard
    }



    @GetMapping("/")
    public String showDashboard(Model model) {
        model.addAttribute("branchName", AES256.processCrypto(session.getAttribute("brName").toString(),Cipher.DECRYPT_MODE));
        model.addAttribute("userName", AES256.processCrypto(session.getAttribute("userId").toString(),Cipher.DECRYPT_MODE));
        return "index";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return "redirect:" + Creds.getRedirectURL();
    }


}
