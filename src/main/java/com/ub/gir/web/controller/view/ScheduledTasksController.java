package com.ub.gir.web.controller.view;


import java.io.IOException;
import java.text.ParseException;

import javax.annotation.Resource;

import com.ub.gir.web.service.ScheduledTasks;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@CrossOrigin(origins = "http://localhost:8082")
@RestController
@RequestMapping({"/tasks"})
public class ScheduledTasksController {
    @Resource
    ScheduledTasks scheduledTasks;

    @PostMapping({"/deleteLogTrack"})
    public void deleteLogTrack() throws IOException, ParseException {
        scheduledTasks.deleteLogTrack();
    }

    @PostMapping({"/transferToHisRec"})
    public void transferToHisRec() throws ParseException, IOException {
        scheduledTasks.transferToHisRec();
    }

    @PostMapping({"/transferHistoryRecToDeleteRec"})
    public void transferHistoryRecToDeleteRec() throws IOException, ParseException {
        scheduledTasks.transferHistoryRecToDeleteRec();
    }

    @PostMapping({"/cleanOfDelRec"})
    public void cleanOfDelRec() throws IOException, ParseException {
        scheduledTasks.cleanOfDelRec();
    }

    @PostMapping({"/loadFromExternalDb"})
    public void loadFromExternalDb() {
        scheduledTasks.loadFromExternalDb();
    }
}