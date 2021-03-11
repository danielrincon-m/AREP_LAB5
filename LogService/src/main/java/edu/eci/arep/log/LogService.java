package edu.eci.arep.log;

import edu.eci.arep.persistence.Database;
import spark.Request;
import spark.Response;

public class LogService {
    public static String putString(Request req, Response res) {
        System.out.println("Connected");
        Database db = new Database();
        return db.insertString(req, res);
    }
}
