package edu.eci.arep.persistence;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import spark.Request;
import spark.Response;

import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Database {

    public String insertString(Request req, Response res) {
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        Document cadena = new Document();
        cadena.put("string", req.queryParams("string"));
        cadena.put("date", date.format(formatter));

        MongoClient mongoClient = null;
        try {
            mongoClient = connect();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            res.status(500);
            return "Pailis";
        }

        insert(mongoClient, cadena);
        return get(mongoClient, 10);
    }

    private void insert(MongoClient mongoClient, Document data) {
        MongoDatabase database = mongoClient.getDatabase("LogService");
        MongoCollection<Document> collection = database.getCollection("strings");
        collection.insertOne(data);
    }

    private String get(MongoClient mongoClient, int n) {
        MongoDatabase database = mongoClient.getDatabase("LogService");
        MongoCollection<Document> collection = database.getCollection("strings");
        ArrayList<String> data = collection
                .find()
                .sort(new BasicDBObject("date", -1))
                .limit(n)
                .projection(Projections.excludeId())
                .map(Document::toJson)
                .into(new ArrayList<>());
        return data.toString();
    }

    private MongoClient connect() throws UnknownHostException {
        return new MongoClient(new MongoClientURI("mongodb://mongodb:27017"));
    }
}
