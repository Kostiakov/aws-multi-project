package com.example.ec2.service;

public interface NotificationService {

    boolean registerEmail(String email);

    boolean unregisterEmail(String email);

}
