package com.example.opaybanking.config;

import java.util.*;

public class DataPlansConfig {

    public static final Map<String, List<DataPlan>> NETWORK_DATA_PLANS = new HashMap<>();

    static {
        NETWORK_DATA_PLANS.put("MTN", Arrays.asList(
                new DataPlan("1", "100MB", 100.0, 1),
                new DataPlan("2", "350MB", 200.0, 7),
                new DataPlan("3", "1GB", 500.0, 30),
                new DataPlan("4", "2GB", 1000.0, 30),
                new DataPlan("5", "3GB", 1500.0, 30),
                new DataPlan("6", "5GB", 2500.0, 30),
                new DataPlan("7", "10GB", 5000.0, 30)
        ));

        NETWORK_DATA_PLANS.put("GLO", Arrays.asList(
                new DataPlan("1", "100MB", 100.0, 1),
                new DataPlan("2", "350MB", 200.0, 7),
                new DataPlan("3", "1.5GB", 500.0, 30),
                new DataPlan("4", "3GB", 1000.0, 30),
                new DataPlan("5", "5GB", 2000.0, 30),
                new DataPlan("6", "7GB", 2500.0, 30),
                new DataPlan("7", "10GB", 3000.0, 30)
        ));

        NETWORK_DATA_PLANS.put("AIRTEL", Arrays.asList(
                new DataPlan("1", "100MB", 100.0, 1),
                new DataPlan("2", "300MB", 200.0, 7),
                new DataPlan("3", "1GB", 500.0, 30),
                new DataPlan("4", "2GB", 1000.0, 30),
                new DataPlan("5", "3GB", 1500.0, 30),
                new DataPlan("6", "5GB", 2000.0, 30),
                new DataPlan("7", "10GB", 4000.0, 30)
        ));

        NETWORK_DATA_PLANS.put("9MOBILE", Arrays.asList(
                new DataPlan("1", "100MB", 100.0, 1),
                new DataPlan("2", "400MB", 200.0, 7),
                new DataPlan("3", "1.5GB", 500.0, 30),
                new DataPlan("4", "2GB", 1000.0, 30),
                new DataPlan("5", "3GB", 1200.0, 30),
                new DataPlan("6", "4GB", 1500.0, 30),
                new DataPlan("7", "11GB", 4000.0, 30)
        ));
    }

    public static class DataPlan {
        private String id;
        private String name;
        private double price;
        private int validityDays;

        public DataPlan(String id, String name, double price, int validityDays) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.validityDays = validityDays;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public double getPrice() { return price; }
        public int getValidityDays() { return validityDays; }
    }
}