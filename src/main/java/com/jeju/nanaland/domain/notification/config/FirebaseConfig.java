package com.jeju.nanaland.domain.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class FirebaseConfig {

  @PostConstruct
  public void init() {
    try {
      FileInputStream serviceAccount =
          new FileInputStream("src/main/resources/firebase/nanaland-firebase-key.json");
      FirebaseOptions options = FirebaseOptions.builder()
          .setCredentials(GoogleCredentials.fromStream(serviceAccount))
          .build();
      FirebaseApp.initializeApp(options);
    } catch (Exception e) {
      log.info("Firebase configuration error:", e.getMessage());
    }
  }
}
