package edu.eci.arep;

import static spark.Spark.*;

import edu.eci.arep.manager.ResourceManager;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        port(getPort());
        staticFileLocation("/static");
        get("/newlog", (req, res) -> {
            res.redirect("index.html");
            return null;
        });
        post("/newlog", (req, res) -> {
            String resp;
            try {
                resp = ResourceManager.insertLog(req, res);
            } catch (Exception e) {
                e.printStackTrace();
                resp = "Error";
            }
            return resp;
        });
    }

    static int getPort() {
        if (System.getenv("PORT") != null) {
            return Integer.parseInt(System.getenv("PORT"));
        }
        return 4567; // returns default port if heroku-port isn't set
    }
}
