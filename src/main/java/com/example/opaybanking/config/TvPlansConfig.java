package com.example.opaybanking.config;

import java.util.*;

public class TvPlansConfig {

    public static final Map<String, List<TvPlan>> TV_PROVIDER_PLANS = new HashMap<>();

    static {
        // DSTV Plans
        TV_PROVIDER_PLANS.put("DSTV", Arrays.asList(
                new TvPlan("1", "DStv Premium", 21000.0),
                new TvPlan("2", "DStv Compact Plus", 12500.0),
                new TvPlan("3", "DStv Compact", 8100.0),
                new TvPlan("4", "DStv Confam", 5300.0),
                new TvPlan("5", "DStv Yanga", 2500.0),
                new TvPlan("6", "DStv Padi", 1900.0)
        ));

        // GOTV Plans
        TV_PROVIDER_PLANS.put("GOTV", Arrays.asList(
                new TvPlan("1", "GOtv Max", 3700.0),
                new TvPlan("2", "GOtv Jolli", 2600.0),
                new TvPlan("3", "GOtv Jinja", 1800.0),
                new TvPlan("4", "GOtv Smallie", 900.0),
                new TvPlan("5", "GOtv Supa", 5400.0)
        ));

        TV_PROVIDER_PLANS.put("STARTIMES", Arrays.asList(
                new TvPlan("1", "Nova", 1300.0),
                new TvPlan("2", "Basic", 2300.0),
                new TvPlan("3", "Smart", 3300.0),
                new TvPlan("4", "Classic", 4300.0),
                new TvPlan("5", "Super", 6300.0)
        ));
    }

    public static class TvPlan {
        private String id;
        private String name;
        private double price;

        public TvPlan(String id, String name, double price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }

        // Getters and Setters
        public String getId() { return id; }
        public String getName() { return name; }
        public double getPrice() { return price; }
    }
}