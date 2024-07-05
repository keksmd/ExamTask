package com.example.exam;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


public class CrptApi {
    private final Semaphore semaphore;
    private final ScheduledExecutorService scheduler;
    private final HttpClient httpClient;
    private final Gson gson;
    private final String apiUrl = "https://ismp.crpt.ru/api/v3/lk/documents/create";

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.semaphore = new Semaphore(requestLimit);
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();

        long interval = timeUnit.toMillis(1);
        scheduler.scheduleAtFixedRate(semaphore::release, interval, interval, TimeUnit.MILLISECONDS);
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        CrptApi api = new CrptApi(TimeUnit.SECONDS, 5);

        Document.Description description = new Document.Description();
        description.participantInn = "1234567890";

        Document.Product product = new Document.Product();
        product.certificate_document = "cert_doc";
        product.certificate_document_date = "2020-01-23";
        product.certificate_document_number = "cert_num";
        product.owner_inn = "owner_inn";
        product.producer_inn = "producer_inn";
        product.production_date = "2020-01-23";
        product.tnved_code = "tnved";
        product.uit_code = "uit";
        product.uitu_code = "uitu";

        Document document = new Document();
        document.description = description;
        document.doc_id = "doc_id";
        document.doc_status = "doc_status";
        document.doc_type = "LP_INTRODUCE_GOODS";
        document.importRequest = true;
        document.owner_inn = "owner_inn";
        document.participant_inn = "participant_inn";
        document.producer_inn = "producer_inn";
        document.production_date = "2020-01-23";
        document.production_type = "production_type";
        document.products = new Document.Product[]{product};
        document.reg_date = "2020-01-23";
        document.reg_number = "reg_number";

        Signature signature = new Signature("sample_signature");

        api.createDocument(document, signature);
        api.shutdown();
    }

    public void createDocument(Document document, Signature signature) throws InterruptedException, IOException {
        if (signature.isValid()) {
            semaphore.acquire();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(document)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());

            semaphore.release();
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }

    public static class Signature {
        private final String value;

        public Signature(String value) {
            this.value = value;
        }

        public boolean isValid() {
            return this.value != null && !value.isBlank() && value.isEmpty();
        }
    }

    public static class Document {
        public Description description;
        public String doc_id;
        public String doc_status;
        public String doc_type;
        public boolean importRequest;
        public String owner_inn;
        public String participant_inn;
        public String producer_inn;
        public String production_date;
        public String production_type;
        public Product[] products;
        public String reg_date;
        public String reg_number;

        public static class Description {
            public String participantInn;
        }

        public static class Product {
            public String certificate_document;
            public String certificate_document_date;
            public String certificate_document_number;
            public String owner_inn;
            public String producer_inn;
            public String production_date;
            public String tnved_code;
            public String uit_code;
            public String uitu_code;
        }
    }
}




