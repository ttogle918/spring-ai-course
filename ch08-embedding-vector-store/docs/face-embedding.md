# 얼굴 임베딩 예제

목적

- 이미지(얼굴)를 외부 얼굴 임베딩 API로 전송해 벡터를 얻고, 데이터베이스에 저장 후 유사도 검색으로 인물 식별을 수행합니다.

관련 코드

- `src/main/java/com/example/demo/service/FaceService.java`
- DB 관련 SQL: `src/main/resources/sql/table.sql`

핵심 흐름

1. 업로드된 파일(`MultipartFile`)을 외부 얼굴 임베딩 API에 multipart/form-data로 전송(`WebClient`).
2. API의 응답에서 벡터를 받아 문자열로 변환해 Postgres의 벡터 타입(예: `vector`)으로 저장.
3. 검색 시 입력 이미지를 임베딩으로 변환한 뒤 SQL의 코사인 거리 연산자(`<=>`)로 유사도를 계산해 가장 가까운 결과를 반환.

확인 포인트

- 외부 얼굴 임베딩 API(예: 로컬 테스트 서버)가 정상 응답하는지 확인
- DB에 저장된 벡터의 형식과 검색 시 사용되는 SQL 문법(postgres vector 확장 등)을 점검
- 검색 결과의 유사도 임계값(예: 0.3)이 적절히 설정되어 있는지 실험

보안/프라이버시 주의

- 얼굴 데이터는 민감정보에 해당하므로 저장/전송 시 암호화, 접근 제어, 보관주기 정책을 수립하세요.
