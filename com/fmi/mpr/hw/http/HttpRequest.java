package com.fmi.mpr.hw.http;

public class HttpRequest {
    public enum Type {
        GET, POST, FAVICON, INVALID
    }

    public Type type;
    public String resourceType;
    public String resourceName;
    public String requestString;
    public byte[] data;

    public HttpRequest(Type type, String resourceType, String resourceName, String requestString) {
        this.type = type;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.requestString = requestString;
    }

    public static HttpRequest invalid() {
        return new HttpRequest(Type.INVALID, "", "", "");
    }
    public static HttpRequest favicon() {
        return new HttpRequest(Type.FAVICON, "", "", "");
    }

    public static HttpRequest fromString(String requestString) throws IllegalArgumentException {
       String[] lines = requestString.split("\n");
       String header = lines[0];

       String[] headerItems = header.split(" ");

       String httpMethod = headerItems[0];

        if (headerItems.length < 2) {
            return HttpRequest.invalid();
        }

       String[] resourcePath = headerItems[1].substring(1).split("/");
       String resourceType = resourcePath[0];

       if (resourceType.equals("favicon.ico")) {
           return HttpRequest.favicon();
       }

       if (resourcePath.length < 2) {
           return HttpRequest.invalid();
       }

       String resourceName = resourcePath[1];

       Type type;

       switch (httpMethod) {
           case "GET":
               type = Type.GET;
               break;
           case "POST":
               type = Type.POST;
               break;
           default:
               return HttpRequest.invalid();
        }


       return new HttpRequest(type, resourceType, resourceName, requestString);
    }

    public String toString() {
        return this.requestString;
    }
}
