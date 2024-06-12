package com.sky.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

//@Component
public class MyTask {

    // second, minute, hour, day of month, month, day of week, year(optional)
    @Scheduled(cron = "0/5 * * * * ?")
    public void run() {
        System.out.println("MyTask.run()");
    }
}
