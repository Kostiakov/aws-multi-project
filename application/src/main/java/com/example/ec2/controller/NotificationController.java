package com.example.ec2.controller;

import com.example.ec2.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/registerEmail")
    public String registerEmail(@RequestParam String email) {
        return notificationService.registerEmail(email) ? "SUCCESS" : "FAIL";
    }

    @PostMapping("/unregisterEmail")
    public String unregisterEmail(@RequestParam String email) {
        return notificationService.unregisterEmail(email) ? "SUCCESS" : "FAIL";
    }

}
