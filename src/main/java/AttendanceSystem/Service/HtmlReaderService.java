package AttendanceSystem.Service;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class HtmlReaderService {

    public String readHtml() {
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("templates/dashboard.html")) {

            if (is == null) {
                return "<h1>File templates/dashboard.html tidak ditemukan!</h1>";
            }

            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return "<h1>Error: " + e.getMessage() + "</h1>";
        }
    }
}