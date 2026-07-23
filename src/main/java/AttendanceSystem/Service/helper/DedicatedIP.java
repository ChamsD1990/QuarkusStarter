package AttendanceSystem.Service.helper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.HttpHeaders;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@ApplicationScoped
public class DedicatedIP {

    // ============ NEW METHOD ============
    /**
     * Get client IP from HTTP headers (for real client IP behind proxy)
     */
    public String getClientIp(HttpHeaders headers) {
        // Cek berbagai header untuk mendapatkan IP asli
        String ip = headers.getHeaderString("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getHeaderString("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getHeaderString("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getHeaderString("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getHeaderString("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getHeaderString("Remote-Addr");
        }

        // Jika multiple IP (X-Forwarded-For), ambil yang pertama
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        // Validasi IP
        if (ip == null || ip.isEmpty() || !isIPv4(ip)) {
            ip = "0.0.0.0";
        }

        return ip;
    }

    // ============ EXISTING METHODS ============

    public String getPublicIPv4() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    continue;
                }
                String displayName = networkInterface.getDisplayName();
                if (displayName != null && (displayName.toLowerCase().contains("virtual") ||
                        displayName.toLowerCase().contains("vmware") ||
                        displayName.toLowerCase().contains("vbox") ||
                        displayName.toLowerCase().contains("docker") ||
                        displayName.toLowerCase().contains("hyper-v"))) {
                    continue;
                }

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address) {
                        String ip = address.getHostAddress();
                        if (!ip.equals("127.0.0.1") && !ip.equals("0.0.0.0")) {
                            return ip;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "127.0.0.1";
    }

    public List<String> getAllIPv4() {
        List<String> ips = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                if (!networkInterface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address) {
                        ips.add(address.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ips;
    }

    public String getPublicIPv4Only() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    continue;
                }

                String displayName = networkInterface.getDisplayName();
                if (displayName != null && (displayName.toLowerCase().contains("virtual") ||
                        displayName.toLowerCase().contains("vmware") ||
                        displayName.toLowerCase().contains("vbox") ||
                        displayName.toLowerCase().contains("docker") ||
                        displayName.toLowerCase().contains("hyper-v"))) {
                    continue;
                }

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();

                    if (address instanceof Inet4Address) {
                        String ip = address.getHostAddress();

                        if (!ip.equals("127.0.0.1") &&
                                !ip.equals("0.0.0.0") &&
                                !isPrivateIP(ip)) {
                            return ip;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return getPublicIPFromExternal();
    }

    public boolean isPrivateIP(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            return address.isSiteLocalAddress() ||
                    address.isLoopbackAddress() ||
                    address.isLinkLocalAddress();
        } catch (UnknownHostException e) {
            return true;
        }
    }

    public String getPublicIPFromExternal() {
        try {
            java.net.URL url = new java.net.URL("https://api.ipify.org");
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (java.util.Scanner scanner = new java.util.Scanner(connection.getInputStream())) {
                    if (scanner.hasNext()) {
                        return scanner.next();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "127.0.0.1";
    }

    public String getIPWithPriority() {
        String ethernetIP = null;
        String wifiIP = null;
        String virtualIP = null;

        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    continue;
                }

                String displayName = networkInterface.getDisplayName().toLowerCase();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address) {
                        String ip = address.getHostAddress();
                        if (ip.equals("127.0.0.1") || ip.equals("0.0.0.0")) {
                            continue;
                        }

                        if (displayName.contains("ethernet") ||
                                displayName.contains("enp") ||
                                displayName.contains("eth")) {
                            ethernetIP = ip;
                        } else if (displayName.contains("wifi") ||
                                displayName.contains("wlan") ||
                                displayName.contains("wl")) {
                            wifiIP = ip;
                        } else if (displayName.contains("virtual") ||
                                displayName.contains("vmware") ||
                                displayName.contains("vbox") ||
                                displayName.contains("docker")) {
                            virtualIP = ip;
                        } else {
                            if (ethernetIP == null)
                                ethernetIP = ip;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        if (ethernetIP != null)
            return ethernetIP;
        if (wifiIP != null)
            return wifiIP;
        if (virtualIP != null)
            return virtualIP;
        return getPublicIPv4();
    }

    public String getHostname(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            return address.getHostName();
        } catch (UnknownHostException e) {
            return ip;
        }
    }

    public boolean isIPv4(String ip) {
        return ip != null && ip.matches("^([0-9]{1,3}\\.){3}[0-9]{1,3}$");
    }

    // ============ NEW METHODS FOR BLOCKING ============

    private final List<String> blockedIPs = new ArrayList<>();
    private final List<String> whitelistedIPs = new ArrayList<>();

    public boolean isBlocked(String ip) {
        if (ip == null)
            return true;
        return blockedIPs.contains(ip);
    }

    public boolean isWhitelisted(String ip) {
        if (ip == null)
            return false;
        return whitelistedIPs.contains(ip);
    }

    public void blockIp(String ip) {
        if (ip != null && !ip.isEmpty() && !blockedIPs.contains(ip)) {
            blockedIPs.add(ip);
        }
    }

    public void unblockIp(String ip) {
        blockedIPs.remove(ip);
    }

    public void whitelistIp(String ip) {
        if (ip != null && !ip.isEmpty() && !whitelistedIPs.contains(ip)) {
            whitelistedIPs.add(ip);
        }
    }
}