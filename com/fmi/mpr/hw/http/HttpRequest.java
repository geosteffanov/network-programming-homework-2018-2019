package com.fmi.mpr.hw.http;

public class HttpRequest {
    public enum Type {
        GET, POST
    }

    public Type type;
    public String resourceType;
    public String resourceName;

    public HttpRequest(Type type, String resourceType, String resourceName) {
        this.type = type;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
    }

    public static HttpRequest fromString(String requestString) throws IllegalArgumentException {
       String[] lines = requestString.split("\n");
       String header = lines[0];

       String[] headerItems = header.split(" ");

       String httpMethod = headerItems[0];

       String[] resourcePath = headerItems[1].split("/");

       String resourceType = resourcePath[0];
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
               throw new IllegalArgumentException("Unrecognized http method: " + httpMethod);
        }


       return new HttpRequest(type, resourceType, resourceName);
    }
}
