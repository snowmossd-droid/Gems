package me.vennlmao.gems.resourcepack;

import com.sun.net.httpserver.HttpServer;
import me.vennlmao.gems.GemsPlugin;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.Executors;

public class PackServer {

    private final GemsPlugin plugin;
    private HttpServer httpServer;
    private String packUrl;
    private byte[] packSha1;
    private Path packFile;

    public PackServer(GemsPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() throws Exception {
        packFile = plugin.getDataFolder().toPath().resolve("resourcepack.zip");
        if (!Files.exists(packFile)) {
            plugin.getLogger().severe("resourcepack.zip not found!");
            return;
        }

        packSha1 = computeSha1(packFile);

        String host = resolvePublicHost();
        int httpPort = findFreePort();

        httpServer = HttpServer.create(new InetSocketAddress("0.0.0.0", httpPort), 0);
        httpServer.createContext("/gems-pack.zip", exchange -> {
            byte[] data = Files.readAllBytes(packFile);
            exchange.getResponseHeaders().set("Content-Type", "application/zip");
            exchange.sendResponseHeaders(200, data.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(data);
            }
        });
        httpServer.setExecutor(Executors.newFixedThreadPool(4));
        httpServer.start();

        packUrl = "http://" + host + ":" + httpPort + "/gems-pack.zip";
        plugin.getLogger().info("Pack server → " + packUrl);
    }

    public void stop() {
        if (httpServer != null) httpServer.stop(1);
    }

    public String getPackUrl()  { return packUrl; }
    public byte[] getPackSha1() { return packSha1; }
    public boolean isReady()    { return packUrl != null && packSha1 != null; }

    private String resolvePublicHost() {
        String serverIp = readServerProperty("server-ip", "").trim();
        if (!serverIp.isEmpty()) return serverIp;

        try {
            InetAddress addr = InetAddress.getLocalHost();
            String hostname = addr.getCanonicalHostName();
            if (!hostname.equals(addr.getHostAddress())) return hostname;
            return addr.getHostAddress();
        } catch (Exception ignored) {}

        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress("8.8.8.8", 80));
            return s.getLocalAddress().getHostAddress();
        } catch (Exception ignored) {}

        return "127.0.0.1";
    }

    private int findFreePort() {
        int preferred = plugin.getConfig().getInt("resourcepack.port", 0);
        if (preferred > 0 && isPortFree(preferred)) return preferred;

        int mcPort = Integer.parseInt(readServerProperty("server-port", "25565").trim());
        for (int candidate = mcPort + 1; candidate < mcPort + 200; candidate++) {
            if (isPortFree(candidate)) return candidate;
        }

        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        } catch (Exception e) {
            return 8765;
        }
    }

    private boolean isPortFree(int port) {
        try (ServerSocket s = new ServerSocket(port)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String readServerProperty(String key, String fallback) {
        Path props = plugin.getServer().getWorldContainer().toPath()
            .resolve("server.properties");
        if (!Files.exists(props)) return fallback;
        try (InputStream is = Files.newInputStream(props)) {
            Properties p = new Properties();
            p.load(is);
            return p.getProperty(key, fallback);
        } catch (Exception e) {
            return fallback;
        }
    }

    private byte[] computeSha1(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (InputStream is = Files.newInputStream(file)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) != -1) digest.update(buf, 0, n);
        }
        return digest.digest();
    }
}
