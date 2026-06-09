package me.vennlmao.gems.resourcepack;

import me.vennlmao.gems.GemsPlugin;

public class PackServer {

    private final GemsPlugin plugin;
    private String packUrl;
    private byte[] packSha1;

    public PackServer(GemsPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() throws Exception {
        packUrl = plugin.getConfig().getString("resourcepack.url", "");
        String sha1Hex = plugin.getConfig().getString("resourcepack.sha1", "");

        if (packUrl.isEmpty()) {
            plugin.getLogger().severe("resourcepack.url is not set in config.yml!");
            return;
        }
        if (sha1Hex.isEmpty()) {
            plugin.getLogger().severe("resourcepack.sha1 is not set in config.yml!");
            return;
        }

        packSha1 = hexToBytes(sha1Hex);
        plugin.getLogger().info("Pack URL: " + packUrl);
    }

    public void stop() {}

    public String getPackUrl()  { return packUrl; }
    public byte[] getPackSha1() { return packSha1; }
    public boolean isReady()    { return packUrl != null && !packUrl.isEmpty() && packSha1 != null; }

    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i + 1), 16));
        return data;
    }
}
