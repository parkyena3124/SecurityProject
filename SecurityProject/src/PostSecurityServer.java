import com.sun.net.httpserver.HttpExchange; // 한 번의 HTTP 요청과 응답 정보를 다루는 클래스를 불러옵니다.
import com.sun.net.httpserver.HttpServer; // 자바 기본 웹 서버를 만드는 클래스를 불러옵니다.

import java.io.IOException; // 입출력 중 발생할 수 있는 예외 클래스를 불러옵니다.
import java.io.OutputStream; // 서버가 브라우저로 데이터를 보내는 출력 통로를 불러옵니다.
import java.net.InetSocketAddress; // 서버 주소와 포트번호를 지정하는 클래스를 불러옵니다.
import java.nio.charset.StandardCharsets; // 한글을 UTF-8로 처리하기 위한 상수를 불러옵니다.
import java.util.HashMap;
import java.util.Map;
import java.net.URLDecoder;

public class PostSecurityServer {
    public static void main(String[] args) throws IOException {

        // 내 컴퓨터의 8081번 포트에 웹 서버 생성
        HttpServer server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 8081),
                0
        );

        // 첫 로그인 화면 처리
        server.createContext("/", exchange -> {

            // GET 요청만 허용
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())){
                sendResponse(
                        exchange,
                        405,
                        "<h2>GET 요청만 허용됩니다.<h2>"
                );
                return;
            }

            String html = """
                    <!DOCTYPE html>
                    <html lang="ko">
                    <head>
                        <meta charset="UTF-8">
                        <title>입력값 검증 실습</title>
                    </head>
                    <body>
                        <h2>로그인 입력값 검증 실습</h2>
                        
                    <form action="/login" method="post">
                        <p>
                            아이디:
                            <input type="text" name="id">
                        </p>
                        
                        <p>
                            비밀번호:
                            <input type="password" name="password">
                        </p>
                        
                        <button type="submit">로그인</button>
                    </form>
                    
                    <p>
                        아이디는 영문자와 숫자로
                        4자 이상 20자 이하로 입력하세요.
                    </p>
                    
                    <p>
                        비밀번호는 8자 이상 30자 이하로 입력하세요.
                    </p>
            </body>
            </html>
            """;

            sendResponse(exchange,200,html);
        });

        // 로그인 POST 요청 처리
        server. createContext("/login", exchange ->{

            String method = exchange.getRequestMethod();

            System.out.println();
            System.out.println("[로그인 요청 수신]");
            System.out.println("요청 방식: "+method);

            // POST 요청만 허용
            if (!"POST".equalsIgnoreCase(method)){
                sendResponse(
                        exchange,
                        405,
                        "<h2>POST 요청만 허용됩니다.</h2>"
                );
                return;
            }

            // POST 요청 본문 읽기
            byte[] requestBytes =
                    exchange.getRequestBody().readAllBytes();

            String requestData = new String(
                    requestBytes,
                    StandardCharsets.UTF_8
            );

            // id와 password를 분리
            Map<String, String> formData =
                    parseFormData(requestData);

            String id = formData.get("id");
            String password = formData.get("password");

            //아이디 입력 여부 검사
            if (id == null || id. isBlank()){
                sendResponse(
                        exchange,
                        400,
                        errorPage("아이디를 입력하세요.")
                );
                return;
            }

            // 아이디 형식 검사
            if (!id.matches("[A-Za-z0-9]{4,20}")){
                sendResponse(
                        exchange,
                        400,
                        errorPage(
                                "아이디는 영문자와 숫자로 "+
                                        "4자 이상 20자 이하만 가능합니다."
                        )
                );
                return;
            }

            // 비밀번호 입력 여부 검사
            if (password == null || password.isBlank()){
                sendResponse(
                        exchange,
                        400,
                        errorPage("비밀번호를 입력하세요.")
                );
                return;
            }

            // 비밀번호 길이 검사
            if(password.length()<8 ||
                    password.length() > 30){

                sendResponse(
                        exchange,
                        400,
                        errorPage(
                                "비밀번호는 8자 이상 "+
                                        "30자 이하로 입력하세요."
                        )
                );
                return;
            }

            // 모든 검사를 통과한 경우
            System.out.println("입력값 검사 통과");
            System.out.println("전송받은 아이디: "+id);

            String result = """
                    <!DOCTYPE html>
                    <html lang="ko">
                    <head>
                        <meta charset="UTF-8">
                        <title>검사 결과</title>
                    </head>
                    <body>
                        <h2>입력값 검사를 통과했습니다.</h2>
                        
                        <p>
                            현재는 실제 로그인을 확인하지 않고
                            입력 형식만 검사합니다.
                        </p>
                        
                        <a href="/">로그인 화면으로 돌아가기</a>
                    </body>
                    </html>
                    """;

            sendResponse(exchange, 200, result);
        });

        server.start();

        System.out.println("POST 실습 서버가 실행되었습니다.");
        System.out.println("접속 주소: http://127.0.0.1:8081");
    }

    // POST 데이터를 항목별로 분리하는 메서드
    private static Map<String, String> parseFormData(
            String requestData
    ) {

        Map<String, String> formData = new HashMap<>();

        String[] items = requestData.split("&");

        for (String item : items) {

            String[] nameAndValue = item.split("=", 2);

            String name = URLDecoder.decode(
                    nameAndValue[0],
                    StandardCharsets.UTF_8
            );

            String value = "";

            if (nameAndValue.length == 2) {
                value = URLDecoder.decode(
                        nameAndValue[1],
                        StandardCharsets.UTF_8
                );
            }

            formData.put(name, value);
        }

        return formData;
    }

    // 오류 화면을 만드는 메서드
    private static String errorPage(String message) {

        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8">
                    <title>입력 오류</title>
                </head>
                <body>
                    <h2>입력값이 올바르지 않습니다.</h2>

                    <p>%s</p>

                    <a href="/">다시 입력하기</a>
                </body>
                </html>
                """.formatted(message);
    }

    // 브라우저에 응답을 전송하는 메서드
    private static void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String responseText
    ) throws IOException {

        byte[] responseBytes =
                responseText.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type",
                "text/html; charset=UTF-8"
        );

        exchange.sendResponseHeaders(
                statusCode,
                responseBytes.length
        );

        try (OutputStream output =
                     exchange.getResponseBody()) {

            output.write(responseBytes);
        }
    }
}



