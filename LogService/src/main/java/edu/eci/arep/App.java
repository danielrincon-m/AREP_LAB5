package edu.eci.arep;

import edu.eci.arep.log.LogService;

import static spark.Spark.port;
import static spark.Spark.put;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        port(getPort());
        put("/log", LogService::putString);
    }

    static int getPort() {
        if (System.getenv("PORT") != null) {
            return Integer.parseInt(System.getenv("PORT"));
        }
        return 35001; //returns default port if heroku-port isn't set
    }
}
