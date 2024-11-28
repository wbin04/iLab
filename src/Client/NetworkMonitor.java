package Client;

import java.net.InetAddress;

public class NetworkMonitor {
    public String[] getIP(String domain) {
        try {
            InetAddress[] addresses = InetAddress.getAllByName(domain);
            String[] ips = new String[addresses.length];
            for (int i = 0; i < addresses.length; i++) {
                ips[i] = addresses[i].getHostAddress();
            }
            return ips;
        } catch (Exception e) {
            e.printStackTrace();
            return new String[0];
        }
    }
    
    public void blockDomain(String[] ips) {
    	for (String ip : ips) {
            System.out.println(ip);
            String adminCmd = "cmd.exe /c powershell -Command \"Start-Process powershell -ArgumentList '-Command \"New-NetFirewallRule -DisplayName ''Block IP'' -Direction Outbound -Action Block -RemoteAddress " + ip + "\"' -Verb RunAs\"";

            System.out.println("Executing command: " + adminCmd);

            try {
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", adminCmd);
                Process process = pb.start();
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    System.out.println("Successfully blocked IP: " + ip);
                } else {
                    System.out.println("Failed to block IP: " + ip + " with exit code: " + exitCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
