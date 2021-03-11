package edu.eci.arep.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import spark.Request; 
import spark.Response;

public class ResourceManager {
    private static final String network = "logsrv";

    private static final int[] ports = { 35001, 35002, 35003 };
    private static int serviceInstances;
    private static int actualService;

    static {
        serviceInstances = ports.length;
        actualService = 0;
    }

    public static String insertLog(Request req, Response res) throws IOException {
        URL url = new URL("http://logservice" + ports[actualService] + ":6000/log");
        System.out.println(url.toString());
        actualService = (actualService + 1) % serviceInstances;

        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;
        http.setRequestMethod("PUT");
        http.setDoOutput(true);

        Map<String, String> arguments = new HashMap<>();
        arguments.put("string", req.queryParams("log"));
        StringJoiner sj = new StringJoiner("&");
        for (Map.Entry<String, String> entry : arguments.entrySet())
            sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
        byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
        int length = out.length;

        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        http.connect();
        try (OutputStream os = http.getOutputStream()) {
            os.write(out);
        }

        InputStream is = http.getInputStream();
        return new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
    }
}
