package com.unal.isw2.lab5;

import jakarta.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SaludController {

    @GetMapping("/api/v1/salud")
    public Map<String, String> salud(HttpServletRequest request) {
        X509Certificate[] certs =(X509Certificate[]) request.getAttribute("jakarta.servlet.request.X509Certificate");

        if (certs != null && certs.length > 0) {
            String cn = certs[0].getSubjectX500Principal().getName();
            return Map.of("estado", "ok", "cliente", cn);
        }

        return Map.of("estado", "ok");
    }
}
