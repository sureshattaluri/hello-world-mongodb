package com.example.hellomongodb;

import build.buf.gen.cloud.planton.apis.commons.apiresource.ApiResourceMetadata;
import build.buf.gen.cloud.planton.apis.resourcemanager.v1.organization.Organization;
import build.buf.gen.cloud.planton.apis.resourcemanager.v1.organization.OrganizationSpec;
import build.buf.gen.cloud.planton.apis.resourcemanager.v1.organization.OrganizationStackJobSettings;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;

import java.util.Collections;

public class HelloMongoDB {
    public static void main(String[] args) {
        // Create MongoCredential object with username, database, and password
        MongoCredential credential = MongoCredential.createCredential("root", "admin", "UG:#+7pb25Zd".toCharArray());

        // Configure the MongoClientSettings with server address and credential
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToClusterSettings(builder ->
                        builder.hosts(Collections.singletonList(new ServerAddress("mdbk8s-planton-cloud-app-prod-main.planton.live", 27017))))
                .credential(credential)
                .build();

        // Create the MongoClient using the configured settings
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            MongoDatabase database = mongoClient.getDatabase("test");
            MongoCollection<Document> collection = database.getCollection("organization");

            // Create a Protobuf message
            Organization data = Organization.newBuilder()
                    .setApiVersion("resource-manager.planton.cloud/v1")
                    .setKind("Organization")
                    .setMetadata(ApiResourceMetadata.newBuilder()
                            .setId("suresh-test")
                            .setName("Suresh Test Organization")
                            .build())
//                    .setSpec(OrganizationSpec.newBuilder()
//                            .setDescription("this is to test mongodb writes and reads")
////                            .setStackJobSettings(OrganizationStackJobSettings.newBuilder()
////                                    .setIsDefaultPulumiBackendCredentialDisabled(true)
////                                    .build())
//                            .build())
                    .build();

            // Convert Protobuf message to JSON
            String jsonData = JsonFormat.printer().print(data);
            Document jsonDocument = Document.parse(jsonData);

            collection.replaceOne(
                    new Document("metadata.id", data.getMetadata().getId()),  // Filter by "metadata.id"
                    jsonDocument,                                            // Replace with the new JSON document
                    new ReplaceOptions().upsert(true)                        // Upsert if no document matches
            );

            // Retrieve the JSON document based on "metadata.id"
            Document retrievedDocument = collection.find(new Document("metadata.id", data.getMetadata().getId())).first();
            if (retrievedDocument != null) {
                System.out.println("Retrieved document: " + retrievedDocument.toJson());
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}
