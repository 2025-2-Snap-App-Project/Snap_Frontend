**📱시각장애인을 위한 식품 정보 안내 서비스**

이 프로젝트는 시각장애인의 식품 구매 과정에서의 불편함을 해소하기 위해 개발된 **Android 앱**으로,

카메라로 제품을 비추기만 하면 **제품명, 소비기한, 원재료명, 요약 정보**를 음성으로 안내해주는 서비스입니다.

---------

**✨ 주요 기능**

**🔍 1. 실시간 제품 인식 (YOLO + ML Kit)**

- Ultralytics YOLO 기반 실시간 객체 탐지
→ 제품명, 제품 라벨 자동 인식

- Google ML Kit로 소비기한(날짜) 인식

- 제품명/라벨/소비기한이 모두 인식되면 서버에 이미지 전송

**🗣️ 2. 음성 안내 기능**

- 안드로이드 내장 TTS(Text-to-Speech) 사용

- TalkBack 사용 환경을 고려하여 설계

- 인식된 제품 정보를 음성으로 안내

**📸 3. 카메라 기능**

- CameraX 기반의 실시간 카메라 구현

- YOLO 모델과의 실시간 추론 연동

**🗂️ 4. 로컬 저장 기능 (Room DB)**

- 사용자가 저장한 제품 정보를 앱 내부 DB로 관리합니다.
  
|제목|내용|
|------|---|
|product_id|고유 ID|
|storage_location|보관 장소|
|product_name|제품명|
|expiration_date|소비기한|
|summary|요약 설명|
|ingredients|원재료명|

**🧩 전체 동작 흐름**

1. 클라이언트(앱) : CameraX 실행

2. 클라이언트 : YOLO + ML Kit로 제품명, 라벨, 소비기한 실시간 탐지

3. 클라이언트 : 모든 요소가 인식되면 → 서버로 이미지 전송

4. 서버 : Google Cloud Vision API로 OCR 실행

5. 서버 : Gemini가 제품 정보 요약 생성

6. 클라이언트 : 서버 결과를 받아 TTS로 사용자에게 음성 안내

**🏗️ 기술 스택**

- Android Studio + Kotlin

- Design Pattern : MVC

- CameraX

- Ultralytics YOLO

- Google ML Kit

- Room Database

- Android TTS
