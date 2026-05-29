package com.auction.client.util;

import org.controlsfx.control.Notifications;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.util.Duration;

public class NotificationUtil {

    public static void showPushNotification(String title, String message) {
        Platform.runLater(() -> {
            Notifications.create()
                    .title(title)
                    .text(message)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.BOTTOM_RIGHT)
                    .showInformation(); // Mặc định hiển thị icon thông tin
        });
    }

    public static void showPushNotificationWarning(String title, String message) {
        Platform.runLater(() -> {
            Notifications.create()
                    .title(title)
                    .text(message)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.BOTTOM_RIGHT)
                    .showWarning();
        });
    }
}
