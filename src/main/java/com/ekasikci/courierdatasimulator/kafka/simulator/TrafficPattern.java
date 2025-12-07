package com.ekasikci.courierdatasimulator.kafka.simulator;

import java.time.LocalTime;

/**
 * Traffic patterns that simulate real-world courier data flow
 */
public enum TrafficPattern {

    /**
     * Constant rate - steady flow all day
     */
    CONSTANT {
        @Override
        public double getMultiplier(LocalTime time) {
            return 1.0;
        }
    },

    /**
     * Morning rush - high traffic 8-10am
     */
    MORNING_RUSH {
        @Override
        public double getMultiplier(LocalTime time) {
            int hour = time.getHour();
            if (hour >= 8 && hour < 10) {
                return 5.0; // 5x traffic
            } else if (hour >= 10 && hour < 12) {
                return 2.0; // 2x traffic (winding down)
            }
            return 1.0;
        }
    },

    /**
     * Random spike - sudden unpredictable increases
     */
    SPIKE {
        @Override
        public double getMultiplier(LocalTime time) {
            // Random spike every ~15 minutes
            if (Math.random() < 0.05) {
                return 10.0 + Math.random() * 10.0; // 10-20x spike
            }
            return 1.0;
        }
    },

    /**
     * Realistic - follows typical daily courier pattern
     */
    REALISTIC {
        @Override
        public double getMultiplier(LocalTime time) {
            int hour = time.getHour();

            // Morning rush (8-10am): 5x
            if (hour >= 8 && hour < 10) {
                return 5.0;
            }
            // Lunch spike (12-1pm): 3x
            else if (hour >= 12 && hour < 13) {
                return 3.0;
            }
            // Evening peak (5-7pm): 4x
            else if (hour >= 17 && hour < 19) {
                return 4.0;
            }
            // Night (11pm-6am): 0.2x
            else if (hour >= 23 || hour < 6) {
                return 0.2;
            }
            // Normal hours
            else {
                return 1.0;
            }
        }
    },

    /**
     * Black Friday - extreme high load all day
     */
    BLACK_FRIDAY {
        @Override
        public double getMultiplier(LocalTime time) {
            int hour = time.getHour();

            // Peak hours (9am-9pm): 20x
            if (hour >= 9 && hour < 21) {
                return 20.0 + Math.random() * 10.0; // 20-30x with variance
            }
            // Other hours: still 5x normal
            else {
                return 5.0;
            }
        }
    };

    /**
     * Get traffic multiplier for current time
     * 
     * @param time Current time
     * @return Multiplier to apply to base rate (1.0 = normal, 5.0 = 5x traffic)
     */
    public abstract double getMultiplier(LocalTime time);

    /**
     * Calculate actual rate based on base rate and current time
     */
    public int calculateRate(int baseRate, LocalTime time) {
        return (int) (baseRate * getMultiplier(time));
    }
}