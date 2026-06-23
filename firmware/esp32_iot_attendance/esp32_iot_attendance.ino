/**
 * AuraGate IoT Attendance Terminal Node
 * ----------------------------------------------------
 * Target Hardware: ESP32 or ESP8266
 * Sensors: AS608 Fingerprint, RC522 RFID Reader, LiquidCrystal_I2C LCD
 * Feedback: Red/Green LEDs and active buzzer
 * Libraries Required:
 *   - Adafruit Fingerprint Sensor Library
 *   - MFRC522 (by githubyuri/OSw)
 *   - LiquidCrystal_I2C (by Frank de Brabander)
 *   - ArduinoJson (by Benoit Blanchon)
 */

#if defined(ESP8266)
  #include <ESP8266WiFi.h>
  #include <ESP8266HTTPClient.h>
  #include <SoftwareSerial.h>
#elif defined(ESP32)
  #include <WiFi.h>
  #include <HTTPClient.h>
  #include <HardwareSerial.h>
#endif

#include <SPI.h>
#include <MFRC522.h>
#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <Adafruit_Fingerprint.h>
#include <ArduinoJson.h>

// --- WIFI CONFIGURATION ---
const char* ssid = "YOUR_WIFI_SSID";
const char* password = "YOUR_WIFI_PASSWORD";

// --- BACKEND API ENDPOINT CONFIGURATION ---
const char* serverUrl = "http://192.168.1.100:8080/api/iot/scan"; // Replace with your Spring Boot Server IP
const char* deviceToken = "SECURE_ESP32_ATTENDANCE_TOKEN_2026";
const int classId = 1; // Optional class location ID associated with this terminal node

// --- ESP32 PIN CONFIGURATIONS ---
#if defined(ESP32)
  #define PIN_RST          4
  #define PIN_SS           5
  #define LED_GREEN        12
  #define LED_RED          14
  #define BUZZER           13
  // AS608 Fingerprint Serial Connection (Hardware Serial 2)
  #define FP_RX            16 
  #define FP_TX            17
#else // ESP8266 Defaults
  #define PIN_RST          D3
  #define PIN_SS           D8
  #define LED_GREEN        D4 // Builtin LED on ESP8266
  #define LED_RED          D0
  #define BUZZER           D1
  #define FP_RX            D2 // Software Serial
  #define FP_TX            D3
#endif

// --- INITIALIZE SENSORS ---
LiquidCrystal_I2C lcd(0x27, 16, 2); // Address 0x27 or 0x3F
MFRC522 mfrc522(PIN_SS, PIN_RST);

#if defined(ESP32)
  HardwareSerial fpSerial(2);
#else
  SoftwareSerial fpSerial(FP_RX, FP_TX);
#endif
Adafruit_Fingerprint fingerprint = Adafruit_Fingerprint(&fpSerial);

void setup() {
  Serial.begin(115200);
  
  // Set GPIO outputs
  pinMode(LED_GREEN, OUTPUT);
  pinMode(LED_RED, OUTPUT);
  pinMode(BUZZER, OUTPUT);
  digitalWrite(LED_GREEN, LOW);
  digitalWrite(LED_RED, LOW);
  digitalWrite(BUZZER, LOW);

  // Initialize LCD Screen
  Wire.begin();
  lcd.init();
  lcd.backlight();
  lcd.setCursor(0, 0);
  lcd.print("AuraGate Node v1");
  lcd.setCursor(0, 1);
  lcd.print("Initializing...");
  delay(1000);

  // Initialize SPI & RFID Card Reader
  SPI.begin();
  mfrc522.PCD_Init();
  Serial.println("RFID MFRC522 initialized.");

  // Initialize AS608 Fingerprint Sensor
  #if defined(ESP32)
    fpSerial.begin(57600, SERIAL_8N1, FP_RX, FP_TX);
  #else
    fpSerial.begin(57600);
  #endif

  if (fingerprint.verifyPassword()) {
    Serial.println("Fingerprint AS608 sensor detected!");
  } else {
    Serial.println("ERROR: AS608 sensor not found.");
    lcd.setCursor(0, 1);
    lcd.print("FP Sensor Error ");
    digitalWrite(LED_RED, HIGH);
    delay(2000);
    digitalWrite(LED_RED, LOW);
  }

  // Connect to local WiFi network
  connectWiFi();
}

void loop() {
  // Check if WiFi connection is still alive
  if (WiFi.status() != WL_CONNECTED) {
    connectWiFi();
  }

  // Option 1: Scan for RFID cards
  if (mfrc522.PICC_IsNewCardPresent() && mfrc522.PICC_ReadCardSerial()) {
    String cardUid = "";
    for (byte i = 0; i < mfrc522.uid.size; i++) {
      cardUid += String(mfrc522.uid.uidByte[i] < 0x10 ? "0" : "");
      cardUid += String(mfrc522.uid.uidByte[i], HEX);
    }
    cardUid.toUpperCase();
    
    Serial.print("RFID card detected! UID: ");
    Serial.println(cardUid);
    
    triggerBeep(1); // Short Beep
    postBiometricScan(0, cardUid); // Send to backend
    
    mfrc522.PICC_HaltA();
    delay(1000);
  }

  // Option 2: Scan for Fingerprint
  int fingerprintId = getFingerprintID();
  if (fingerprintId > 0) {
    Serial.print("Fingerprint matched! Template ID: ");
    Serial.println(fingerprintId);
    
    triggerBeep(1); // Short Beep
    postBiometricScan(fingerprintId, ""); // Send to backend
    
    delay(2000); // Wait between scans
  }

  delay(100);
}

// Check if a finger is scanned on the AS608 module
int getFingerprintID() {
  uint8_t p = fingerprint.getImage();
  if (p != FINGERPRINT_OK) return -1;

  p = fingerprint.image2Tpl();
  if (p != FINGERPRINT_OK) return -1;

  p = fingerprint.fingerFastSearch();
  if (p != FINGERPRINT_OK) {
    // Fingerprint imaged but not stored in database template memory
    Serial.println("Fingerprint does not match any template.");
    triggerFeedback(false, "Unknown Finger", "Scan Failed");
    return -2;
  }

  // Found a match!
  return fingerprint.fingerID;
}

// Connect/Reconnect to local WiFi Router
void connectWiFi() {
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Connecting WiFi");
  Serial.print("Connecting to WiFi: ");
  Serial.println(ssid);
  
  WiFi.begin(ssid, password);
  
  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED && attempts < 20) {
    delay(500);
    Serial.print(".");
    lcd.setCursor(attempts % 16, 1);
    lcd.print(".");
    attempts++;
  }
  
  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\nWiFi connected successfully!");
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("WiFi Connected!");
    lcd.setCursor(0, 1);
    lcd.print("System Ready... ");
    triggerBeep(2); // Double confirmation beep
    delay(1500);
  } else {
    Serial.println("\nWiFi connection failed.");
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("WiFi Conn Error");
    lcd.setCursor(0, 1);
    lcd.print("Retrying...    ");
    digitalWrite(LED_RED, HIGH);
    delay(2000);
    digitalWrite(LED_RED, LOW);
  }
}

// Post scan credentials to the Spring Boot REST API
void postBiometricScan(int fingerprintId, String rfidUid) {
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Verifying scan...");
  
  WiFiClient client;
  HTTPClient http;

  // Build target URL query parameters
  String url = String(serverUrl) + "?classId=" + String(classId);
  http.begin(client, url);
  
  // Set headers
  http.addHeader("Content-Type", "application/json");
  http.addHeader("X-Device-Token", deviceToken);

  // Construct JSON Body
  StaticJsonDocument<200> doc;
  if (fingerprintId > 0) {
    doc["fingerprintId"] = fingerprintId;
  } else if (rfidUid.length() > 0) {
    doc["rfidUid"] = rfidUid;
  }

  String requestBody;
  serializeJson(doc, requestBody);

  // Perform Request
  int httpResponseCode = http.POST(requestBody);

  if (httpResponseCode > 0) {
    String responseString = http.getString();
    Serial.println("Response code: " + String(httpResponseCode));
    Serial.println("Response body: " + responseString);

    StaticJsonDocument<300> filter;
    DeserializationError error = deserializeJson(filter, responseString);

    if (!error) {
      const char* status = filter["status"];
      const char* name = filter["name"];
      const char* message = filter["message"];

      if (strcmp(status, "success") == 0) {
        // Attendance recorded successfully
        triggerFeedback(true, name, message);
      } else {
        // Verification failed (unknown ID or not enrolled)
        triggerFeedback(false, name, message);
      }
    } else {
      // JSON Parsing error
      triggerFeedback(false, "Parse Error", "Bad JSON format");
    }
  } else {
    // Network HTTP failure
    Serial.print("HTTP Post failed: ");
    Serial.println(httpResponseCode);
    triggerFeedback(false, "Server Offline", "Network Error");
  }

  http.end();
}

// Sounds active buzzer for a count of cycles
void triggerBeep(int count) {
  for (int i = 0; i < count; i++) {
    digitalWrite(BUZZER, HIGH);
    delay(100);
    digitalWrite(BUZZER, LOW);
    if (i < count - 1) delay(50);
  }
}

// Display access results and blink corresponding feedback LEDs
void triggerFeedback(bool isSuccess, String name, String message) {
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print(name.substring(0, 16));
  lcd.setCursor(0, 1);
  lcd.print(message.substring(0, 16));

  if (isSuccess) {
    digitalWrite(LED_GREEN, HIGH);
    triggerBeep(1); // One beep success
    delay(2000);
    digitalWrite(LED_GREEN, LOW);
  } else {
    digitalWrite(LED_RED, HIGH);
    triggerBeep(2); // Two beeps failure
    delay(2000);
    digitalWrite(LED_RED, LOW);
  }

  // Restore LCD
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("AuraGate Node v1");
  lcd.setCursor(0, 1);
  lcd.print("System Ready... ");
}
