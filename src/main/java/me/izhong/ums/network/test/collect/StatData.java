package me.izhong.ums.network.test.collect;

import lombok.Data;

import java.io.Serializable;
import java.util.concurrent.locks.ReentrantLock;

@Data
public class StatData implements Serializable {
    private String key;
    private long count;
    private long size;
    private double min;
    private double max;
    private double total;
    private long total2;
    private double average;
    private String appName, category, action, result;

    transient private ReentrantLock lock;

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(key);
        buf.append(" ");
        buf.append(count);
        buf.append(" ");
        buf.append(size);
        buf.append(" ");
        buf.append(min);
        buf.append(" ");
        buf.append(max);
        buf.append(" ");
        buf.append(total);
        buf.append(" ");
        buf.append(average);
        return buf.toString();
    }

    public StatData() {

    }

    public StatData(String key, String appName, String category, String action, String result, ReentrantLock lock) {
        this.lock = lock;
        this.appName = appName;
        this.category = category;
        this.action = action;
        this.result = result;

        this.key = key;
        count = 0;
        size = 0;
        min = 0;
        max = 0;
        total = 0;
        total2 = 0;
        average = 0;
    }

    public StatData(String key) {
        this.key = key;
        count = 0;
        size = 0;
        min = 0;
        max = 0;
        total = 0;
        total2 = 0;
        average = 0;
    }

    public void accumulate(long count, double cost) {
        if (lock != null)
            lock.lock();
        try {
            this.count += count;
            this.size += 1;
            if (min < 0.00000001 || cost < min)
                min = cost;
            if (cost > max)
                max = cost;
            total += cost;
            total2 += cost;
            if (this.count > 0)
                average = total / this.count;
        } finally {
            if (lock != null)
                lock.unlock();
        }
    }

    public void accumulate(StatData data) {
        if (lock != null)
            lock.lock();
        try {
            this.count += data.count;
            this.size += 1;
            if (min < 0.00000001 || data.min < min)
                min = data.min;
            if (data.max > max)
                max = data.max;
            total += data.total;
            total2 += data.total2;
            if (this.count > 0)
                average = total / this.count;
        } finally {
            if (lock != null)
                lock.unlock();
        }
    }
}
