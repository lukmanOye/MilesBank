package com.example.opaybanking.config;

import java.util.*;

public class AirtimePlansConfig {

    public static final Map<String, List<AirtimePlan>> NETWORK_AIRTIME_PLANS = new HashMap<>();

    static {
        List<AirtimePlan> commonPlans = Arrays.asList(
                new AirtimePlan("1", "₦50", 50.0),
                new AirtimePlan("2", "₦100", 100.0),
                new AirtimePlan("3", "₦200", 200.0),
                new AirtimePlan("4", "₦500", 500.0),
                new AirtimePlan("5", "₦1000", 1000.0),
                new AirtimePlan("6", "₦2000", 2000.0),
                new AirtimePlan("7", "₦5000", 5000.0)
        );

        NETWORK_AIRTIME_PLANS.put("MTN", commonPlans);
        NETWORK_AIRTIME_PLANS.put("GLO", commonPlans);
        NETWORK_AIRTIME_PLANS.put("AIRTEL", commonPlans);
        NETWORK_AIRTIME_PLANS.put("9MOBILE", commonPlans);
    }

    public static class AirtimePlan {
        private String id;
        private String name;
        private double amount;

        public AirtimePlan(String id, String name, double amount) {
            this.id = id;
            this.name = name;
            this.amount = amount;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public double getAmount() { return amount; }
    }
}