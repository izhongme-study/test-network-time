package me.izhong.ums.network.test.collect;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class StatisticsService  {

    @Autowired
    private InfluxDB influxdb;

    private List<Point> points = Collections.synchronizedList(new ArrayList<>());

    final static private int INTERVAL = 1000;

    private Timer reportTimer;

    private Map<String, StatData> map = new ConcurrentHashMap<String, StatData>();

    private String serverName="TEST";
    private String appName="TEST";

    private ReentrantLock lock = new ReentrantLock();

    @PostConstruct
    public void init() {
        reportTimer = new Timer(true);
        reportTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Point[] pointArray;
                lock.lock();
                try {
                    pointArray = points.toArray(new Point[0]);
                    points.clear();
                }finally {
                    lock.unlock();
                }
                sendToInfluxdb(pointArray);
            }
        }, INTERVAL, INTERVAL);

        reportTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    log.debug("Report statistics data.");
                    if (map.size() > 0  ) {
                        // make a copy of original map to avoid changing during collecting.
                        Map<String, StatData> copy = new HashMap<String, StatData>(map.size());
                        copy.putAll(map);
                        map.clear();
                        //收集到缓存
                        collect(serverName, copy);
                    }
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }, INTERVAL * 3, INTERVAL);
    }

    private void sendToInfluxdb(Point[] pointArray) {
        if(pointArray == null || pointArray.length == 0)
            return;

        try {
            BatchPoints batchPoints = BatchPoints
                    .database("netpay")
                    .retentionPolicy("netpay_default")
                    .consistency(InfluxDB.ConsistencyLevel.ALL)
                    .points(pointArray)
                    .build();
            influxdb.write(batchPoints);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    public void collect(String serverName, Map<String, StatData> dataMap) {
        lock.lock();
        try {
            long now = System.currentTimeMillis();
            for (StatData data : dataMap.values()) {
                try {
                    Point point = Point.measurement("statistics")
                            .time(now, TimeUnit.MILLISECONDS)
                            .tag("server", serverName)
                            .tag("event", data.getKey())
                            .tag("app", data.getAppName())
                            .tag("category", data.getCategory())
                            .tag("action", data.getAction())
                            .tag("result", data.getResult())
                            .addField("count", data.getCount())
                            .addField("size", data.getSize())
                            .addField("min", data.getMin())
                            .addField("max", data.getMax())
                            .addField("total", data.getTotal())
                            .addField("total2", data.getTotal2())
                            .addField("average", data.getAverage())
                            .build();
                    points.add(point);
                }catch (Exception e){
                    log.error("Collect stat error: ", e);
                }
            }
        } finally {
            lock.unlock();
        }
    }


    public void accumulate(String category, String action, String result, long count, double cost) {
        //service 由其他服务器dubbo协议提供，如果未依赖注入service无法进行统计，直接返回
        try {
            String key = String.format("%s.%s.%s.%s", appName, category, action, result);
            StatData c = map.get(key);
            if (c == null) {
                c = new StatData(key, appName, category, action, result, new ReentrantLock());
                map.put(key, c);
            }
            c.accumulate(count, cost);
        } catch (Exception e) {
            log.error("", e);
        }
    }

}
