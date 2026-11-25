// 사용 예: docker compose run --rm k6 run /scripts/smoke.js
// 대상 주소를 바꾸려면: docker compose run --rm -e TARGET=http://host.docker.internal:8082 k6 run /scripts/smoke.js
import http from 'k6/http';
import { check, sleep } from 'k6';

const TARGET = __ENV.TARGET || 'http://host.docker.internal:8081';

export const options = {
  vus: 10,            // 동시에 요청하는 가상 사용자 수
  duration: '30s',    // 테스트 시간
  thresholds: {
    http_req_failed: ['rate<0.01'],     // 실패율 1% 미만
    http_req_duration: ['p(95)<500'],   // 95%의 요청이 500ms 안에 응답
  },
};

export default function () {
  const res = http.get(`${TARGET}/actuator/health`);
  check(res, { 'status 200': (r) => r.status === 200 });
  sleep(1);
}
