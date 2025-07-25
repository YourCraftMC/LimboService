/*
  ~ This file is part of Limbo.
  ~
  ~ Copyright (C) 2024. YourCraftMC <admin@ycraft.cn>
  ~ Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
  ~ Copyright (C) 2022. Contributors
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
 */

package com.loohp.limbo.scheduler;

import cn.ycraft.limbo.config.ServerConfig;
import com.loohp.limbo.Limbo;
import com.loohp.limbo.scheduler.LimboScheduler.CurrentSchedulerTask;
import com.loohp.limbo.scheduler.LimboScheduler.LimboSchedulerTask;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Tick {

    private int tickingInterval;
    private final AtomicLong tick = new AtomicLong(0);

    private final ExecutorService ASYNC_EXECUTOR = Executors.newFixedThreadPool(4);
    private final Queue<LimboSchedulerTask> asyncTasksQueue = new ConcurrentLinkedQueue<>();

    public Tick(Limbo instance) {
        new Thread(() -> {
            tickingInterval = (int) Math.round(1000.0 / ServerConfig.SERVER.TPS.resolve());

            for (int i = 0; i < 4; i++) {
                ASYNC_EXECUTOR.submit(() -> {
                    while (instance.isRunning()) {
                        LimboSchedulerTask task = asyncTasksQueue.poll();
                        if (task == null) {
                            try {
                                TimeUnit.NANOSECONDS.sleep(10000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            LimboTask limboTask = task.getTask();
                            try {
                                limboTask.run();
                            } catch (Throwable e) {
                                System.err.println("Task " + task.getTaskId() + " threw an exception: " + e.getLocalizedMessage());
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }

            while (instance.isRunning()) {
                long start = System.currentTimeMillis();
                tick.incrementAndGet();
                instance.getPlayers().forEach(each -> {
                    if (each.clientConnection.isReady()) {
                        each.playerInteractManager.update();
						/*
						try {
							each.getDataWatcher().update();
						} catch (IllegalArgumentException | IllegalAccessException e) {
							e.printStackTrace();
						}
						*/
                    }
                });
                instance.getWorlds().forEach(each -> {
                    try {
                        each.update();
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });

                CurrentSchedulerTask tasks = instance.getScheduler().collectTasks(getCurrentTick());
                if (tasks != null) {
                    asyncTasksQueue.addAll(tasks.getAsyncTasks());

                    tasks.getSyncedTasks().forEach(task -> {
                        LimboTask limboTask = task.getTask();
                        try {
                            limboTask.run();
                        } catch (Throwable e) {
                            System.err.println("Task " + task.getTaskId() + " threw an exception: " + e.getLocalizedMessage());
                            e.printStackTrace();
                        }
                    });
                }

                long end = System.currentTimeMillis();
                try {
                    TimeUnit.MILLISECONDS.sleep(tickingInterval - (end - start));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public long getCurrentTick() {
        return tick.get();
    }

    public void waitAndKillThreads(long waitTime) {
        try {
            ASYNC_EXECUTOR.awaitTermination(waitTime, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
